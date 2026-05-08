package com.auction.model.core;
import com.auction.model.users.Bidder;
import java.io.Serializable; //

public class Bid implements Serializable {
    private static final long serialVersionUID = 1L; // Giúp tránh lỗi version khi truyền qua Socket

    // Giữ nguyên các thuộc tính và hàm của bạn khác bên dưới...

    private Bidder bidder;
    private Double amount;

    public Bid(Bidder bidder, double amount) {
        this.bidder = bidder;
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public Bidder getBidder() {
        return bidder;
    }
}