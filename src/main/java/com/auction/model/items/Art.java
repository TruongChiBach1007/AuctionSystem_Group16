package com.auction.model.items;

import java.io.Serializable;

public class Art extends Item implements Serializable {
    private static final long serialVersionUID = 1L;
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