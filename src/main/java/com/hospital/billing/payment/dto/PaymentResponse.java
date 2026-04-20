package com.hospital.billing.payment.dto;

import java.sql.Timestamp;

public class PaymentResponse {

    private Long paymentId;
    private Long billId;
    private Integer paymentAmount;
    private String status;
    private String method;
    private Timestamp paidAt;
    private String createdBy;
    private String canceledBy;

    // [추가] 직원명 표시용
    private String createdByName;
    private String canceledByName;

    public PaymentResponse() {
    }

    public PaymentResponse(Long paymentId,
                           Long billId,
                           Integer paymentAmount,
                           String status,
                           String method,
                           Timestamp paidAt,
                           String createdBy,
                           String canceledBy) {
        this(
                paymentId,
                billId,
                paymentAmount,
                status,
                method,
                paidAt,
                createdBy,
                canceledBy,
                null,
                null
        );
    }

    public PaymentResponse(Long paymentId,
                           Long billId,
                           Integer paymentAmount,
                           String status,
                           String method,
                           Timestamp paidAt,
                           String createdBy,
                           String canceledBy,
                           String createdByName,
                           String canceledByName) {
        this.paymentId = paymentId;
        this.billId = billId;
        this.paymentAmount = paymentAmount;
        this.status = status;
        this.method = method;
        this.paidAt = paidAt;
        this.createdBy = createdBy;
        this.canceledBy = canceledBy;
        this.createdByName = createdByName;
        this.canceledByName = canceledByName;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public String getCanceledBy() {
        return canceledBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public String getCanceledByName() {
        return canceledByName;
    }
}