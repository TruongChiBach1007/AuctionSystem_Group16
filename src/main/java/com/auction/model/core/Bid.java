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
    // Sửa lại hàm này trong file Bid.java của Minh nhé
    public String getBidderName() {
        // 1. Nếu có đối tượng Bidder thì lấy FullName
        if (this.bidder != null) {
            return this.bidder.getFullName();
        }

        // 2. Nếu không có Bidder (dùng tên thủ công) thì lấy manualBidderName
        // Nếu manualBidderName cũng null nốt thì mới hiện "N/A" (Không xác định)
        return (this.manualBidderName != null) ? this.manualBidderName : "N/A";
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