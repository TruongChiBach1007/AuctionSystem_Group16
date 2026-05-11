package com.auction.model.core;

import com.auction.model.users.Bidder;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Bid implements Serializable {
    private static final long serialVersionUID = 1L;
    private Bidder bidder;
    private Double amount;
    private LocalTime time;
    private String manualBidderName;

    public Bid(Bidder bidder, double amount) {
        this.bidder = bidder;
        this.amount = amount;
        this.time = LocalTime.now();
    }
    public Bid(String manualBidderName, double amount) {
        this.manualBidderName = manualBidderName;
        this.amount = amount;
        this.time = LocalTime.now();
    }

    //hàm để hiển thị thông tin trên table
    public String getBidderName() {
        if (this.bidder != null) {
            return this.bidder.getFullName();
        }
        // Trả về tên bạn để khi bạn test (truyền null vào Bid) nó vẫn hiện chữ
        return "Trương Chí Bách";
    }
    // hàm fx lấy realtime
    public String getBidTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return time.format(formatter);
    }

    public double getAmount() {
        return amount;
    }

    public Bidder getBidder() {
        return bidder;
    }
}