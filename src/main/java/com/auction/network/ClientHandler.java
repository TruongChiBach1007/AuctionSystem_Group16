package com.auction.network;

import com.auction.model.core.Bid;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
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
                if (obj instanceof AuctionMessage message) {
                    processMessage(message);
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
            case AUCTION_OPENED -> AuctionServer.openAuction(message.getItem(), this);
            case AUCTION_STOPPED -> AuctionServer.stopAuction(message.getItemId());
            case BID -> {
                Bid bid = message.getBid();
                if (bid != null) {
                    AuctionServer.handleBid(message.getItemId(), bid);
                }
            }
            case AUCTION_ENDED -> {
                if ("timer_end".equals(message.getWinnerName())) {
                    AuctionServer.broadcastAuctionEnded(message.getItemId());
                }
            }
            default -> { }
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
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
