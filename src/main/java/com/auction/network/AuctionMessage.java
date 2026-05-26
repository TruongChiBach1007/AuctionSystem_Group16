package com.auction.network;

import com.auction.model.core.Bid;
import com.auction.model.core.DepositRequest;
import com.auction.model.items.Item;

import java.io.Serializable;
import java.util.List;

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

    // [FIX 1] Danh sách lịch sử bid để gửi cho người mới vào
    private List<Bid> bidList;

    // [FIX 3] Thông tin người thắng khi kết thúc phiên
    private String winnerName;
    private double winnerAmount;

    // [SYNC TIMER] Số giây còn lại thực tế trên server
    private int remainingSeconds;

    public AuctionMessage(MessageType type) {
        this.type = type;
    }

    public AuctionMessage(MessageType type, Bid bid) {
        this.type = type;
        this.bid = bid;
        this.bidId = bid != null ? bid.getId() : null;
    }

    // Constructor BID kèm remainingSeconds để client đồng bộ đồng hồ sau mỗi lần có bid
    public AuctionMessage(MessageType type, Bid bid, int remainingSeconds) {
        this.type = type;
        this.bid = bid;
        this.bidId = bid != null ? bid.getId() : null;
        this.remainingSeconds = remainingSeconds;
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

    // [FIX 1] Constructor gửi lịch sử bid + giá hiện tại + thời gian còn lại cho người mới vào
    public AuctionMessage(MessageType type, List<Bid> bidList, double currentHighestBid, int remainingSeconds) {
        this.type = type;
        this.bidList = bidList;
        this.winnerAmount = currentHighestBid;
        this.remainingSeconds = remainingSeconds;
    }

    // [FIX 3] Constructor gửi kết thúc phiên
    public AuctionMessage(MessageType type, String winnerName, double winnerAmount, boolean isAuctionEnd) {
        this.type = type;
        this.winnerName = winnerName;
        this.winnerAmount = winnerAmount;
    }

    public MessageType getType() { return type; }
    public Bid getBid() { return bid; }
    public String getBidId() { return bidId; }
    public Item getItem() { return item; }
    public String getItemId() { return itemId; }
    public String getMessage() { return message; }
    public DepositRequest getDepositRequest() { return depositRequest; }
    public String getDepositId() { return depositId; }
    public List<Bid> getBidList() { return bidList; }
    public String getWinnerName() { return winnerName; }
    public double getWinnerAmount() { return winnerAmount; }
    public int getRemainingSeconds() { return remainingSeconds; }
}
