package com.auction.dao;

import com.auction.model.items.Item;
import com.auction.model.items.ItemStatus;
import java.util.List;

public interface IItemDAO {
    // 1. Dùng hàm này để đăng sản phẩm mới
    boolean addItem(Item item);

    // 2. Hàm lọc sản phẩm theo trạng thái (Ví dụ: Tìm tất cả hàng PENDING cho Admin, hoặc APPROVED cho Bidder)
    List<Item> getItemsByStatus(ItemStatus status);

    // 3. Admin dùng hàm này để duyệt sản phẩm (Đổi status của một Item dựa vào ID hoặc chính object đó)
    void updateItemStatus(Item item, ItemStatus newStatus);
    List<Item> getItemsBySeller(String username);
    // Hàm cập nhật thông tin sản phẩm
    boolean updateItem(Item item);

    // Hàm xóa sản phẩm theo ID
    boolean deleteItem(String id);
}