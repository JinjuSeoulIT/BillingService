package com.hospital.billing.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bills")
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 환자 ID
    @Column(nullable = false)
    private Long patientId;

    // 진료(내원/접수) 일시
    @Column(nullable = false)
    private LocalDateTime treatmentDate;

    // 총 진료비
    @Column(nullable = false)
    private Integer totalAmount;

    // 생성 시각
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 청구 상태 (추가)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingStatus status;

    // JPA 기본 생성자
    protected Bill() {
    }

    // 생성자 (기존 + status 기본값 READY)
    public Bill(Long patientId,
                LocalDateTime treatmentDate,
                Integer totalAmount,
                LocalDateTime createdAt) {
        this.patientId = patientId;
        this.treatmentDate = treatmentDate;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.status = BillingStatus.READY;
    }

    // ===== getter / setter =====
    public Long getId() {
        return id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public LocalDateTime getTreatmentDate() {
        return treatmentDate;
    }

    public void setTreatmentDate(LocalDateTime treatmentDate) {
        this.treatmentDate = treatmentDate;
    }

    public Integer getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Integer totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public BillingStatus getStatus() {
        return status;
    }

    public void setStatus(BillingStatus status) {
        this.status = status;
    }
}
