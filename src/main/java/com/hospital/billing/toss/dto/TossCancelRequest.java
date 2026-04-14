package com.hospital.billing.toss.dto;

public class TossCancelRequest {

    private String paymentKey;
    private String cancelReason;
    private Long cancelAmount;

    public TossCancelRequest() {
    }

    public TossCancelRequest(String paymentKey, String cancelReason, Long cancelAmount) {
        this.paymentKey = paymentKey;
        this.cancelReason = cancelReason;
        this.cancelAmount = cancelAmount;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public void setPaymentKey(String paymentKey) {
        this.paymentKey = paymentKey;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public Long getCancelAmount() {
        return cancelAmount;
    }

    public void setCancelAmount(Long cancelAmount) {
        this.cancelAmount = cancelAmount;
    }
}