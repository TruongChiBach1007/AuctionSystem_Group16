package com.auction.model.core;

import com.auction.model.users.Bidder;

public class Bid {
    private Bidder bidder;
    private Double amount;

    public Bid(Bidder bidder, double amount) {
        this.bidder = bidder;
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public Bidder getBidder() {
        return bidder;
    }
}