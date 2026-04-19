package com.auction.model;

public class Electronics extends Item {
    private int warrantyMonths;

    // Sửa dòng này: Thêm tham số thứ 5 là currentPrice vào constructor
    public Electronics(String id, String name, String description, double startingPrice, double currentPrice) {
        // Truyền đủ 5 cái vào super
        super(id, name, description, startingPrice, currentPrice);
        this.warrantyMonths = warrantyMonths;
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    public void setWarrantyMonths(int warrantyMonths) {
        this.warrantyMonths = warrantyMonths;
    }

    @Override
    public String getDetailedInfo() {
        return "Danh mục: Đồ điện tử. Bảo hành: " + warrantyMonths + " tháng.";
    }
}
