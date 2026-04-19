package com.auction.model;

public class Art extends Item {
    private String artist;

    public Art(String id, String name, String description, double startingPrice, double currentPrice) {
        super(id, name, description, startingPrice, currentPrice);
        this.artist = artist;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    @Override
    public String getDetailedInfo() {
        return "Tác phẩm nghệ thuật - Họa sĩ: " + artist;
    }
}