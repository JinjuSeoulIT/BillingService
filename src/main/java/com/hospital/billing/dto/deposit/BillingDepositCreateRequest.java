package com.hospital.billing.dto.deposit;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hospital.billing.payment.entity.PaymentMethod;

import java.time.LocalDateTime;

public class BillingDepositCreateRequest {

    private Long patientId;
    private Integer depositAmount;
    private PaymentMethod paymentMethod;
    private String depositMemo;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime receivedAt;

    public BillingDepositCreateRequest() {
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Integer getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(Integer depositAmount) {
        this.depositAmount = depositAmount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDepositMemo() {
        return depositMemo;
    }

    public void setDepositMemo(String depositMemo) {
        this.depositMemo = depositMemo;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }
}
