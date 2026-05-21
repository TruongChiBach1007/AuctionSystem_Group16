package com.auction.network;

import com.auction.model.core.Bid;
import com.auction.model.items.Item;

import java.io.Serializable;

public class AuctionMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private MessageType type;
    private Bid bid;
    private String bidId;
    private Item item;
    private String itemId;
    private String message;

    public AuctionMessage(MessageType type) {
        this.type = type;
    }

    public AuctionMessage(MessageType type, Bid bid) {
        this.type = type;
        this.bid = bid;
        this.bidId = bid != null ? bid.getId() : null;
    }

    public AuctionMessage(MessageType type, String bidId) {
        this.type = type;
        this.bidId = bidId;
    }

    public AuctionMessage(MessageType type, Item item) {
        this.type = type;
        this.item = item;
        this.itemId = item != null ? item.getId() : null;
    }

    public AuctionMessage(MessageType type, String id, boolean itemMessage) {
        this.type = type;
        if (itemMessage) {
            this.itemId = id;
        } else {
            this.bidId = id;
        }
    }

    public AuctionMessage(MessageType type, String bidId, String message) {
        this.type = type;
        this.bidId = bidId;
        this.message = message;
    }

    public MessageType getType() {
        return type;
    }

    public Bid getBid() {
        return bid;
    }

    public String getBidId() {
        return bidId;
    }

    public Item getItem() {
        return item;
    }

    public String getItemId() {
        return itemId;
    }

    public String getMessage() {
        return message;
    }
}
