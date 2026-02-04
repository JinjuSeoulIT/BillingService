package com.hospital.billing.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 환자 ID (나중에 Patient와 연결)
    @Column(nullable = false)
    private Long patientId;

    // 결제 금액
    @Column(nullable = false)
    private Integer amount;

    // 결제 상태 (예: PAID, CANCELLED)
    @Column(nullable = false)
    private String status;

    // 생성 시각
    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected Payment() {
        // JPA 기본 생성자
    }

    public Payment(Long patientId, Integer amount, String status) {
        this.patientId = patientId;
        this.amount = amount;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public Integer getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}