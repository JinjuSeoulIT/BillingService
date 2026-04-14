package com.hospital.billing.dto;

import com.hospital.billing.entity.Bill;
import com.hospital.billing.entity.BillingStatus;

import java.time.LocalDateTime;

public class BillSummaryResponse {

    private Long billId;

    // 추가: 업무용 청구 번호
    private String billingNo;

    private Long patientId;
    private LocalDateTime treatmentDate;
    private Integer totalAmount;
    private BillingStatus status;
    private Integer remainingAmount;

    public BillSummaryResponse() {
    }

    public BillSummaryResponse(Bill bill) {
        this.billId = bill.getId();
        this.billingNo = bill.getBillingNo(); // 추가
        this.patientId = bill.getPatientId();
        this.treatmentDate = bill.getTreatmentDate().toLocalDateTime();
        this.totalAmount = bill.getTotalAmount();
        this.status = bill.getStatus();
        this.remainingAmount = bill.getRemainingAmount();
    }

    /**
     * 계산형 상태 반영용 생성자
     */
    public BillSummaryResponse(Bill bill, BillingStatus calculatedStatus) {
        this.billId = bill.getId();
        this.billingNo = bill.getBillingNo(); // 추가
        this.patientId = bill.getPatientId();
        this.treatmentDate = bill.getTreatmentDate().toLocalDateTime();
        this.totalAmount = bill.getTotalAmount();
        this.status = calculatedStatus;
        this.remainingAmount = bill.getRemainingAmount();
    }

    public Long getBillId() {
        return billId;
    }

    // 추가
    public String getBillingNo() {
        return billingNo;
    }

    public Long getPatientId() {
        return patientId;
    }

    public LocalDateTime getTreatmentDate() {
        return treatmentDate;
    }

    public Integer getTotalAmount() {
        return totalAmount;
    }

    public BillingStatus getStatus() {
        return status;
    }

    public Integer getRemainingAmount() {
        return remainingAmount;
    }
}