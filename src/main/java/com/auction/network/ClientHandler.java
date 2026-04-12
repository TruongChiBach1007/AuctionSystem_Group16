package com.auction.network;

import com.auction.model.Auction;
import com.auction.model.Bid;
import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Auction auction;

    public ClientHandler(Socket socket, Auction auction) {
        this.socket = socket;
        this.auction = auction;
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
                if (obj instanceof Bid) {
                    Bid newBid = (Bid) obj;
                    processBid(newBid);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Client disconnected: " + socket.getInetAddress());
        } finally {
            closeConnection();
        }
    }

    private void processBid(Bid bid) {
        synchronized (auction) {
            try {
                auction.placeBid(bid);
                System.out.println("New bid accepted: " + bid.getAmount());
            } catch (Exception e) {
                sendToClient("Error: " + e.getMessage());
            }
        }
    }

    public void sendToClient(Object message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}