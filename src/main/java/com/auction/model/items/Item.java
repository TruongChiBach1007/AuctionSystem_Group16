package com.auction.model.items;

import java.io.Serializable;

public abstract class Item implements Serializable {
    private String id;
    private String name;
    private String description;
    private double startingPrice;
    private double currentPrice;
    private ItemStatus status;
    private String sellerName;
    private String imageUrl;
    private Object sellerUsername;

    public Item (String id, String name, String description, double startingPrice, double currentPrice) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
        this.status = ItemStatus.PENDING;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getStartingPrice() { return startingPrice; }
    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }
    public ItemStatus getStatus() { return status; }
    public void setStatus(ItemStatus status) { this.status = status; }
    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getCategory() { return getClass().getSimpleName(); }
    public String getStatusText() {
        if (status == null) return "UNKNOWN";
        return switch (status) {
            case PENDING -> "Cho duyet";
            case APPROVED -> "Da duyet";
            case REJECTED -> "Tu choi";
        };
    }

    public abstract String getDetailedInfo();

    // Hàm hiển thị cơ bản (Polymorphism - Đa hình)
    @Override
    public String toString() {
        return "Sản phẩm: " + name + " | Giá hiện tại: " + currentPrice;
    }

    public Object getSellerUsername() {
        return this.sellerUsername; // Trong đó sellerUsername là một biến kiểu String
    }
}

