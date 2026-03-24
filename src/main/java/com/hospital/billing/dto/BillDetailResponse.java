package com.hospital.billing.dto;

import com.hospital.billing.entity.Bill;

import java.time.LocalDateTime;

public class BillDetailResponse {

    private Long billId;
    private Long patientId;
    private LocalDateTime treatmentDate;

    private Integer totalAmount;
    private Integer paidAmount;        // 추가
    private Integer remainingAmount;   // 추가

    private String status;

    public BillDetailResponse(Bill bill) {
        this.billId = bill.getId();
        this.patientId = bill.getPatientId();
        this.treatmentDate = bill.getTreatmentDate().toLocalDateTime();

        this.totalAmount = bill.getTotalAmount();
        this.paidAmount = bill.getPaidAmount();
        this.remainingAmount = bill.getRemainingAmount();

        this.status = bill.getStatus().name();
    }

    public Long getBillId() {
        return billId;
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

    public Integer getPaidAmount() {
        return paidAmount;
    }

    public Integer getRemainingAmount() {
        return remainingAmount;
    }

    public String getStatus() {
        return status;
    }
}
