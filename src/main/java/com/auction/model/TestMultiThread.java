package com.auction.model;
// test xem nhiều người cùng nhảy vào đặt giá một lúc hay không
public class TestMultiThread {
    public static void main(String[] args) throws Exception {

        Auction auction = new Auction(100);

        Bidder a = new Bidder(1,"A","t","ABC","1@",1.2);
        Bidder b = new Bidder(2,"B","x","ABC","1@",1.3);

        Thread t1 = new Thread(() -> {
            auction.placeBid(new Bid(a, 150));
        });

        Thread t2 = new Thread(() -> {
            auction.placeBid(new Bid(b, 201));
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("Final price: " + auction.getCurrentPrice());
        System.out.println("Winner: " + auction.getHighestBidder().getFullName());
    }
}