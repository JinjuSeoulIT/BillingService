package com.hospital.billing.dto;

import java.time.LocalDateTime;

public class BillSummaryResponse {

    private Long billId;
    private Long patientId;
    private LocalDateTime treatmentDate;
    private Integer totalAmount;

    public BillSummaryResponse() {
    }

    public BillSummaryResponse(Long billId, Long patientId,
                               LocalDateTime treatmentDate, Integer totalAmount) {
        this.billId = billId;
        this.patientId = patientId;
        this.treatmentDate = treatmentDate;
        this.totalAmount = totalAmount;
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
}
