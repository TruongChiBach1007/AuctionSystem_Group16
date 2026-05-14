package com.auction.network;

import com.auction.model.core.Auction;
import com.auction.model.core.Bid;
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
                // Server ngồi đợi Client gửi đối tượng sang
                Object obj = in.readObject();

                if (obj instanceof Bid) {
                    processBid((Bid) obj); // Gửi sang hàm xử lý đấu giá
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Một người chơi đã thoát.");
        } finally {
            try {
                if (socket != null) socket.close();
                // XÓA MÌNH KHỎI DANH SÁCH ĐỂ SERVER KHÔNG GỬI NHẦM NỮA
                AuctionServer.removeClient(this);
                System.out.println(">>> Đã dọn dẹp 1 kết nối thừa.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processBid(Bid bid) {
        synchronized (auction) {
            try {
                auction.placeBid(bid);
                System.out.println("New bid accepted: " + bid.getAmount());

                // DÒNG "PHÉP THUẬT" Ở ĐÂY:
                // Bảo Server gửi cái Bid này tới TẤT CẢ mọi người đang kết nối
                AuctionServer.broadcast(bid);

            } catch (Exception e) {
                sendToClient("Error: " + e.getMessage());
            }
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


    public void sendToClient(Object message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.err.println("Lỗi khi gửi dữ liệu tới client: " + e.getMessage());
        }
    }
}