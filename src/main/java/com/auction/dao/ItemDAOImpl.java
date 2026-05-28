package com.auction.dao;

import com.auction.model.items.Item;
import com.auction.model.items.ItemStatus;
import com.auction.utils.DatabaseConnection;

import java.util.ArrayList;
import java.util.List;

public class ItemDAOImpl implements IItemDAO {
    private final List<Item> itemTable;

    public ItemDAOImpl() {
        this.itemTable = DatabaseConnection.getInstance().getItemTable();
    }

    @Override
    public boolean addItem(Item item) {
        if (item == null) {
            return false;
        }
        if (item.getId() != null) {
            for (int i = 0; i < itemTable.size(); i++) {
                if (item.getId().equals(itemTable.get(i).getId())) {
                    itemTable.set(i, item);
                    DatabaseConnection.getInstance().save();
                    return true;
                }
            }
        }
        itemTable.add(item);
        DatabaseConnection.getInstance().save();
        return true;
    }

    @Override
    public List<Item> getItemsByStatus(ItemStatus status) {
        List<Item> result = new ArrayList<>();
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
            item.setStatus(newStatus);
            DatabaseConnection.getInstance().save();
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
                DatabaseConnection.getInstance().save();
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
        boolean removed = itemTable.removeIf(item -> id.equals(item.getId()));
        if (removed) {
            DatabaseConnection.getInstance().save();
        }
        return removed;
    }
}
