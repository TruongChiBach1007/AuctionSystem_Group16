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
        if (item == null) {
            return false;
        }
        itemTable.add(item);
        return true;
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
    @Override
    public List<Item> getItemsBySeller(String username) {
        List<Item> result = new ArrayList<>();
        for (Item item : itemTable) {
            if (item.getSellerUsername() != null && item.getSellerUsername().equals(username)) {
                result.add(item);
            }
        }
        return result;
    }

    @Override
    public boolean updateItem(Item item) {
        if (item == null || item.getId() == null) {
            return false;
        }
        for (int i = 0; i < itemTable.size(); i++) {
            if (item.getId().equals(itemTable.get(i).getId())) {
                itemTable.set(i, item);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean deleteItem(String id) {
        if (id == null) {
            return false;
        }
        return itemTable.removeIf(item -> id.equals(item.getId()));
    }
}
