package com.hospital.billing.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "BILLING_REQUEST")
public class BillingRequest {

    @Id
    // BILLING_REQUEST_INTG_SEQ 시퀀스 사용
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "billing_request_seq_generator")
    @SequenceGenerator(
            name = "billing_request_seq_generator",
            sequenceName = "BILLING_REQUEST_INTG_SEQ",
            allocationSize = 1
    )
    @Column(name = "BILLING_REQUEST_ID")
    private Long id;

    //  외부 요청 식별자(eventId)
    @Column(name = "EVENT_ID", nullable = false, unique = true, length = 100)
    private String eventId;

    //  방문 기준 연결
    @Column(name = "VISIT_ID")
    private Long visitId;

    // [환자 기준 연결
    @Column(name = "PATIENT_ID")
    private Long patientId;

    //  요청 종류
    @Column(name = "REQUEST_TYPE", nullable = false, length = 50)
    private String requestType;

    // 처리 상태 (RECEIVED / SUCCESS / FAILED)
    @Column(name = "REQUEST_STATUS", nullable = false, length = 20)
    private String requestStatus;

    // 성공 시 생성된 BILL_ID 연결
    @Column(name = "BILL_ID")
    private Long billId;

    //  원본 요청 JSON 저장
    @Lob
    @Column(name = "REQUEST_PAYLOAD")
    private String requestPayload;

    // 실패 메시지 저장
    @Column(name = "ERROR_MESSAGE", length = 1000)
    private String errorMessage;

    // 요청 수신 시각
    @Column(name = "CREATED_AT", nullable = false)
    private Timestamp createdAt;

    // 처리 완료 시각
    @Column(name = "PROCESSED_AT")
    private Timestamp processedAt;

    protected BillingRequest() {
    }

    public BillingRequest(String eventId,
                          Long visitId,
                          Long patientId,
                          String requestType,
                          String requestStatus,
                          String requestPayload,
                          Timestamp createdAt) {
        this.eventId = eventId;
        this.visitId = visitId;
        this.patientId = patientId;
        this.requestType = requestType;
        this.requestStatus = requestStatus;
        this.requestPayload = requestPayload;
        this.createdAt = createdAt;
    }

    // 요청 수신 상태 생성용 정적 메서드
    public static BillingRequest received(String eventId,
                                          Long visitId,
                                          Long patientId,
                                          String requestType,
                                          String requestPayload,
                                          Timestamp createdAt) {
        return new BillingRequest(
                eventId,
                visitId,
                patientId,
                requestType,
                "RECEIVED",
                requestPayload,
                createdAt
        );
    }

    // 성공 처리
    public void markSuccess(Long billId, Timestamp processedAt) {
        this.billId = billId;
        this.requestStatus = "SUCCESS";
        this.errorMessage = null;
        this.processedAt = processedAt;
    }

    // 실패 처리
    public void markFailed(String errorMessage, Timestamp processedAt) {
        this.requestStatus = "FAILED";
        this.errorMessage = errorMessage;
        this.processedAt = processedAt;
    }

    public Long getId() {
        return id;
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

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public String getRequestPayload() {
        return requestPayload;
    }

    public void setRequestPayload(String requestPayload) {
        this.requestPayload = requestPayload;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Timestamp processedAt) {
        this.processedAt = processedAt;
    }
}