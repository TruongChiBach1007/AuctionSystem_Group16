package com.auction.model.items;

public class ProductCard {
    private String name;
    private String category;
    private double currentPrice;
    private double startingPrice;
    private String status;
    private String timeLeft;
    private String description;
    private String cardColor; // màu nền card

    public ProductCard(String name, String category, double currentPrice,
                       double startingPrice, String status, String timeLeft,
                       String description, String cardColor) {
        this.name = name;
        this.category = category;
        this.currentPrice = currentPrice;
        this.startingPrice = startingPrice;
        this.status = status;
        this.timeLeft = timeLeft;
        this.description = description;
        this.cardColor = cardColor;
    }

    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getCurrentPrice() { return currentPrice; }
    public double getStartingPrice() { return startingPrice; }
    public String getStatus() { return status; }
    public String getTimeLeft() { return timeLeft; }
    public String getDescription() { return description; }
    public String getCardColor() { return cardColor; }
}