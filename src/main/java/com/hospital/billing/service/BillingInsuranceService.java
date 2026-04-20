package com.hospital.billing.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.billing.dto.insurance.BillingInsuranceSummaryResponse;
import com.hospital.billing.dto.insurance.InsuranceCalculationSummaryResponse;
import com.hospital.billing.dto.insurance.InsuranceHistoryInfoResponse;
import com.hospital.billing.dto.insurance.InsuranceInfoResponse;
import com.hospital.billing.entity.Bill;
import com.hospital.billing.entity.BillItem;
import com.hospital.billing.repository.BillItemRepository;
import com.hospital.billing.repository.BillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BillingInsuranceService {

    private static final Logger log = LoggerFactory.getLogger(BillingInsuranceService.class);
    private static final String HEALTH_INSURANCE_TYPE = "NATIONAL";

    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final String patientApiBaseUrl;

    public BillingInsuranceService(BillRepository billRepository,
                                   BillItemRepository billItemRepository,
                                   ObjectMapper objectMapper,
                                   @Value("${patient.api.base-url:http://192.168.1.60:8181}") String patientApiBaseUrl) {
        this.billRepository = billRepository;
        this.billItemRepository = billItemRepository;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
        this.patientApiBaseUrl = patientApiBaseUrl;
    }

    public BillingInsuranceSummaryResponse getInsuranceSummary(Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new IllegalArgumentException("청구 정보를 찾을 수 없습니다. billId=" + billId));

        int originalAmount = safeAmount(bill.getTotalAmount());
        int calculatedAmount = calculateBillItemsAmount(bill.getId(), originalAmount);

        BillingInsuranceSummaryResponse response = new BillingInsuranceSummaryResponse();
        response.setBillId(bill.getId());
        response.setPatientId(bill.getPatientId());
        response.setOriginalAmount(originalAmount);
        response.setCalculatedAmount(calculatedAmount);

        Long patientId = bill.getPatientId();

        InsuranceInfoResponse validInsurance = null;
        List<InsuranceInfoResponse> insuranceList = Collections.emptyList();
        List<InsuranceHistoryInfoResponse> insuranceHistories = Collections.emptyList();

        try {
            validInsurance = filterHealthInsurance(fetchValidInsurance(patientId));
            insuranceList = filterHealthInsuranceList(fetchInsuranceList(patientId));
        } catch (Exception e) {
            log.error("보험 정보 조회 실패 - billId={}, patientId={}", billId, patientId, e);
            response.setInsuranceError("보험 정보 조회 실패");
        }

        try {
            insuranceHistories = filterHealthInsuranceHistories(
                    fetchInsuranceHistories(patientId),
                    insuranceList
            );
        } catch (Exception e) {
            log.error("보험 이력 조회 실패 - billId={}, patientId={}", billId, patientId, e);
            response.setInsuranceHistoryError("보험 이력 조회 실패");
        }

        response.setValidInsurance(validInsurance);
        response.setInsuranceList(insuranceList);
        response.setInsuranceHistories(insuranceHistories);
        response.setCalculation(calculateInsuranceSummary(validInsurance, calculatedAmount));

        return response;
    }

    private List<InsuranceInfoResponse> fetchInsuranceList(Long patientId) {
        String url = UriComponentsBuilder
                .fromHttpUrl(patientApiBaseUrl)
                .path("/api/insurances")
                .queryParam("patientId", patientId)
                .toUriString();

        JsonNode resultNode = fetchResultNode(url);
        if (resultNode == null || resultNode.isNull()) {
            return Collections.emptyList();
        }

        return objectMapper.convertValue(resultNode, new TypeReference<List<InsuranceInfoResponse>>() {});
    }

    private InsuranceInfoResponse fetchValidInsurance(Long patientId) {
        String url = UriComponentsBuilder
                .fromHttpUrl(patientApiBaseUrl)
                .path("/api/insurances/valid")
                .queryParam("patientId", patientId)
                .toUriString();

        JsonNode resultNode = fetchResultNode(url);
        if (resultNode == null || resultNode.isNull()) {
            return null;
        }

        return objectMapper.convertValue(resultNode, InsuranceInfoResponse.class);
    }

    private List<InsuranceHistoryInfoResponse> fetchInsuranceHistories(Long patientId) {
        String url = UriComponentsBuilder
                .fromHttpUrl(patientApiBaseUrl)
                .path("/api/insurances/history")
                .queryParam("patientId", patientId)
                .toUriString();

        JsonNode resultNode = fetchResultNode(url);
        if (resultNode == null || resultNode.isNull()) {
            return Collections.emptyList();
        }

        return objectMapper.convertValue(resultNode, new TypeReference<List<InsuranceHistoryInfoResponse>>() {});
    }

    private JsonNode fetchResultNode(String url) {
        ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
        JsonNode body = response.getBody();

        if (body == null) {
            throw new IllegalStateException("환자 보험 API 응답 본문이 없습니다.");
        }

        JsonNode successNode = body.get("success");
        if (successNode != null && !successNode.asBoolean()) {
            String message = body.has("message") ? body.get("message").asText() : "환자 보험 API 호출 실패";
            throw new IllegalStateException(message);
        }

        return body.get("result");
    }

    private InsuranceCalculationSummaryResponse calculateInsuranceSummary(InsuranceInfoResponse validInsurance,
                                                                          int calculatedAmount) {
        String insuranceType = validInsurance != null ? validInsurance.getInsuranceType() : null;
        double coverageRate = getCoverageRateByInsuranceType(insuranceType);
        int insuranceAppliedAmount = (int) Math.floor(calculatedAmount * coverageRate);
        int patientBurdenAmount = calculatedAmount - insuranceAppliedAmount;
        String note = getInsuranceCalculationNote(validInsurance);

        return new InsuranceCalculationSummaryResponse(
                insuranceType,
                coverageRate,
                insuranceAppliedAmount,
                patientBurdenAmount,
                note
        );
    }

    private int calculateBillItemsAmount(Long billId, int originalAmount) {
        List<BillItem> billItems = billItemRepository.findByBillId(billId);

        if (billItems.isEmpty()) {
            return originalAmount;
        }

        return billItems.stream()
                .map(BillItem::getAmount)
                .filter(amount -> amount != null)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private int safeAmount(Integer amount) {
        return amount == null ? 0 : amount;
    }

    private InsuranceInfoResponse filterHealthInsurance(InsuranceInfoResponse insurance) {
        if (insurance == null) {
            return null;
        }

        String normalized = normalizeInsuranceType(insurance.getInsuranceType());
        if (!HEALTH_INSURANCE_TYPE.equals(normalized)) {
            return null;
        }

        return insurance;
    }

    private List<InsuranceInfoResponse> filterHealthInsuranceList(List<InsuranceInfoResponse> insuranceList) {
        if (insuranceList == null || insuranceList.isEmpty()) {
            return Collections.emptyList();
        }

        return insuranceList.stream()
                .filter(Objects::nonNull)
                .filter(insurance -> HEALTH_INSURANCE_TYPE.equals(normalizeInsuranceType(insurance.getInsuranceType())))
                .toList();
    }

    private List<InsuranceHistoryInfoResponse> filterHealthInsuranceHistories(
            List<InsuranceHistoryInfoResponse> insuranceHistories,
            List<InsuranceInfoResponse> insuranceList
    ) {
        if (insuranceHistories == null || insuranceHistories.isEmpty()) {
            return Collections.emptyList();
        }

        if (insuranceList == null || insuranceList.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> healthInsuranceIds = insuranceList.stream()
                .filter(Objects::nonNull)
                .map(InsuranceInfoResponse::getInsuranceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (healthInsuranceIds.isEmpty()) {
            return Collections.emptyList();
        }

        return insuranceHistories.stream()
                .filter(Objects::nonNull)
                .filter(history -> history.getInsuranceId() != null)
                .filter(history -> healthInsuranceIds.contains(history.getInsuranceId()))
                .toList();
    }

    private String normalizeInsuranceType(String insuranceType) {
        if (insuranceType == null || insuranceType.trim().isEmpty()) {
            return null;
        }

        String value = insuranceType.trim().toUpperCase();

        if (value.equals("NATIONAL") || value.equals("NHI") || value.equals("NHI보험") || value.equals("국민건강보험") || value.equals("건강보험")) {
            return "NATIONAL";
        }
        if (value.equals("MEDICAID") || value.equals("의료급여")) {
            return "MEDICAID";
        }
        if (value.equals("INDUSTRIAL") || value.equals("산재보험")) {
            return "INDUSTRIAL";
        }
        if (value.equals("AUTO") || value.equals("자동차보험")) {
            return "AUTO";
        }
        if (value.equals("PRIVATE") || value.equals("실손보험") || value.equals("민간보험")) {
            return "PRIVATE";
        }

        return insuranceType;
    }

    private double getCoverageRateByInsuranceType(String insuranceType) {
        String normalized = normalizeInsuranceType(insuranceType);

        if ("NATIONAL".equals(normalized)) {
            return 0.7;
        }
        if ("MEDICAID".equals(normalized)) {
            return 0.8;
        }
        if ("INDUSTRIAL".equals(normalized) || "AUTO".equals(normalized)) {
            return 1.0;
        }
        return 0.0;
    }

    private String getInsuranceCalculationNote(InsuranceInfoResponse validInsurance) {
        if (validInsurance == null) {
            return "현재 유효 보험이 없어 본인부담금 전액 기준으로 표시합니다.";
        }

        String normalized = normalizeInsuranceType(validInsurance.getInsuranceType());

        if ("NATIONAL".equals(normalized)) {
            return "임시 보험 계산 정책 기준으로 건강보험 70%, 본인부담 30%를 적용했습니다.";
        }
        if ("MEDICAID".equals(normalized)) {
            return "임시 보험 계산 정책 기준으로 의료급여 80%, 본인부담 20%를 적용했습니다.";
        }
        if ("INDUSTRIAL".equals(normalized)) {
            return "임시 보험 계산 정책 기준으로 산재보험 100%, 본인부담 0%를 적용했습니다.";
        }
        if ("AUTO".equals(normalized)) {
            return "임시 보험 계산 정책 기준으로 자동차보험 100%, 본인부담 0%를 적용했습니다.";
        }
        if ("PRIVATE".equals(normalized)) {
            return "실손보험은 사후 청구 대상이므로 현재 본인부담금 계산에 직접 반영하지 않았습니다.";
        }
        return "현재 유효 보험이 없어 본인부담금 전액 기준으로 표시합니다.";
    }
}
