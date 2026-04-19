package com.auction.network;

import com.auction.model.Auction;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

public class AuctionServer {
    private static final int PORT = 1234;
    private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private static final Auction auction = new Auction(100.0, 3600000);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println(">>> Auction Server is running on port " + PORT + "...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println(">>> New client connected: " + socket.getInetAddress());

                ClientHandler handler = new ClientHandler(socket, auction);
                clients.add(handler);

                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println(">>> Server Error: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static void broadcast(Object message) {
        for (ClientHandler client : clients) {
            client.sendToClient(message);
        }
    }


    public static void removeClient(ClientHandler client) {
        clients.remove(client);
    }
}