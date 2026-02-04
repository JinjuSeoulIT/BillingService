package com.hospital.billing.dto;

public class BillStatusResponse {

    private Long billId;
    private String status;   // 예: UNPAID / PAID / CANCELED / UNKNOWN

    public BillStatusResponse() {
    }

    public BillStatusResponse(Long billId, String status) {
        this.billId = billId;
        this.status = status;
    }

    public Long getBillId() {
        return billId;
    }

    public String getStatus() {
        return status;
    }
}
