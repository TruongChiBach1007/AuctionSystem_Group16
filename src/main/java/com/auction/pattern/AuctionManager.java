package com.auction.pattern;

import com.auction.model.core.Auction;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * AuctionManager - Quản lý tập trung toàn bộ các phiên đấu giá (Singleton Pattern)
 */
public class AuctionManager {
    // 1. Instance duy nhất của Manager
    private static AuctionManager instance;

    // 2. Sử dụng ConcurrentHashMap để an toàn khi nhiều Thread (Socket/GUI) truy cập cùng lúc
    // Key: String (ID của Item), Value: Auction (Đối tượng phiên đấu giá)
    private Map<String, Auction> auctions = new ConcurrentHashMap<>();

    // 3. Private Constructor để ngăn việc tạo đối tượng từ bên ngoài
    private AuctionManager() {}

    // 4. Hàm lấy Instance duy nhất
    public static synchronized AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    // 5. Thêm một phiên đấu giá mới vào hệ thống
    public void addAuction(String itemId, Auction auction) {
        if (itemId != null && auction != null) {
            auctions.put(itemId, auction);
            System.out.println("[Manager] Đã thêm phiên đấu giá cho sản phẩm: " + itemId);
        }
    }

    // 6. Lấy một phiên đấu giá dựa trên ID
    public Auction getAuction(String itemId) {
        return auctions.get(itemId);
    }

    // 7. Lấy toàn bộ danh sách để hiển thị lên GUI (Dành cho Member 1)
    public Map<String, Auction> getAllAuctions() {
        return auctions;
    }

    // 8. Xóa phiên đấu giá khi kết thúc (nếu cần)
    public void removeAuction(String itemId) {
        auctions.remove(itemId);
    }
}