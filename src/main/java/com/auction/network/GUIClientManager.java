package com.auction.network;

import com.auction.controller.AdminDashboardController;
import com.auction.controller.AuctionRoomController;
import com.auction.controller.BidderDashboardController;
import com.auction.model.core.Bid;
import com.auction.model.core.TopUpMessage;
import javafx.application.Platform;
import java.io.*;
import java.net.Socket;

public class GUIClientManager {
    // 1. Các biến dùng chung (Static)
    private static GUIClientManager instance;
    private static Socket socket;
    private static ObjectOutputStream outInstance;
    private static ObjectInputStream inInstance;
    private static Object controller; // Phải là static để các màn hình dùng chung

    // 2. Singleton Pattern - CHỖ NÀY ĐÃ FIX LỖI STATIC CỦA CẬU
    public static GUIClientManager getInstance() {
        if (instance == null) {
            instance = new GUIClientManager();
        }
        return instance;
    }

    // Constructor mặc định - Cần public để các Controller gọi được
    public GUIClientManager() {
        // Gán instance bằng chính đối tượng vừa được tạo
        instance = this;
    }

    // 3. Kết nối Server
    public void startConnection(String ip, int port) {
        try {
            if (socket != null && !socket.isClosed()) return;

            socket = new Socket(ip, port);
            outInstance = new ObjectOutputStream(socket.getOutputStream());
            inInstance = new ObjectInputStream(socket.getInputStream());

            System.out.println(">>> [NETWORK] Da ket noi toi Server!");
            startListening();
        } catch (IOException e) {
            System.err.println(">>> [NETWORK] Loi ket noi: " + e.getMessage());
        }
    }

    // 4. Luồng nhận tin từ Server
    private void startListening() {
        new Thread(() -> {
            try {
                while (true) {
                    Object obj = inInstance.readObject();

                    if (obj instanceof TopUpMessage) {
                        TopUpMessage topUp = (TopUpMessage) obj;
                        System.out.println(">>> [NETWORK] Nhan tin tu: " + topUp.getUsername() + " | Trang thai: " + topUp.getStatus());

                        if (controller instanceof AdminDashboardController) {
                            // CASE 1: Máy Admin chỉ nhận hiển thị lên bảng khi trạng thái là "Chờ duyệt"
                            // Điều này ngăn bảng bị nhân đôi dòng khi gói tin "Hoàn thành" phát loa quay lại
                            if ("Chờ duyệt".equals(topUp.getStatus())) {
                                Platform.runLater(() -> {
                                    ((AdminDashboardController) controller).receiveDepositRequest(topUp);
                                });
                            }
                        } else if (controller instanceof AuctionRoomController) {
                            // CASE 2: Máy User chỉ thực hiện cộng số dư khi trạng thái đã là "Hoàn thành"
                            if ("Hoàn thành".equals(topUp.getStatus())) {
                                AuctionRoomController room = (AuctionRoomController) controller;
                                Platform.runLater(() -> {
                                    if (room.currentUser.getUsername().equals(topUp.getUsername())) {
                                        // Tính toán số dư tăng trưởng: Số dư hiện tại + Số tiền nạp mới
                                        double updatedBalance = room.currentUser.getBalance() + topUp.getNewBalance();

                                        // Cập nhật dữ liệu và hiển thị nhãn tiền mới lên UI
                                        room.currentUser.setBalance(updatedBalance);
                                        room.balanceLabel.setText(String.format("%.0f", updatedBalance));

                                        System.out.println(">>> [NETWORK] Tai khoan cua ban da duoc cong: +" + topUp.getNewBalance());
                                    }
                                });
                            }
                        }else if (controller instanceof BidderDashboardController) {
                            // CHỐT CHẶN BẮT BUỘC: Chỉ khi Admin đã bấm duyệt thành "Hoàn thành" thì Trang chủ mới được cộng tiền!
                            if ("Hoàn thành".equals(topUp.getStatus())) {
                                BidderDashboardController dashboard = (BidderDashboardController) controller;
                                javafx.application.Platform.runLater(() -> {
                                    dashboard.handleNetworkTopUpSuccess(topUp.getNewBalance());
                                });
                            }
                        }
                    }
                    else if (obj instanceof Bid) {
                        Bid bid = (Bid) obj;
                        System.out.println(">>> [NETWORK] Client da nhan duoc goi tin Bid: " + bid.getAmount());

                        // Nếu màn hình đang hiển thị là phòng đấu giá thì ép giao diện vẽ lại
                        if (controller instanceof AuctionRoomController) {
                            AuctionRoomController room = (AuctionRoomController) controller;

                            // Bọc vào Platform.runLater để tránh xung đột đơ luồng UI
                            javafx.application.Platform.runLater(() -> {
                                room.handleIncomingBid(bid);
                            });
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println(">>> [NETWORK] Ngat ket noi Server!");
            }
        }).start();

    }

    // 5. Các hàm hỗ trợ
    public void setController(Object ctrl) {
        controller = ctrl;
        System.out.println(">>> [NETWORK] Dang phục vụ: " + ctrl.getClass().getSimpleName());
    }

    public void sendTopUp(TopUpMessage msg) throws IOException {
        if (outInstance != null) {
            outInstance.writeObject(msg);
            outInstance.flush();
        } else {
            System.err.println(">>> [NETWORK] Loi: outInstance dang NULL!");
        }
    }
    public void sendBid(Bid bid) throws IOException {
        if (outInstance != null) {
            outInstance.writeObject(bid);
            outInstance.flush();
            System.out.println(">>> [NETWORK] Da gui gia dau moi: " + bid.getAmount());
        } else {
            System.err.println(">>> [NETWORK] Loi: outInstance dang bi NULL, khong the gui Bid!");
            // Thử kết nối lại nếu bị mất vòi nước
            startConnection("localhost", 1234);
        }
    }
    public void sendRequestHistory(com.auction.model.core.RequestHistoryMessage msg) {
        try {
            if (outInstance != null) {
                outInstance.writeObject(msg);
                outInstance.flush();
                System.out.println(">>> [NETWORK] Da gui don xin tai lai lich su qua khu len Server.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
// ========================================================
} // NGOẶC CUỐI CÙNG KẾT THÚC CLASS