package com.auction.service;
import com.auction.model.users.Bidder;

import java.io.Serializable;

public class AutoBid implements Serializable {
    private static final long serialVersionUID = 1L;

    private Bidder bidder;
    private double maxBid;
    private double increment;

    public AutoBid(Bidder bidder, double maxBid, double increment) {
        this.bidder = bidder;
        this.maxBid = maxBid;
        this.increment = increment;
    }

    public Bidder getBidder() {
        return bidder;
    }
    public double getMaxBid() {
        return maxBid;
    }

    public double getIncrement() {
        return increment;
    }
}
