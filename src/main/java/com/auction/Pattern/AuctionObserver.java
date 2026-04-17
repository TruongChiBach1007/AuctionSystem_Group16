package com.auction.Pattern;
public interface AuctionObserver {
    //gửi thông báo khi có giá mới
    void BidUpdate(double newPrice,String bidderName);
    //gửi thông báo khi có winner
    void AuctionClosed(String winnerName,Double finalPrice);
}
