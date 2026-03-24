package com.hospital.billing.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "PAYMENT")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_seq_gen")
    @SequenceGenerator(
            name = "payment_seq_gen",
            sequenceName = "PAYMENT_SEQ",
            allocationSize = 1
    )
    @Column(name = "PAYMENT_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BILL_ID", nullable = false)
    private Bill bill;

    @Column(name = "PAYMENT_AMOUNT", nullable = false)
    private Integer paymentAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "PAYMENT_STATUS", nullable = false)
    private PaymentStatus status;

    // [수정] String → PaymentMethod enum
    @Enumerated(EnumType.STRING)
    @Column(name = "PAYMENT_METHOD", nullable = false)
    private PaymentMethod method;

    @Column(name = "PAID_AT")
    private Timestamp paidAt;

    protected Payment() {}

    // [수정] 결제수단 받는 생성자
    public Payment(Bill bill, Integer paymentAmount, PaymentMethod method) {
        this.bill = bill;
        this.paymentAmount = paymentAmount;
        this.status = PaymentStatus.COMPLETED;
        this.method = method;
        this.paidAt = new Timestamp(System.currentTimeMillis());
    }

    public void cancel() {
        if (this.status == PaymentStatus.CANCELED) {
            throw new IllegalStateException("이미 취소된 결제입니다.");
        }
        this.status = PaymentStatus.CANCELED;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public Long getId() { return id; }
    public Bill getBill() { return bill; }
    public Integer getPaymentAmount() { return paymentAmount; }
    public PaymentStatus getStatus() { return status; }

    // [수정] 반환 타입 String → PaymentMethod
    public PaymentMethod getMethod() { return method; }

    public Timestamp getPaidAt() { return paidAt; }
}