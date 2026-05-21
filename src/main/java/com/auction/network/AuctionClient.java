package com.auction.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Consumer;

public class AuctionClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Consumer<AuctionMessage> messageHandler;

    public void connect(boolean admin, Consumer<AuctionMessage> messageHandler) throws IOException {
        this.messageHandler = messageHandler;
        this.socket = new Socket("localhost", 1234);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());

        send(new AuctionMessage(admin ? MessageType.REGISTER_ADMIN : MessageType.REGISTER_BIDDER));

        Thread listener = new Thread(this::listen, admin ? "AdminSocketClient" : "BidderSocketClient");
        listener.setDaemon(true);
        listener.start();
    }

    public void connect(MessageType registerType, Consumer<AuctionMessage> messageHandler) throws IOException {
        this.messageHandler = messageHandler;
        this.socket = new Socket("localhost", 1234);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());

        send(new AuctionMessage(registerType));

        Thread listener = new Thread(this::listen, "AuctionSocketClient");
        listener.setDaemon(true);
        listener.start();
    }

    private void listen() {
        try {
            while (!socket.isClosed()) {
                Object obj = in.readObject();
                if (obj instanceof AuctionMessage && messageHandler != null) {
                    messageHandler.accept((AuctionMessage) obj);
                }
            }
        } catch (Exception e) {
            System.out.println("Socket client disconnected: " + e.getMessage());
        }
    }

    public synchronized void send(AuctionMessage message) {
        if (out == null || message == null) return;
        try {
            out.writeObject(message);
            out.flush();
            out.reset();
        } catch (IOException e) {
            System.out.println("Cannot send socket message: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.out.println("Cannot close socket: " + e.getMessage());
        }
    }
}
