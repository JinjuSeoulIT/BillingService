package com.hospital.billing.dto.integration;

import java.time.LocalDateTime;
import java.util.List;

public class ClinicalCompletedRequest {

    private String eventId;
    private Long visitId;
    private Long patientId;
    private String status;
    private LocalDateTime occurredAt;
    private List<ClinicalClaimItemRequest> items;

    public ClinicalCompletedRequest() {
    }

    public ClinicalCompletedRequest(String eventId,
                                    Long visitId,
                                    Long patientId,
                                    String status,
                                    LocalDateTime occurredAt,
                                    List<ClinicalClaimItemRequest> items) {
        this.eventId = eventId;
        this.visitId = visitId;
        this.patientId = patientId;
        this.status = status;
        this.occurredAt = occurredAt;
        this.items = items;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Long getVisitId() {
        return visitId;
    }

    public void setVisitId(Long visitId) {
        this.visitId = visitId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public List<ClinicalClaimItemRequest> getItems() {
        return items;
    }

    public void setItems(List<ClinicalClaimItemRequest> items) {
        this.items = items;
    }
}