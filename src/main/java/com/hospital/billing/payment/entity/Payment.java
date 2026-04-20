package com.hospital.billing.payment.entity;

import com.hospital.billing.entity.Bill;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "PAYMENT_METHOD", nullable = false)
    private PaymentMethod method;

    @Column(name = "PAID_AT")
    private Timestamp paidAt;

    // 토스 카드 결제 원거래 식별값
    @Column(name = "PAYMENT_KEY", length = 200)
    private String paymentKey;

    // 토스 주문 번호
    @Column(name = "ORDER_ID", length = 100)
    private String orderId;

    // [추가] 결제 생성 직원 ID
    @Column(name = "CREATED_BY", length = 30)
    private String createdBy;

    // [추가] 결제 취소 직원 ID
    @Column(name = "CANCELED_BY", length = 30)
    private String canceledBy;

    protected Payment() {}

    // 기존 생성자 유지
    public Payment(Bill bill, Integer paymentAmount, PaymentMethod method) {
        this.bill = bill;
        this.paymentAmount = paymentAmount;
        this.status = PaymentStatus.COMPLETED;
        this.method = method;
        this.paidAt = new Timestamp(System.currentTimeMillis());
    }

    // 카드 결제용 생성자
    public Payment(Bill bill,
                   Integer paymentAmount,
                   PaymentMethod method,
                   String paymentKey,
                   String orderId) {
        this.bill = bill;
        this.paymentAmount = paymentAmount;
        this.status = PaymentStatus.COMPLETED;
        this.method = method;
        this.paidAt = new Timestamp(System.currentTimeMillis());
        this.paymentKey = paymentKey;
        this.orderId = orderId;
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

    // [추가] 필요 시 승인정보 저장/수정용 setter
    public void setPaymentKey(String paymentKey) {
        this.paymentKey = paymentKey;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    // [추가] 직원 ID setter
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setCanceledBy(String canceledBy) {
        this.canceledBy = canceledBy;
    }

    public Long getId() { return id; }
    public Bill getBill() { return bill; }
    public Integer getPaymentAmount() { return paymentAmount; }
    public PaymentStatus getStatus() { return status; }
    public PaymentMethod getMethod() { return method; }
    public Timestamp getPaidAt() { return paidAt; }

    // [추가] 토스 취소 시 필요
    public String getPaymentKey() { return paymentKey; }
    public String getOrderId() { return orderId; }

    // [추가] 직원 ID getter
    public String getCreatedBy() { return createdBy; }
    public String getCanceledBy() { return canceledBy; }
}