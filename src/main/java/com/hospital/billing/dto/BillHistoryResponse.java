package com.hospital.billing.dto;

import java.time.LocalDateTime;

public class BillHistoryResponse {

    private Long historyId;            // 이력 ID (나중에 History 테이블 생기면 연결)
    private Long billId;               // 어떤 Bill의 이력인지
    private String action;             // 예: CREATED / CONFIRMED / CANCELED / UPDATED
    private String message;            // 상세 설명(선택)
    private LocalDateTime createdAt;   // 이력이 기록된 시간

    public BillHistoryResponse() {
    }

    public BillHistoryResponse(Long historyId, Long billId, String action, String message, LocalDateTime createdAt) {
        this.historyId = historyId;
        this.billId = billId;
        this.action = action;
        this.message = message;
        this.createdAt = createdAt;
    }

    public Long getHistoryId() {
        return historyId;
    }

    public Long getBillId() {
        return billId;
    }

    public String getAction() {
        return action;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
