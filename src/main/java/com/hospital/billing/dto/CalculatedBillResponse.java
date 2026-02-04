package com.hospital.billing.dto;

public class CalculatedBillResponse {

    private Long billId;
    private Integer originalAmount;     // 원금액
    private Integer calculatedAmount;   // 계산 후 금액 (더미)
    private String calculationNote;     // 계산 설명

    public CalculatedBillResponse() {
    }

    public CalculatedBillResponse(Long billId, Integer originalAmount,
                                  Integer calculatedAmount, String calculationNote) {
        this.billId = billId;
        this.originalAmount = originalAmount;
        this.calculatedAmount = calculatedAmount;
        this.calculationNote = calculationNote;
    }

    public Long getBillId() {
        return billId;
    }

    public Integer getOriginalAmount() {
        return originalAmount;
    }

    public Integer getCalculatedAmount() {
        return calculatedAmount;
    }

    public String getCalculationNote() {
        return calculationNote;
    }
}
