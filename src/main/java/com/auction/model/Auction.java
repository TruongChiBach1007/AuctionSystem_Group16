package com.auction.model;

public class Auction {
    private double currentPrice;
    private Bidder highestBidder;
    private boolean closed = false;

    private final Object lock = new Object();

    public Auction(double starPrice) {
        this.currentPrice = starPrice;
    }

    public boolean placeBid(Bid bid) {
        synchronized (lock) { // chỉ có 1 thread đợc xảy ra tại thời điểm đó
            if (closed){
                throw new IllegalStateException("Phiên đã kết thúc!");
            }
            if (bid == null || bid.getBidder() == null) {
                throw new IllegalArgumentException("Bid không hợp lệ!");
            }
            if (bid.getAmount()<=currentPrice){
                throw new IllegalArgumentException("Bid không hợp lệ! ");
            }
            currentPrice=bid.getAmount();
            highestBidder=bid.getBidder();
            System.out.println("New bid :" + currentPrice+ "by" +highestBidder.getName());

            return true;
        }
    }
    public void closeAuction() {
        synchronized (lock) {
            closed = true;
            if(highestBidder!=null) {
                System.out.println("Winner: " + highestBidder.getName());
            }
        }
    }
}

