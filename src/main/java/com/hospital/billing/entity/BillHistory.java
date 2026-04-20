package com.hospital.billing.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "BILL_HISTORY")
public class BillHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bill_history_seq_generator")
    @SequenceGenerator(
            name = "bill_history_seq_generator",
            sequenceName = "BILL_HISTORY_INTG_SEQ", // 실제 DB 시퀀스명이 다르면 이 줄만 네 DB 이름으로 바꿔줘
            allocationSize = 1
    )
    @Column(name = "BILL_HISTORY_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BILL_ID", nullable = false)
    private Bill bill;

    @Enumerated(EnumType.STRING)
    @Column(name = "OLD_STATUS", length = 20)
    private BillingStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "NEW_STATUS", nullable = false, length = 20)
    private BillingStatus newStatus;

    @Column(name = "CHANGED_AT", nullable = false)
    private Timestamp changedAt;

    @Column(name = "CHANGED_BY", length = 30)
    private String changedBy;

    @Column(name = "CHANGE_REASON", length = 200)
    private String changeReason;

    protected BillHistory() {
    }

    public BillHistory(Bill bill,
                       BillingStatus oldStatus,
                       BillingStatus newStatus,
                       Timestamp changedAt,
                       String changedBy,
                       String changeReason) {
        this.bill = bill;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedAt = changedAt;
        this.changedBy = changedBy;
        this.changeReason = changeReason;
    }

    public static BillHistory create(Bill bill,
                                     BillingStatus oldStatus,
                                     BillingStatus newStatus,
                                     String changedBy,
                                     String changeReason,
                                     Timestamp changedAt) {
        return new BillHistory(
                bill,
                oldStatus,
                newStatus,
                changedAt,
                changedBy,
                changeReason
        );
    }

    public Long getId() {
        return id;
    }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    public BillingStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(BillingStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public BillingStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(BillingStatus newStatus) {
        this.newStatus = newStatus;
    }

    public Timestamp getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Timestamp changedAt) {
        this.changedAt = changedAt;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }
}