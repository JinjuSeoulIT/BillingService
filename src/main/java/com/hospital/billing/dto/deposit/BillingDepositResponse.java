package com.hospital.billing.dto.deposit;

import com.hospital.billing.entity.BillingDeposit;
import com.hospital.billing.entity.BillingDepositStatus;
import com.hospital.billing.payment.entity.PaymentMethod;

import java.sql.Timestamp;

public class BillingDepositResponse {

    private Long depositId;
    private Long patientId;
    private Integer depositAmount;
    private PaymentMethod paymentMethod;
    private BillingDepositStatus depositStatus;
    private String depositMemo;
    private Timestamp receivedAt;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public static BillingDepositResponse from(BillingDeposit entity) {
        BillingDepositResponse response = new BillingDepositResponse();
        response.depositId = entity.getDepositId();
        response.patientId = entity.getPatientId();
        response.depositAmount = entity.getDepositAmount();
        response.paymentMethod = entity.getPaymentMethod();
        response.depositStatus = entity.getDepositStatus();
        response.depositMemo = entity.getDepositMemo();
        response.receivedAt = entity.getReceivedAt();
        response.createdAt = entity.getCreatedAt();
        response.updatedAt = entity.getUpdatedAt();
        return response;
    }

    public Long getDepositId() {
        return depositId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public Integer getDepositAmount() {
        return depositAmount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public BillingDepositStatus getDepositStatus() {
        return depositStatus;
    }

    public String getDepositMemo() {
        return depositMemo;
    }

    public Timestamp getReceivedAt() {
        return receivedAt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }
}
