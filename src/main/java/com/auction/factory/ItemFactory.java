package com.auction.factory;
import com.auction.model.*;

public class ItemFactory {
    public static Item createItem(String type, String id, String name, String description, double startPrice, double currentPrice, Object extra) {
        if (type == null) return null;
        switch (type.toLowerCase()) {
            case "Electronics":
                return new Electronics(id, name, description, startPrice, currentPrice, (int) extra);
            case "Art":
                return new Art(id, name, description, startPrice, currentPrice, (String) extra);
            case "Vehicle":
                return new Vehicle(id, name, description, startPrice, currentPrice, (double) extra);
            default:
                return null;

        }
    }
}
