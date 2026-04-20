package com.hospital.billing.entity;

import com.hospital.billing.payment.entity.PaymentMethod;
import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "BILLING_DEPOSIT")
public class BillingDeposit {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "billing_deposit_seq_generator")
    @SequenceGenerator(
            name = "billing_deposit_seq_generator",
            sequenceName = "BILLING_DEPOSIT_SEQ",
            allocationSize = 1
    )
    @Column(name = "DEPOSIT_ID")
    private Long depositId;

    @Column(name = "PATIENT_ID", nullable = false)
    private Long patientId;

    @Column(name = "DEPOSIT_AMOUNT", nullable = false)
    private Integer depositAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "PAYMENT_METHOD", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "DEPOSIT_STATUS", nullable = false, length = 20)
    private BillingDepositStatus depositStatus;

    @Column(name = "DEPOSIT_MEMO", length = 500)
    private String depositMemo;

    @Column(name = "RECEIVED_AT", nullable = false)
    private Timestamp receivedAt;

    @Column(name = "CREATED_AT", nullable = false)
    private Timestamp createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private Timestamp updatedAt;

    public BillingDeposit() {
    }

    public static BillingDeposit create(
            Long patientId,
            Integer depositAmount,
            PaymentMethod paymentMethod,
            String depositMemo,
            Timestamp receivedAt
    ) {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        BillingDeposit entity = new BillingDeposit();
        entity.patientId = patientId;
        entity.depositAmount = depositAmount;
        entity.paymentMethod = paymentMethod;
        entity.depositStatus = BillingDepositStatus.REGISTERED;
        entity.depositMemo = depositMemo;
        entity.receivedAt = receivedAt != null ? receivedAt : now;
        entity.createdAt = now;
        entity.updatedAt = now;
        return entity;
    }

    public Long getDepositId() {
        return depositId;
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

    public BillingDepositStatus getDepositStatus() {
        return depositStatus;
    }

    public void setDepositStatus(BillingDepositStatus depositStatus) {
        this.depositStatus = depositStatus;
    }

    public String getDepositMemo() {
        return depositMemo;
    }

    public void setDepositMemo(String depositMemo) {
        this.depositMemo = depositMemo;
    }

    public Timestamp getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Timestamp receivedAt) {
        this.receivedAt = receivedAt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
