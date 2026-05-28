package com.auction.model.core;

import java.io.Serializable;

public class AuctionSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String itemId;
    private final String productName;
    private final double currentPrice;
    private final String leaderName;
    private final int remainingSeconds;
    private final String status;

    public AuctionSummary(String itemId, String productName, double currentPrice,
                          String leaderName, int remainingSeconds, String status) {
        this.itemId = itemId;
        this.productName = productName;
        this.currentPrice = currentPrice;
        this.leaderName = leaderName;
        this.remainingSeconds = remainingSeconds;
        this.status = status;
    }

    public String getItemId() {
        return itemId;
    }

    public String getProductName() {
        return productName;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public String getCurrentPriceText() {
        return String.format("%,.0f VNĐ", currentPrice);
    }

    public String getLeaderName() {
        return leaderName == null || leaderName.isBlank() ? "Chưa có" : leaderName;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public String getRemainingTimeText() {
        int min = Math.max(0, remainingSeconds) / 60;
        int sec = Math.max(0, remainingSeconds) % 60;
        return String.format("%02d:%02d", min, sec);
    }

    public String getStatus() {
        return status;
    }
}
