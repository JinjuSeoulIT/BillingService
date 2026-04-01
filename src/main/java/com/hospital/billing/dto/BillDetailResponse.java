package com.hospital.billing.dto;

import com.hospital.billing.entity.Bill;

import java.time.LocalDateTime;
import java.util.List;

public class BillDetailResponse {

    private Long billId;
    private Long patientId;
    private LocalDateTime treatmentDate;

    private Integer totalAmount;
    private Integer paidAmount;        // 기존 유지
    private Integer remainingAmount;   // 기존 유지

    private String status;

    // 청구 상세 조회 시 항목 목록까지 같이 내려주기 위한 필드 추가
    private List<BillItemResponse> billItems;


    // 기존 사용처와의 호환을 위해 기존 생성자 유지
    public BillDetailResponse(Bill bill) {
        this.billId = bill.getId();
        this.patientId = bill.getPatientId();
        this.treatmentDate = bill.getTreatmentDate().toLocalDateTime();

        this.totalAmount = bill.getTotalAmount();
        this.paidAmount = bill.getPaidAmount();
        this.remainingAmount = bill.getRemainingAmount();

        this.status = bill.getStatus().name();

        // 기존 생성자 호출 시에도 null 방지 차원에서 빈 값 허용
        this.billItems = List.of();
    }

    // Bill + BillItem 목록을 함께 담는 생성자 추가
    public BillDetailResponse(Bill bill, List<BillItemResponse> billItems) {
        this.billId = bill.getId();
        this.patientId = bill.getPatientId();
        this.treatmentDate = bill.getTreatmentDate().toLocalDateTime();

        this.totalAmount = bill.getTotalAmount();
        this.paidAmount = bill.getPaidAmount();
        this.remainingAmount = bill.getRemainingAmount();

        this.status = bill.getStatus().name();

        this.billItems = billItems;
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

    public List<BillItemResponse> getBillItems() {
        return billItems;
    }
}