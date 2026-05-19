package com.auction.dao;

import com.auction.model.items.Item;
import com.auction.model.items.ItemStatus;
import com.auction.utils.DatabaseConnection;
import java.util.ArrayList;
import java.util.List;

public class ItemDAOImpl implements IItemDAO {
    private List<Item> itemTable;

    public ItemDAOImpl() {
        // Kết nối thẳng tới kho chứa hàng chung của hệ thống
        this.itemTable = DatabaseConnection.getInstance().getItemTable();
    }

    @Override
    public boolean addItem(Item item) {
        itemTable.add(item);
        return false;
    }

    @Override
    public List<Item> getItemsByStatus(ItemStatus status) {
        List<Item> result = new ArrayList<>();
        // Duyệt qua toàn bộ kho hàng, món nào trùng khớp trạng thái thì nhặt ra
        for (Item item : itemTable) {
            if (item.getStatus() == status) {
                result.add(item);
            }
        }
        return result;
    }

    @Override
    public void updateItemStatus(Item item, ItemStatus newStatus) {
        if (item != null) {
            item.setStatus(newStatus); // Thay đổi trạng thái (Ví dụ từ PENDING sang APPROVED)
        }
    }
}