package com.auction;
// test xem nhiều người cùng nhảy vào đặt giá một lúc hay không
import com.auction.model.core.Auction;
import com.auction.model.core.Bid;
import com.auction.model.users.Bidder;

import java.util.Date;

public class TestMultiThread {
    public static void main(String[] args) throws Exception {

        Auction auction = new Auction(100,5000);

        System.out.println("Thời gian kết thúc dự kiến ban đầu :"+new Date(auction.getEndTime()));

        Bidder a = new Bidder(1,"A","t","LOL","1@",200);
        Bidder b = new Bidder(2,"B","x","ABC","1@",300);

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


        System.out.println("Thời gian kết thúc mới: " + new Date(auction.getEndTime()));
        System.out.println("Final price: " + auction.getCurrentPrice());
        System.out.println("Winner: " + auction.getHighestBidder().getFullName());
    }
}