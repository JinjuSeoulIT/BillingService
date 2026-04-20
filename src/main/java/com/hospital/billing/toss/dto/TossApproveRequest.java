package com.hospital.billing.toss.dto;

public class TossApproveRequest {

    private String paymentKey;
    private String orderId;
    private Long amount;
    private Long billId;
    private String staffId;

    public TossApproveRequest() {
    }

    public TossApproveRequest(String paymentKey, String orderId, Long amount, Long billId, String staffId) {
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.amount = amount;
        this.billId = billId;
        this.staffId = staffId;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public void setPaymentKey(String paymentKey) {
        this.paymentKey = paymentKey;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }
}