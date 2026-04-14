package com.hospital.billing.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "BILL")
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bill_seq_generator")
    @SequenceGenerator(
            name = "bill_seq_generator",
            sequenceName = "BILL_INTG_SEQ",
            allocationSize = 1
    )
    @Column(name = "BILL_ID")
    private Long id;

    // 추가: 업무용 청구 번호
    @Column(name = "BILLING_NO", unique = true, length = 30)
    private String billingNo;

    // 환자 ID
    @Column(name = "PATIENT_ID", nullable = false)
    private Long patientId;

    // 진료 방문 ID (진료와 연결)
    @Column(name = "VISIT_ID")
    private Long visitId;

    // 이벤트 ID (중복 처리 방지)
    @Column(name = "SOURCE_EVENT_ID", unique = true)
    private String sourceEventId;

    // 진료(내원/접수) 일시
    @Column(name = "TREATMENT_DATE", nullable = false)
    private Timestamp treatmentDate;

    // 총 진료비
    @Column(name = "TOTAL_AMOUNT", nullable = false)
    private Integer totalAmount;

    // 누적 결제 금액
    @Column(name = "PAID_AMOUNT", nullable = false)
    private Integer paidAmount;

    // 남은 금액
    @Column(name = "REMAINING_AMOUNT", nullable = false)
    private Integer remainingAmount;

    // 생성 시각
    @Column(name = "CREATED_AT", nullable = false)
    private Timestamp createdAt;

    // 청구 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private BillingStatus status;

    public Bill() {
    }

    public Bill(Long patientId,
                Timestamp treatmentDate,
                Integer totalAmount,
                Timestamp createdAt) {
        this.patientId = patientId;
        this.treatmentDate = treatmentDate;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.status = BillingStatus.READY;
        this.paidAmount = 0;
        this.remainingAmount = totalAmount;
    }

    // getter setter

    public Long getId() {
        return id;
    }

    // 추가
    public String getBillingNo() {
        return billingNo;
    }

    // 추가
    public void setBillingNo(String billingNo) {
        this.billingNo = billingNo;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getVisitId() {
        return visitId;
    }

    public void setVisitId(Long visitId) {
        this.visitId = visitId;
    }

    public String getSourceEventId() {
        return sourceEventId;
    }

    public void setSourceEventId(String sourceEventId) {
        this.sourceEventId = sourceEventId;
    }

    public Timestamp getTreatmentDate() {
        return treatmentDate;
    }

    public void setTreatmentDate(Timestamp treatmentDate) {
        this.treatmentDate = treatmentDate;
    }

    public Integer getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Integer totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Integer paidAmount) {
        this.paidAmount = paidAmount;
    }

    public Integer getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(Integer remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public BillingStatus getStatus() {
        return status;
    }

    public void setStatus(BillingStatus status) {
        this.status = status;
    }
}