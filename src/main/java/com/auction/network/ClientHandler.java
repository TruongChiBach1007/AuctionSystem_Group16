package com.auction.network;

import com.auction.model.core.Auction;
import com.auction.model.core.Bid;
import com.auction.model.core.RequestHistoryMessage;
import com.auction.model.core.TopUpMessage;

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
                // 1. Đợi nhận gói tin (Giữ nguyên)
                Object obj = in.readObject();

                // --- TRẠM GÁC 1: Kiểm tra xem Server có thấy gói tin không ---
                System.out.println(">>> [SERVER] Vừa nhận được đối tượng kiểu: " + obj.getClass().getSimpleName());

                if (obj instanceof Bid) {
                    processBid((Bid) obj);
                }
                else if (obj instanceof TopUpMessage) {
                    TopUpMessage topUp = (TopUpMessage) obj;

                    // --- TRẠM GÁC 2: In chi tiết nội dung nạp tiền ---
                    System.out.println(">>> [SERVER] Đang phát loa (Broadcast) nạp tiền cho: " + topUp.getUsername());

                    // Gửi cho tất cả các Client (bao gồm cả Admin và các User khác)
                    AuctionServer.broadcast(topUp);
                }
                else if (obj instanceof RequestHistoryMessage) {
                    System.out.println(">>> [SERVER] Tai khoan moi vao phong, dang dong goi gui lai lich su cu...");

                    // Cậu check xem danh sách lưu lịch sử đặt giá trên Server tên là gì (Ví dụ: auction.getBids())
                    // Vòng lặp này sẽ tự động bắn trả lại từng lượt đặt giá cũ về riêng cho máy vừa kết nối
                    if (auction != null && auction.getBidHistory() != null) {
                        for (Bid pastBid : auction.getBidHistory()) {
                            this.out.writeObject(pastBid); // Dùng đúng tên biến OutStream của ClientHandler
                            this.out.flush();
                        }
                    }
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