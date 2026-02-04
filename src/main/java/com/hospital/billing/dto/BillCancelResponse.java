package com.hospital.billing.dto;

public class BillCancelResponse {

    private Long billId;
    private String status;

    public BillCancelResponse(Long billId, String status) {
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
