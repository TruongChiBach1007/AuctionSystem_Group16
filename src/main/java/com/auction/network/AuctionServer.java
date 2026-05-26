package com.auction.network;

import com.auction.model.core.Bid;
import com.auction.model.core.DepositRequest;
import com.auction.model.core.DepositStatus;
import com.auction.model.core.Auction;
import com.auction.model.items.Item;
import com.auction.model.items.ItemStatus;
import com.auction.model.users.Bidder;
import com.auction.model.users.User;
import com.auction.utils.DatabaseConnection;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuctionServer {
    private static final int PORT = 1234;
    private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private static final List<ClientHandler> admins = new CopyOnWriteArrayList<>();
    private static final List<ClientHandler> bidders = new CopyOnWriteArrayList<>();
    private static final List<ClientHandler> sellers = new CopyOnWriteArrayList<>();
    private static final Map<String, Item> pendingItems = new ConcurrentHashMap<>();
    private static final Map<String, Item> approvedItems = new ConcurrentHashMap<>();
    private static final Map<String, DepositRequest> pendingDeposits = new ConcurrentHashMap<>();
    private static final Auction auction = new Auction(10000.0, 3600000);
    private static boolean started = false;

    // [FIX 1] Lưu lịch sử bid và giá cao nhất để sync cho người mới vào
    private static final List<Bid> globalBidHistory = new CopyOnWriteArrayList<>();
    private static volatile double currentHighestBid = 10000.0;
    private static volatile String currentHighestBidderName = null;

    // [SYNC TIMER] Lưu thời điểm kết thúc phiên — client mới join sẽ nhận số giây còn lại thực tế
    private static final int AUCTION_DURATION_SECONDS = 600; // 10 phút, khớp với client
    private static volatile long auctionEndTimeMillis = System.currentTimeMillis() + AUCTION_DURATION_SECONDS * 1000L;
    private static final int SNIPING_EXTEND_SECONDS = 15; // Gia hạn khi có bid trong 10s cuối

    public static void main(String[] args) {
        runServer();
    }

    public static synchronized void startInBackground() {
        if (started) return;
        started = true;
        Thread serverThread = new Thread(AuctionServer::runServer, "AuctionSocketServer");
        serverThread.setDaemon(true);
        serverThread.start();
    }

    private static void runServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println(">>> Auction Server is running on port " + PORT + "...");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println(">>> New client connected: " + socket.getInetAddress());
                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (BindException e) {
            System.out.println(">>> Auction Server already running on port " + PORT + ".");
        } catch (IOException e) {
            System.err.println(">>> Server Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void registerAdmin(ClientHandler client) {
        admins.add(client);
        for (Item item : pendingItems.values()) {
            client.sendToClient(new AuctionMessage(MessageType.ITEM_PENDING, item));
        }
        for (DepositRequest request : pendingDeposits.values()) {
            client.sendToClient(new AuctionMessage(MessageType.DEPOSIT_PENDING, request));
        }
    }

    public static void registerBidder(ClientHandler client) {
        bidders.add(client);
        for (Item item : approvedItems.values()) {
            client.sendToClient(new AuctionMessage(MessageType.ITEM_APPROVED, item));
        }
        // [SYNC TIMER] Tính số giây còn lại thực tế trên server
        int remainingSeconds = (int) Math.max(0, (auctionEndTimeMillis - System.currentTimeMillis()) / 1000);

        // [FIX 1] Luôn gửi SYNC_BID_HISTORY (kể cả khi chưa có bid nào) để đồng bộ đồng hồ
        List<Bid> snapshot = new ArrayList<>(globalBidHistory);
        client.sendToClient(new AuctionMessage(MessageType.SYNC_BID_HISTORY, snapshot, currentHighestBid, remainingSeconds));
    }

    public static void registerSeller(ClientHandler client) {
        sellers.add(client);
    }

    public static void handleItemRequest(Item item) {
        if (item == null) return;
        item.setStatus(ItemStatus.PENDING);
        pendingItems.put(item.getId(), item);
        broadcastToAdmins(new AuctionMessage(MessageType.ITEM_PENDING, item));
    }

    public static void approveItem(String itemId) {
        Item item = pendingItems.remove(itemId);
        if (item == null) return;
        item.setStatus(ItemStatus.APPROVED);
        approvedItems.put(item.getId(), item);
        DatabaseConnection.getInstance().getItemTable().add(item);
        broadcast(new AuctionMessage(MessageType.ITEM_APPROVED, item));
    }

    public static void rejectItem(String itemId) {
        Item item = pendingItems.remove(itemId);
        if (item == null) return;
        item.setStatus(ItemStatus.REJECTED);
        broadcast(new AuctionMessage(MessageType.ITEM_REJECTED, item));
    }

    public static void handleDepositRequest(DepositRequest request) {
        if (request == null || request.getAmount() <= 0) return;
        request.setStatus(DepositStatus.PENDING);
        pendingDeposits.put(request.getId(), request);
        DatabaseConnection.getInstance().getDepositRequestTable().add(request);
        broadcastToAdmins(new AuctionMessage(MessageType.DEPOSIT_PENDING, request));
    }

    public static void approveDeposit(String depositId) {
        DepositRequest request = pendingDeposits.remove(depositId);
        if (request == null) return;
        request.setStatus(DepositStatus.APPROVED);
        addBalance(request.getUsername(), request.getAmount());
        broadcast(new AuctionMessage(MessageType.DEPOSIT_APPROVED, request));
    }

    public static void rejectDeposit(String depositId) {
        DepositRequest request = pendingDeposits.remove(depositId);
        if (request == null) return;
        request.setStatus(DepositStatus.REJECTED);
        broadcast(new AuctionMessage(MessageType.DEPOSIT_REJECTED, request));
    }

    // [FIX 1 + 2] Xử lý BID: lưu lịch sử, cập nhật giá cao nhất, gia hạn nếu anti-sniping
    public static synchronized void handleBid(Bid bid) {
        if (bid == null) return;
        globalBidHistory.add(0, bid);
        currentHighestBid = bid.getAmount();
        currentHighestBidderName = bid.getBidderName();

        // [SYNC TIMER] Anti-sniping: nếu bid trong 10s cuối → gia hạn server endTime
        long remaining = auctionEndTimeMillis - System.currentTimeMillis();
        if (remaining <= 10_000) {
            auctionEndTimeMillis += SNIPING_EXTEND_SECONDS * 1000L;
            System.out.println(">>> [SERVER] Anti-sniping! Gia hạn thêm " + SNIPING_EXTEND_SECONDS + "s");
        }

        System.out.println(">>> [SERVER] Giá mới: " + currentHighestBid + " bởi " + currentHighestBidderName);
    }

    // [FIX 3] Phát tín hiệu kết thúc phiên cho tất cả client
    public static void broadcastAuctionEnded() {
        String winner = currentHighestBidderName != null ? currentHighestBidderName : "Không có";
        broadcast(new AuctionMessage(MessageType.AUCTION_ENDED, winner, currentHighestBid, true));
        System.out.println(">>> [SERVER] Phiên kết thúc! Người thắng: " + winner + " - Giá: " + currentHighestBid);
    }

    private static void addBalance(String username, long amount) {
        for (User user : DatabaseConnection.getInstance().getUserTable()) {
            if (user instanceof Bidder bidder && user.getUsername().equalsIgnoreCase(username)) {
                bidder.setBalance(bidder.getBalance() + amount);
                return;
            }
        }
    }

    public static void broadcast(Object message) {
        for (ClientHandler client : clients) {
            client.sendToClient(message);
        }
    }

    private static void broadcastToAdmins(Object message) {
        for (ClientHandler admin : admins) {
            admin.sendToClient(message);
        }
    }

    public static void removeClient(ClientHandler client) {
        clients.remove(client);
        admins.remove(client);
        bidders.remove(client);
        sellers.remove(client);
    }

    public static double getCurrentHighestBid() { return currentHighestBid; }
    public static String getCurrentHighestBidderName() { return currentHighestBidderName; }
    public static int getRemainingSeconds() {
        return (int) Math.max(0, (auctionEndTimeMillis - System.currentTimeMillis()) / 1000);
    }
}
