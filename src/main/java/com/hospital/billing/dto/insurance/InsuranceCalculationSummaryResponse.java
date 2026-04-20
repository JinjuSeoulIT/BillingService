package com.hospital.billing.dto.insurance;

public class InsuranceCalculationSummaryResponse {

    private String insuranceType;
    private double coverageRate;
    private int insuranceAppliedAmount;
    private int patientBurdenAmount;
    private String note;

    public InsuranceCalculationSummaryResponse() {
    }

    public InsuranceCalculationSummaryResponse(String insuranceType,
                                               double coverageRate,
                                               int insuranceAppliedAmount,
                                               int patientBurdenAmount,
                                               String note) {
        this.insuranceType = insuranceType;
        this.coverageRate = coverageRate;
        this.insuranceAppliedAmount = insuranceAppliedAmount;
        this.patientBurdenAmount = patientBurdenAmount;
        this.note = note;
    }

    public String getInsuranceType() {
        return insuranceType;
    }

    public void setInsuranceType(String insuranceType) {
        this.insuranceType = insuranceType;
    }

    public double getCoverageRate() {
        return coverageRate;
    }

    public void setCoverageRate(double coverageRate) {
        this.coverageRate = coverageRate;
    }

    public int getInsuranceAppliedAmount() {
        return insuranceAppliedAmount;
    }

    public void setInsuranceAppliedAmount(int insuranceAppliedAmount) {
        this.insuranceAppliedAmount = insuranceAppliedAmount;
    }

    public int getPatientBurdenAmount() {
        return patientBurdenAmount;
    }

    public void setPatientBurdenAmount(int patientBurdenAmount) {
        this.patientBurdenAmount = patientBurdenAmount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
