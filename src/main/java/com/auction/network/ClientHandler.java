package com.auction.network;

import com.auction.model.core.Bid;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object obj = in.readObject();
                if (obj instanceof AuctionMessage) {
                    processMessage((AuctionMessage) obj);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Client disconnected: " + socket.getInetAddress());
        } finally {
            closeConnection();
        }
    }

    private void processMessage(AuctionMessage message) {
        if (message == null || message.getType() == null) return;

        switch (message.getType()) {
            case REGISTER_ADMIN -> AuctionServer.registerAdmin(this);
            case REGISTER_BIDDER -> AuctionServer.registerBidder(this);
            case REGISTER_SELLER -> AuctionServer.registerSeller(this);
            case ITEM_REQUEST -> AuctionServer.handleItemRequest(message.getItem());
            case APPROVE_ITEM -> AuctionServer.approveItem(message.getItemId());
            case REJECT_ITEM -> AuctionServer.rejectItem(message.getItemId());
            case DEPOSIT_REQUEST -> AuctionServer.handleDepositRequest(message.getDepositRequest());
            case APPROVE_DEPOSIT -> AuctionServer.approveDeposit(message.getDepositId());
            case REJECT_DEPOSIT -> AuctionServer.rejectDeposit(message.getDepositId());

            case BID -> {
                Bid bid = message.getBid();
                if (bid != null) {
                    // [FIX 1+2] Lưu bid và cập nhật endTime nếu anti-sniping
                    AuctionServer.handleBid(bid);
                    // Tạo message mới với remainingSeconds cập nhật để tất cả client đồng bộ đồng hồ
                    int remaining = AuctionServer.getRemainingSeconds();
                    AuctionMessage syncedMessage = new AuctionMessage(MessageType.BID, bid, remaining);
                    AuctionServer.broadcast(syncedMessage);
                }
            }

            case AUCTION_ENDED -> {
                // [FIX 3] Client báo hết giờ → server phát AUCTION_ENDED chính thức cho tất cả
                // Chỉ xử lý message "timer_end" từ client (tránh loop)
                if ("timer_end".equals(message.getWinnerName())) {
                    AuctionServer.broadcastAuctionEnded();
                }
            }

            default -> { /* Other message types are server-to-client only */ }
        }
    }

    public void sendToClient(Object message) {
        try {
            out.writeObject(message);
            out.flush();
            out.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection() {
        AuctionServer.removeClient(this);
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
