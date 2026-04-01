package com.hospital.billing.dto.integration;

public class ClinicalCompletedResult {

    private Long billId;
    private boolean alreadyProcessed;

    public ClinicalCompletedResult() {
    }

    public ClinicalCompletedResult(Long billId, boolean alreadyProcessed) {
        this.billId = billId;
        this.alreadyProcessed = alreadyProcessed;
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public boolean isAlreadyProcessed() {
        return alreadyProcessed;
    }

    public void setAlreadyProcessed(boolean alreadyProcessed) {
        this.alreadyProcessed = alreadyProcessed;
    }
}