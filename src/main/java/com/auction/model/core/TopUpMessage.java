package com.auction.model.core;

import java.io.Serializable;

public class TopUpMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private double newBalance;
    private String requestTime;
    private String status;

    // 1. Constructor 4 tham số
    public TopUpMessage(String username, double newBalance, String requestTime, String status) {
        this.username = username;
        this.newBalance = newBalance;
        this.requestTime = requestTime;
        this.status = status;
    }

    // 2. Các hàm Getters
    public String getUsername() { return username; }
    public double getNewBalance() { return newBalance; }
    public String getRequestTime() { return requestTime; }
    public String getStatus() { return status; }

    // 3. Hàm Setter
    public void setStatus(String status) { this.status = status; }

    // 4. Hàm toString() phải nằm TRONG class (trước dấu ngoặc cuối cùng)
    @Override
    public String toString() {
        return "TopUpMessage{" +
                "username='" + username + '\'' +
                ", newBalance=" + newBalance +
                ", requestTime='" + requestTime + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
} // NGOẶC NÀY MỚI LÀ NGOẶC KẾT THÚC TOÀN BỘ CLASS!