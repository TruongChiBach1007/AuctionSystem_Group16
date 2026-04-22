package com.auction;
import com.auction.service.AutoBid;
import com.auction.model.core.Auction;
import com.auction.model.core.Bid;
import com.auction.model.users.Bidder;

public class TestAutoBid {
    public static void main(String[] args) {
        //tạo phòng đấu gia
        Auction auction = new Auction(100,10000000);
        //tạo user
        Bidder userA = new Bidder(1, "Người đặt tay ","a","bach","2@",160);
        Bidder AutoB = new Bidder(2, "Người dùng Auto","b","robot","3@",500);
        //đăng ký robot cho B

        AutoBid setting = new AutoBid(AutoB,500,10);
        auction.registerAutoBid(setting);

        System.out.println("--- BẮT ĐẦU PHIÊN ĐẤU GIÁ ---");
        System.out.println("Giá khởi điểm: " + auction.getCurrentPrice());

        // 4. KÍCH HOẠT: User A đặt giá tay 150 để Robot của B nhảy vào "đớp"
        System.out.println("[Action] Người A đặt giá 150...");
        auction.placeBid(new Bid(userA, 150));

        // 5. Kết quả sau khi Robot tự động phản hồi
        System.out.println("--- KẾT QUẢ HIỆN TẠI ---");
        System.out.println("Giá hiện tại: " + auction.getCurrentPrice());
        System.out.println("Người giữ giá cao nhất: " + auction.getHighestBidder().getFullName());

        // 6. Đóng phiên
        auction.closeAuction();
    }
}
