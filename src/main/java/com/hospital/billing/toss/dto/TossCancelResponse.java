package com.hospital.billing.toss.dto;

public class TossCancelResponse {

    private String paymentKey;
    private String orderId;
    private String status;
    private Long totalAmount;
    private Long canceledAmount;
    private String method;

    public TossCancelResponse() {
    }

    public TossCancelResponse(String paymentKey,
                              String orderId,
                              String status,
                              Long totalAmount,
                              Long canceledAmount,
                              String method) {
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.canceledAmount = canceledAmount;
        this.method = method;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getCanceledAmount() {
        return canceledAmount;
    }

    public void setCanceledAmount(Long canceledAmount) {
        this.canceledAmount = canceledAmount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}