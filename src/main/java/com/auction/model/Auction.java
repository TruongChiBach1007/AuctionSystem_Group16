package com.auction.model;
import com.auction.autobid.AutoBid;
import com.auction.exceptions.InvalidBidException;

import java.util.ArrayList;
import java.util.List;

public class Auction {
    private double currentPrice;
    private Bidder highestBidder;
    private boolean closed = false; // trạng thái phiên đang mở

    private final Object lock = new Object(); //synchronized

    private List<AutoBid> autoBids = new ArrayList<>();

    public Auction(double starPrice) {
        this.currentPrice = starPrice;
    }
    public void registerAutoBid(AutoBid autoBid) {
        synchronized (lock) {
            autoBids.add(autoBid);
        }
    }
    //
    public boolean placeBid(Bid bid) {
        synchronized (lock) { // chỉ có 1 thread đợc xảy ra tại thời điểm đó
            if (closed) {
                throw new IllegalStateException("Phiên đã kết thúc!");
            }
            if (bid == null || bid.getBidder() == null) {
                throw new IllegalArgumentException("Bid không hợp lệ!");
            }
            if (bid.getAmount() <= currentPrice) {
                throw new IllegalArgumentException("Bid không hợp lệ! Vui lòng đặt giá cao hơn ");
            }
            currentPrice = bid.getAmount();
            highestBidder = bid.getBidder();
            System.out.println("New bid :" + currentPrice + "by" + highestBidder.getName());

            return true;
        }
    }
    private void processAutoBids() {
        boolean updated;

        do {
            updated = false;

            for (AutoBid auto : autoBids) {

                if (auto.getBidder() == highestBidder) continue;

                double nextBid = currentPrice + auto.getIncrement();

                if (nextBid <= auto.getMaxBid()) {

                    currentPrice = nextBid;
                    highestBidder = auto.getBidder();

                    System.out.println("AutoBid: " + currentPrice + " by " + highestBidder.getName());

                    updated = true;
                }
            }

        } while (updated);
    }

    public void closeAuction() {
        synchronized (lock) {
            closed = true;
            if (highestBidder != null) {
                System.out.println("Winner: " + highestBidder.getName());
            }
        }
    }

    public Bidder getHighestBidder() {
        return highestBidder;
    }

    public Double getCurrentPrice() {
        return currentPrice;
    }
    public boolean isClosed() {
        return closed;
    }
}
