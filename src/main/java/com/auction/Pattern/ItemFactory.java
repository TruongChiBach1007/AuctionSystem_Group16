package com.auction.Pattern;
import com.auction.model.*;

public class ItemFactory {
    // Thêm tham số 'extraValue' kiểu Object để linh hoạt cho mọi loại hàng
    public static Item createItem(String type, String id, String name, String description, double startingPrice, Object extraValue) {
        switch (type.toLowerCase()) {
            case "art":
                // Giả sử Art không cần tham số riêng,bỏ qua extraValue
                return new Art(id, name, description, startingPrice, startingPrice);

            case "electronics":
                return new Electronics(id, name, description, startingPrice, startingPrice);

            case "vehicle":
                // Ép kiểu extraValue về double để truyền vào engineCapacity của Vehicle
                double capacity = (double) extraValue;
                return new Vehicle(id, name, description, startingPrice, startingPrice, capacity);

            default:
                throw new IllegalArgumentException("Loại hàng không tồn tại!");
        }
    }
}