package com.auction.network;

import com.auction.model.core.AuctionSummary;
import com.auction.model.core.Bid;
import com.auction.model.core.DepositRequest;
import com.auction.model.core.DepositStatus;
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
    private static final int AUCTION_DURATION_SECONDS = 600;
    private static final int SNIPING_EXTEND_SECONDS = 15;

    private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private static final List<ClientHandler> admins = new CopyOnWriteArrayList<>();
    private static final List<ClientHandler> bidders = new CopyOnWriteArrayList<>();
    private static final Map<String, Item> pendingItems = new ConcurrentHashMap<>();
    private static final Map<String, Item> approvedItems = new ConcurrentHashMap<>();
    private static final Map<String, DepositRequest> pendingDeposits = new ConcurrentHashMap<>();
    private static final Map<String, AuctionSession> auctionSessions = new ConcurrentHashMap<>();
    private static boolean started = false;

    private static class AuctionSession {
        private final Item item;
        private final List<Bid> bidHistory = new CopyOnWriteArrayList<>();
        private volatile double currentHighestBid;
        private volatile String currentHighestBidderName;
        private volatile long endTimeMillis;
        private volatile boolean stopped;

        private AuctionSession(Item item) {
            this.item = item;
            this.currentHighestBid = item.getCurrentPrice();
            this.endTimeMillis = System.currentTimeMillis() + AUCTION_DURATION_SECONDS * 1000L;
        }

        private int remainingSeconds() {
            return (int) Math.max(0, (endTimeMillis - System.currentTimeMillis()) / 1000);
        }

        private AuctionSummary toSummary() {
            String status = stopped || remainingSeconds() <= 0 ? "Đã dừng" : "Đang mở";
            return new AuctionSummary(
                    item.getId(),
                    item.getName(),
                    currentHighestBid,
                    currentHighestBidderName,
                    remainingSeconds(),
                    status
            );
        }
    }

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
        loadApprovedItems();
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

    private static void loadApprovedItems() {
        for (Item item : DatabaseConnection.getInstance().getItemTable()) {
            if (item.getStatus() == ItemStatus.APPROVED) {
                approvedItems.put(item.getId(), item);
            } else if (item.getStatus() == ItemStatus.PENDING) {
                pendingItems.put(item.getId(), item);
            }
        }
    }

    public static void registerAdmin(ClientHandler client) {
        admins.add(client);
        for (Item item : pendingItems.values()) {
            client.sendToClient(new AuctionMessage(MessageType.ITEM_PENDING, item));
        }
        // Gửi toàn bộ sản phẩm đã duyệt để admin thấy và có thể xóa
        for (Item item : approvedItems.values()) {
            client.sendToClient(new AuctionMessage(MessageType.ITEM_APPROVED, item));
        }
        for (DepositRequest request : pendingDeposits.values()) {
            client.sendToClient(new AuctionMessage(MessageType.DEPOSIT_PENDING, request));
        }
        client.sendToClient(new AuctionMessage(MessageType.SYNC_AUCTIONS, getAuctionSummaries()));
    }

    public static void registerBidder(ClientHandler client) {
        bidders.add(client);
        // ✅ Chỉ gửi items còn trong DB (đã sync với deleteItem)
        for (Item item : DatabaseConnection.getInstance().getItemTable()) {
            if (item.getStatus() == ItemStatus.APPROVED) {
                client.sendToClient(new AuctionMessage(MessageType.ITEM_APPROVED, item));
            }
        }
    }

    public static void handleItemRequest(Item item) {
        if (item == null || item.getId() == null) return;
        item.setStatus(ItemStatus.PENDING);
        pendingItems.put(item.getId(), item);
        upsertItemInDatabase(item);
        broadcastToAdmins(new AuctionMessage(MessageType.ITEM_PENDING, item));
    }

    public static void approveItem(String itemId) {
        Item item = pendingItems.remove(itemId);
        if (item == null) return;
        item.setStatus(ItemStatus.APPROVED);
        approvedItems.put(item.getId(), item);
        upsertItemInDatabase(item);
        broadcast(new AuctionMessage(MessageType.ITEM_APPROVED, item));
    }

    public static void rejectItem(String itemId) {
        Item item = pendingItems.remove(itemId);
        if (item == null) return;
        item.setStatus(ItemStatus.REJECTED);
        upsertItemInDatabase(item);
        broadcast(new AuctionMessage(MessageType.ITEM_REJECTED, item));
    }

    public static void deleteItem(String itemId) {
        // Xóa khỏi danh sách approved
        Item item = approvedItems.remove(itemId);
        if (item == null) return;
        // Xóa khỏi database
        DatabaseConnection.getInstance().getItemTable().removeIf(i -> i.getId().equals(itemId));
        // Dừng phiên đấu giá nếu đang chạy
        AuctionSession session = auctionSessions.get(itemId);
        if (session != null) {
            session.stopped = true;
        }
        // Broadcast cho tất cả client (bidder mất sản phẩm, admin cập nhật danh sách)
        broadcast(new AuctionMessage(MessageType.DELETE_ITEM, itemId, true));
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

    public static synchronized void openAuction(Item item, ClientHandler requester) {
        if (item == null || item.getId() == null) return;
        approvedItems.putIfAbsent(item.getId(), item);
        AuctionSession session = auctionSessions.computeIfAbsent(item.getId(), id -> new AuctionSession(item));
        requester.sendToClient(new AuctionMessage(
                MessageType.SYNC_BID_HISTORY,
                item.getId(),
                new ArrayList<>(session.bidHistory),
                session.currentHighestBid,
                session.remainingSeconds()
        ));
        if (session.stopped || session.remainingSeconds() <= 0) {
            requester.sendToClient(new AuctionMessage(
                    MessageType.AUCTION_ENDED,
                    item.getId(),
                    session.currentHighestBidderName != null ? session.currentHighestBidderName : "Không có",
                    session.currentHighestBid,
                    true
            ));
        }
        broadcastToAdmins(new AuctionMessage(MessageType.AUCTION_OPENED, session.toSummary()));
    }

    public static synchronized void handleBid(String itemId, Bid bid) {
        if (itemId == null || bid == null || bid.getBidder() == null) return;
        AuctionSession session = auctionSessions.get(itemId);
        if (session == null || session.stopped || session.remainingSeconds() <= 0) return;
        if (bid.getAmount() <= session.currentHighestBid) return;
        if (bid.getBidder().getBalance() < bid.getAmount()) return;

        session.bidHistory.add(0, bid);
        session.currentHighestBid = bid.getAmount();
        session.currentHighestBidderName = bid.getBidderName();
        session.item.setCurrentPrice(bid.getAmount());
        upsertItemInDatabase(session.item);

        long remaining = session.endTimeMillis - System.currentTimeMillis();
        if (remaining <= 10_000) {
            session.endTimeMillis += SNIPING_EXTEND_SECONDS * 1000L;
            System.out.println(">>> [SERVER] Anti-sniping cho " + session.item.getName()
                    + ": gia hạn thêm " + SNIPING_EXTEND_SECONDS + "s");
        }

        int remainingSeconds = session.remainingSeconds();
        AuctionServer.broadcast(new AuctionMessage(MessageType.BID, itemId, bid, remainingSeconds));
        broadcastToAdmins(new AuctionMessage(MessageType.AUCTION_OPENED, session.toSummary()));
    }

    public static synchronized void stopAuction(String itemId) {
        AuctionSession session = auctionSessions.get(itemId);
        if (session == null) return;
        session.stopped = true;
        broadcast(new AuctionMessage(
                MessageType.AUCTION_ENDED,
                itemId,
                session.currentHighestBidderName != null ? session.currentHighestBidderName : "Không có",
                session.currentHighestBid,
                true
        ));
        broadcastToAdmins(new AuctionMessage(MessageType.AUCTION_STOPPED, session.toSummary()));
    }

    public static synchronized void broadcastAuctionEnded(String itemId) {
        stopAuction(itemId);
    }

    private static List<AuctionSummary> getAuctionSummaries() {
        return auctionSessions.values().stream()
                .map(AuctionSession::toSummary)
                .toList();
    }

    private static void upsertItemInDatabase(Item item) {
        List<Item> items = DatabaseConnection.getInstance().getItemTable();
        for (int i = 0; i < items.size(); i++) {
            if (item.getId().equals(items.get(i).getId())) {
                items.set(i, item);
                return;
            }
        }
        items.add(item);
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
    }
}
