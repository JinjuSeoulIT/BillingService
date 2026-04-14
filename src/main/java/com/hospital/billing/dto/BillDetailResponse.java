package com.hospital.billing.dto;

import com.hospital.billing.entity.Bill;
import com.hospital.billing.entity.BillingStatus;

import java.time.LocalDateTime;
import java.util.List;

public class BillDetailResponse {

    private Long billId;

    // 추가: 업무용 청구 번호
    private String billingNo;

    private Long patientId;
    private LocalDateTime treatmentDate;

    private Integer totalAmount;
    private Integer paidAmount;
    private Integer remainingAmount;

    private String status;

    private List<BillItemResponse> billItems;

    public BillDetailResponse(Bill bill) {
        this.billId = bill.getId();
        this.billingNo = bill.getBillingNo(); // 추가
        this.patientId = bill.getPatientId();
        this.treatmentDate = bill.getTreatmentDate().toLocalDateTime();

        this.totalAmount = bill.getTotalAmount();
        this.paidAmount = bill.getPaidAmount();
        this.remainingAmount = bill.getRemainingAmount();

        this.status = bill.getStatus().name();
        this.billItems = List.of();
    }

    public BillDetailResponse(Bill bill, List<BillItemResponse> billItems) {
        this.billId = bill.getId();
        this.billingNo = bill.getBillingNo(); // 추가
        this.patientId = bill.getPatientId();
        this.treatmentDate = bill.getTreatmentDate().toLocalDateTime();

        this.totalAmount = bill.getTotalAmount();
        this.paidAmount = bill.getPaidAmount();
        this.remainingAmount = bill.getRemainingAmount();

        this.status = bill.getStatus().name();
        this.billItems = billItems;
    }

    /**
     * 계산형 상태 반영용 생성자
     */
    public BillDetailResponse(Bill bill,
                              BillingStatus calculatedStatus,
                              List<BillItemResponse> billItems) {
        this.billId = bill.getId();
        this.billingNo = bill.getBillingNo(); // 추가
        this.patientId = bill.getPatientId();
        this.treatmentDate = bill.getTreatmentDate().toLocalDateTime();

        this.totalAmount = bill.getTotalAmount();
        this.paidAmount = bill.getPaidAmount();
        this.remainingAmount = bill.getRemainingAmount();

        this.status = calculatedStatus.name();
        this.billItems = billItems;
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

    public Integer getPaidAmount() {
        return paidAmount;
    }

    public Integer getRemainingAmount() {
        return remainingAmount;
    }

    public String getStatus() {
        return status;
    }

    public List<BillItemResponse> getBillItems() {
        return billItems;
    }
}