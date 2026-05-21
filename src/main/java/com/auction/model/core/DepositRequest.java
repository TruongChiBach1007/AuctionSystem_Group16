package com.auction.model.core;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class DepositRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String username;
    private final long amount;
    private final LocalDateTime requestTime;
    private DepositStatus status;

    public DepositRequest(String username, long amount) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.amount = amount;
        this.requestTime = LocalDateTime.now();
        this.status = DepositStatus.PENDING;
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public long getAmount() { return amount; }
    public LocalDateTime getRequestTime() { return requestTime; }
    public DepositStatus getStatus() { return status; }
    public void setStatus(DepositStatus status) { this.status = status; }

    public String getAmountText() {
        return String.format("%,d VND", amount);
    }

    public String getRequestTimeText() {
        return requestTime.format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"));
    }

    public String getStatusText() {
        if (status == null) return "UNKNOWN";
        return switch (status) {
            case PENDING -> "Cho duyet";
            case APPROVED -> "Da duyet";
            case REJECTED -> "Tu choi";
        };
    }
}
