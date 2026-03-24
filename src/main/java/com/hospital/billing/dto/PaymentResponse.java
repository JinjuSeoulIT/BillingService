package com.hospital.billing.dto;

import java.sql.Timestamp;

public class PaymentResponse {

    private Long paymentId;
    private Long billId;
    private Integer paymentAmount;
    private String status;
    private String method;
    private Timestamp paidAt;

    public PaymentResponse() {
    }

    public PaymentResponse(Long paymentId,
                           Long billId,
                           Integer paymentAmount,
                           String status,
                           String method,
                           Timestamp paidAt) {
        this.paymentId = paymentId;
        this.billId = billId;
        this.paymentAmount = paymentAmount;
        this.status = status;
        this.method = method;
        this.paidAt = paidAt;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public Long getBillId() {
        return billId;
    }

    public Integer getPaymentAmount() {
        return paymentAmount;
    }

    public String getStatus() {
        return status;
    }

    public String getMethod() {
        return method;
    }

    public Timestamp getPaidAt() {
        return paidAt;
    }
}
