package com.auction.network;

import java.io.Serializable;

public enum MessageType implements Serializable {
    REGISTER_ADMIN,
    REGISTER_BIDDER,
    REGISTER_SELLER,
    ITEM_REQUEST,
    ITEM_PENDING,
    APPROVE_ITEM,
    REJECT_ITEM,
    ITEM_APPROVED,
    ITEM_REJECTED,
    DEPOSIT_REQUEST,
    DEPOSIT_PENDING,
    APPROVE_DEPOSIT,
    REJECT_DEPOSIT,
    DEPOSIT_APPROVED,
    DEPOSIT_REJECTED,
    ERROR
}
