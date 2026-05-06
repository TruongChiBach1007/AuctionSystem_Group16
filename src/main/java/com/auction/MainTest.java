package com.auction; // Nên để trong package để dễ chạy

import com.auction.model.core.Auction;
import com.auction.model.core.Bid;
import com.auction.model.users.Bidder;
import com.auction.pattern.AuctionMonitor;

public class MainTest {
    public static void main(String[] args) {
        // 1. Khởi tạo đấu giá: Giá gốc 10.0, thời gian 1 phút
        Auction auction = new Auction(10.0, 60000);

        // 2. Gắn Monitor
        AuctionMonitor monitor = new AuctionMonitor();
        auction.addObserver(monitor);

        // 3. Tạo người chơi
        Bidder b1 = new Bidder(1, "bach123", "pass", "Trương Chí Bách", "bach@mail.com", 5000.0);

        System.out.println("--- BẮT ĐẦU TEST ---");
        // Đặt giá: Tự động in ra ">>> [THÔNG BÁO]..." nhờ notifyUpdate() bạn vừa thêm
        auction.placeBid(new Bid(b1, 1200.0));
    }
}