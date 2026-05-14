package com.auction.network;

import com.auction.controller.AuctionRoomController; // Import file của bạn
import com.auction.model.core.Bid;
import javafx.application.Platform;
import java.io.*;
import java.net.Socket;

public class GUIClientManager {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private AuctionRoomController controller; // "Cầu nối" tới giao diện của bạn kia
    private static Socket socketInstance;
    private static ObjectOutputStream outInstance;

    public GUIClientManager(AuctionRoomController controller) {
        this.controller = controller;
    }

    public void startConnection() {
        if (socketInstance != null && !socketInstance.isClosed()) {
            return; // Nếu đã kết nối rồi thì không làm gì thêm nữa
        }

        new Thread(() -> {
            try {
                socketInstance = new Socket("localhost", 1234);
                outInstance = new ObjectOutputStream(socketInstance.getOutputStream());
                in = new ObjectInputStream(socketInstance.getInputStream());
                while (true) {
                    try {
                        Object obj = in.readObject(); // Đợi nhận Bid từ Server
                        if (obj instanceof Bid) {
                            Bid newBid = (Bid) obj;
                            System.out.println(">>> Client đã nhận được giá mới: " + newBid.getAmount());

                            // Đẩy dữ liệu lên giao diện (JavaFX)
                            if (controller != null) {
                                javafx.application.Platform.runLater(() -> {
                                    controller.updateUIWithNewBid(newBid);
                                });
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Mất kết nối với Server!");
                        break;
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    public void sendBid(Bid bid) throws IOException {
        // Phải dùng đúng outInstance (cái đã được khởi tạo trong Thread)
        if (outInstance != null) {
            outInstance.writeObject(bid);
            outInstance.flush(); // CỰC KỲ QUAN TRỌNG: Đẩy dữ liệu đi ngay lập tức
            System.out.println(">>> Đã gửi Bid lên Server: " + bid.getAmount());
        } else {
            System.out.println(">>> Lỗi: Chưa có luồng gửi dữ liệu (outInstance null)!");
        }
    }
    public void setController(AuctionRoomController controller) {
        this.controller = controller; }
}