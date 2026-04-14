package com.hospital.billing.dto;

import java.time.LocalDateTime;

public class BillHistoryResponse {

    private LocalDateTime occurredAt;
    private String historyType;
    private String title;
    private String description;
    private int amount;

    public BillHistoryResponse() {
    }

    public BillHistoryResponse(LocalDateTime occurredAt,
                               String historyType,
                               String title,
                               String description,
                               int amount) {
        this.occurredAt = occurredAt;
        this.historyType = historyType;
        this.title = title;
        this.description = description;
        this.amount = amount;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getHistoryType() {
        return historyType;
    }

    public void setHistoryType(String historyType) {
        this.historyType = historyType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}