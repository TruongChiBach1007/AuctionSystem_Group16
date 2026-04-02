package com.auction.model;

import java.io.DataOutputStream;
import java.io.Serializable;

public abstract class Item implements Serializable {
    private String id;
    private String name;
    private String description;
    private double startingPrice;
    private double currentPrice;

    public Item (String id, String name, String description, double startingPrice, double currentPrice) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getStartingPrice() { return startingPrice; }
    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

    public abstract String getDetailedInfo();

    // Hàm hiển thị cơ bản (Polymorphism - Đa hình)
    @Override
    public String toString() {
        return "Sản phẩm: " + name + " | Giá hiện tại: " + currentPrice;
    }
}

