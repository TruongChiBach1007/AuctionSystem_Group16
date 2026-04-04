package com.auction.model;

import com.auction.autobid.AutoBid;
import com.auction.exceptions.InvalidBidException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class Auction {
    private double currentPrice;
    private Bidder highestBidder;
    private boolean closed = false; // trạng thái phiên đang mở

    // --- BỔ SUNG BIẾN THỜI GIAN ---
    private long endTime; // Thời gian kết thúc (timestamp)
    private final long SNIPING_WINDOW = 10000; // 10 giây cuối
    private final long EXTEND_TIME = 60000;    // Gia hạn thêm 60 giây



    private final Object lock = new Object(); //synchronized
    private List<AutoBid> autoBids = new ArrayList<>();

    public Auction(double starPrice,long durationMillis) {
        this.currentPrice = starPrice;
        this.endTime = System.currentTimeMillis() + durationMillis;
    }
    public void registerAutoBid(AutoBid autoBid) {//autoBid lấy thuộc tinh AutoBid
        synchronized (lock) {
            autoBids.add(autoBid);
        }
    }
    //
    public boolean placeBid(Bid bid) {
        synchronized (lock) {// chỉ có 1 thread đợc xảy ra tại thời điểm đó
            long currentTime = System.currentTimeMillis();
            if (closed|| currentTime> endTime) {
                this.closed=true;
                throw new IllegalStateException("Phiên đã kết thúc!");
            }
            if (bid == null || bid.getBidder() == null) {
                throw new IllegalArgumentException("Bid không hợp lệ!");
            }
            // Lấy thông tin người đặt và số tiền họ muốn đặt
            Bidder bidder = (Bidder) bid.getBidder(); //?
            double bidAmount = bid.getAmount();
            //  KIỂM TRA SỐ DƯ
            if (bidder.getBalance() < bidAmount) {
                System.out.println("Từ chối Bid: " + bidder.getFullName() + " không đủ số dư (Hiện có: " + bidder.getBalance() + ")");
                return false; // Trả về false để báo hiệu đặt giá không thành công
            }
            if (bidAmount <= currentPrice) {
                throw new IllegalArgumentException("Bid không hợp lệ! Vui lòng đặt giá cao hơn ");
            }
            //LOGIC ANTI-SNIPING
            //NẾU ĐẶT GIÁ THÀNH CÔNG TRONG 10S CUỐI THÌ GIA HẠN THÊM 60S
            if(endTime - currentTime <=SNIPING_WINDOW){
                this.endTime+= EXTEND_TIME;
                System.out.println("Phiên gia hạn thêm 1 phút ! " +new Date(endTime));
            }

            currentPrice = bidAmount;
            highestBidder = bidder;
            System.out.println("New bid :" + currentPrice + "by" + highestBidder.getFullName());

            processAutoBids();
            return true;
        }
    }
    private void processAutoBids() {
        boolean updated;

        do {
            updated = false;
            for (AutoBid auto : autoBids) {

                if (auto.getBidder().equals(highestBidder)) continue;
                // Kiểm tra nếu người giữ giá cao nhất hiện tại chính là người cài AutoBid này thì bỏ qua
                double nextBid = currentPrice + auto.getIncrement();

                if (nextBid <= auto.getMaxBid() && nextBid <= auto.getBidder().getBalance()) {
                    currentPrice = nextBid;
                    highestBidder = auto.getBidder();
                    System.out.println("AutoBid: " + currentPrice + " by " + highestBidder.getFullName());
                    updated = true;
                    }
                }
        }
        while (updated) ;
    }

    public void closeAuction() {
        synchronized (lock) {
            closed = true;
            System.out.println("PHIÊN ĐẤU GIÁ ĐÃ KẾT THÚC");
            if (highestBidder != null) {
                System.out.println("Winner: " + highestBidder.getFullName());
            }
        }
    }
    public long getEndTime() { return endTime; }
    public Bidder getHighestBidder() {return highestBidder;}
    public Double getCurrentPrice() {return currentPrice;}
    public boolean isClosed() {return closed;}
}
