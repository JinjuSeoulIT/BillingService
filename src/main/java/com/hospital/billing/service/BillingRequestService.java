package com.hospital.billing.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.billing.dto.integration.ClinicalCompletedRequest;
import com.hospital.billing.entity.BillingRequest;
import com.hospital.billing.repository.BillingRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class BillingRequestService {

    private static final String REQUEST_TYPE_CLINICAL_COMPLETED = "CLINICAL_COMPLETED";

    private final BillingRequestRepository billingRequestRepository;
    private final ObjectMapper objectMapper;

    public BillingRequestService(BillingRequestRepository billingRequestRepository,
                                 ObjectMapper objectMapper) {
        this.billingRequestRepository = billingRequestRepository;
        this.objectMapper = objectMapper;
    }
    
    // eventId 기준 요청 이력 조회
    @Transactional(readOnly = true)
    public Optional<BillingRequest> findByEventId(String eventId) {
        return billingRequestRepository.findByEventId(eventId);
    }

    // 요청 수신 이력은 별도 트랜잭션으로 저장
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BillingRequest saveReceived(ClinicalCompletedRequest request) {
        BillingRequest billingRequest = BillingRequest.received(
                request.getEventId(),
                request.getVisitId(),
                request.getPatientId(),
                REQUEST_TYPE_CLINICAL_COMPLETED,
                toRequestPayload(request),
                Timestamp.valueOf(LocalDateTime.now())
        );

        return billingRequestRepository.save(billingRequest);
    }


    // 성공 처리는 REQUIRES_NEW 제거
    // 이유:
    // BILL 저장과 BILLING_REQUEST 성공 상태 업데이트가 같은 트랜잭션에서 같이 커밋되어야
    // FK(BILLING_REQUEST.BILL_ID -> BILL.BILL_ID) 위반이 발생하지 않음
    @Transactional
    public void markSuccess(Long billingRequestId, Long billId) {
        BillingRequest billingRequest = billingRequestRepository.findById(billingRequestId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "요청 이력이 존재하지 않습니다. billingRequestId=" + billingRequestId
                ));

        billingRequest.markSuccess(
                billId,
                Timestamp.valueOf(LocalDateTime.now())
        );
    }

    // 실패 처리는 본 트랜잭션이 롤백돼도 남아야 하므로 별도 트랜잭션 유지
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long billingRequestId, String errorMessage) {
        BillingRequest billingRequest = billingRequestRepository.findById(billingRequestId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "요청 이력이 존재하지 않습니다. billingRequestId=" + billingRequestId
                ));

        billingRequest.markFailed(
                errorMessage,
                Timestamp.valueOf(LocalDateTime.now())
        );
    }

    // 요청 payload JSON 문자열 변환
    private String toRequestPayload(ClinicalCompletedRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            return "{\"eventId\":\"" + request.getEventId() + "\"," +
                    "\"visitId\":" + request.getVisitId() + "," +
                    "\"patientId\":" + request.getPatientId() + "," +
                    "\"status\":\"" + request.getStatus() + "\"}";
        }
    }
}