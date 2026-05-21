package com.auction.network;

import com.auction.model.core.Bid;
import com.auction.model.core.DepositRequest;
import com.auction.model.items.Item;

import java.io.Serializable;

public class AuctionMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private MessageType type;
    private Bid bid;
    private String bidId;
    private Item item;
    private String itemId;
    private DepositRequest depositRequest;
    private String depositId;
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

    public AuctionMessage(MessageType type, DepositRequest depositRequest) {
        this.type = type;
        this.depositRequest = depositRequest;
        this.depositId = depositRequest != null ? depositRequest.getId() : null;
    }

    public AuctionMessage(MessageType type, String id, boolean itemMessage) {
        this.type = type;
        if (itemMessage) {
            this.itemId = id;
        } else if (type == MessageType.APPROVE_DEPOSIT || type == MessageType.REJECT_DEPOSIT) {
            this.depositId = id;
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

    public DepositRequest getDepositRequest() {
        return depositRequest;
    }

    public String getDepositId() {
        return depositId;
    }
}
