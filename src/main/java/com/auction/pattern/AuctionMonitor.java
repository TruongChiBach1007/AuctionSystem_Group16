//lớp sẽ in kết quả ra màn hình mỗi khi có ai đó đặt giá.
package com.auction.pattern;

import com.auction.pattern.AuctionObserver;

public class AuctionMonitor implements AuctionObserver {

    public void BidUpdate(double newPrice, String bidderName) {
        System.out.println(">>> [THÔNG BÁO] Giá mới: " + newPrice + " bởi " + bidderName);
    }

    public void AuctionClosed(String winnerName, Double finalPrice) {
        System.out.println(">>> [KẾT THÚC] Người thắng: " + winnerName + " với giá: " + finalPrice);
    }
}