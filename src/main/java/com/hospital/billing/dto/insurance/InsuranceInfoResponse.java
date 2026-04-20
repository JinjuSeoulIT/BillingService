package com.hospital.billing.dto.insurance;

public class InsuranceInfoResponse {

    private Long insuranceId;
    private Long patientId;
    private String insuranceType;
    private String policyNo;
    private boolean activeYn;
    private boolean verifiedYn;
    private String startDate;
    private String endDate;
    private String note;
    private String createdAt;
    private String updatedAt;

    public Long getInsuranceId() {
        return insuranceId;
    }

    public void setInsuranceId(Long insuranceId) {
        this.insuranceId = insuranceId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getInsuranceType() {
        return insuranceType;
    }

    public void setInsuranceType(String insuranceType) {
        this.insuranceType = insuranceType;
    }

    public String getPolicyNo() {
        return policyNo;
    }

    public void setPolicyNo(String policyNo) {
        this.policyNo = policyNo;
    }

    public boolean isActiveYn() {
        return activeYn;
    }

    public void setActiveYn(boolean activeYn) {
        this.activeYn = activeYn;
    }

    public boolean isVerifiedYn() {
        return verifiedYn;
    }

    public void setVerifiedYn(boolean verifiedYn) {
        this.verifiedYn = verifiedYn;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
