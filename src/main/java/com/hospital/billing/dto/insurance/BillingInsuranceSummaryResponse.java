package com.hospital.billing.dto.insurance;

import java.util.ArrayList;
import java.util.List;

public class BillingInsuranceSummaryResponse {

    private Long billId;
    private Long patientId;
    private int originalAmount;
    private int calculatedAmount;
    private InsuranceInfoResponse validInsurance;
    private List<InsuranceInfoResponse> insuranceList = new ArrayList<>();
    private List<InsuranceHistoryInfoResponse> insuranceHistories = new ArrayList<>();
    private InsuranceCalculationSummaryResponse calculation;
    private String insuranceError;
    private String insuranceHistoryError;

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public int getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(int originalAmount) {
        this.originalAmount = originalAmount;
    }

    public int getCalculatedAmount() {
        return calculatedAmount;
    }

    public void setCalculatedAmount(int calculatedAmount) {
        this.calculatedAmount = calculatedAmount;
    }

    public InsuranceInfoResponse getValidInsurance() {
        return validInsurance;
    }

    public void setValidInsurance(InsuranceInfoResponse validInsurance) {
        this.validInsurance = validInsurance;
    }

    public List<InsuranceInfoResponse> getInsuranceList() {
        return insuranceList;
    }

    public void setInsuranceList(List<InsuranceInfoResponse> insuranceList) {
        this.insuranceList = insuranceList;
    }

    public List<InsuranceHistoryInfoResponse> getInsuranceHistories() {
        return insuranceHistories;
    }

    public void setInsuranceHistories(List<InsuranceHistoryInfoResponse> insuranceHistories) {
        this.insuranceHistories = insuranceHistories;
    }

    public InsuranceCalculationSummaryResponse getCalculation() {
        return calculation;
    }

    public void setCalculation(InsuranceCalculationSummaryResponse calculation) {
        this.calculation = calculation;
    }

    public String getInsuranceError() {
        return insuranceError;
    }

    public void setInsuranceError(String insuranceError) {
        this.insuranceError = insuranceError;
    }

    public String getInsuranceHistoryError() {
        return insuranceHistoryError;
    }

    public void setInsuranceHistoryError(String insuranceHistoryError) {
        this.insuranceHistoryError = insuranceHistoryError;
    }
}
