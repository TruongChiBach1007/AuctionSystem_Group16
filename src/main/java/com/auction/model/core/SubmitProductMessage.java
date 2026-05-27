package com.auction.model.core;
import java.io.Serializable;
import com.auction.model.items.ProductCard;

public class SubmitProductMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private ProductCard productCard;

    public SubmitProductMessage(ProductCard productCard) {
        this.productCard = productCard;
    }

    public ProductCard getProductCard() { return productCard; }
}