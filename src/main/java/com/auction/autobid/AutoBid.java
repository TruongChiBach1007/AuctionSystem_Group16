package com.auction.autobid;
import com.auction.model.Bidder;

public class AutoBid {
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