package com.auction.network;


import com.auction.model.core.Bid;
import com.auction.model.users.Bidder;

import java.io.*;
import java.net.Socket;

public class AuctionClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("192.168.100.153", 1234);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            System.out.println("Connected to Auction Server!");

            // Luồng phụ để luôn lắng nghe cập nhật từ Server
            new Thread(() -> {
                try {
                    while (true) {
                        Object response = in.readObject();
                        System.out.println("\n[SERVER UPDATE]: " + response);
                    }
                } catch (Exception e) {
                    System.out.println("Connection closed.");
                }
            }).start();

            // Giả lập gửi một lệnh đặt giá (Cần tạo đối tượng Bid hợp lệ)
            // Thay vì để comment, hãy mở nó ra như thế này:
            // Trong file AuctionClient.java
// 1. Tạo một Bidder giả
            Bidder mockBidder = new Bidder(1, "nguoi_dung_test", "pass", "Nguyen Van A", "test@mail.com", 5000.0);

// 2. Gửi Bid với Bidder này thay vì để null
            Bid testBid = new Bid(mockBidder, 2000.0);
            out.writeObject(testBid);
            out.flush();
            // out.flush();

            // Giữ chương trình chạy
            Thread.sleep(100000);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
