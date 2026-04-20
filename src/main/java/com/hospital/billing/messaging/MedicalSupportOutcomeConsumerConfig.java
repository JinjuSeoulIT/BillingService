package com.hospital.billing.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.billing.facade.BillingFacade;
import com.hospital.common.event.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MedicalSupportOutcomeConsumerConfig {

    private static final String SOURCE_TYPE_MEDICATION = "MEDICAL_SUPPORT_MEDICATION_RECORD";
    private static final String SOURCE_TYPE_TREATMENT = "MEDICAL_SUPPORT_TREATMENT_RESULT";
    private static final String SOURCE_TYPE_DIAGNOSTIC_EXAM = "MEDICAL_SUPPORT_DIAGNOSTIC_EXAM";
    private static final String SOURCE_TYPE_DIAGNOSTIC_TEST_RESULT = "MEDICAL_SUPPORT_DIAGNOSTIC_TEST_RESULT";

    private final BillingFacade billingFacade;
    private final ObjectMapper objectMapper;

    @Bean
    public Consumer<Message<byte[]>> medicationRecordOutcomeConsumer() {
        return message -> applyOutcome(message, SOURCE_TYPE_MEDICATION, "MEDICATION");
    }

    @Bean
    public Consumer<Message<byte[]>> treatmentResultOutcomeConsumer() {
        return message -> applyOutcome(message, SOURCE_TYPE_TREATMENT, "PROCEDURE");
    }

    @Bean
    public Consumer<Message<byte[]>> diagnosticExamOutcomeConsumer() {
        return message -> applyOutcome(message, SOURCE_TYPE_DIAGNOSTIC_EXAM, "TEST");
    }

    @Bean
    public Consumer<Message<byte[]>> diagnosticTestResultOutcomeConsumer() {
        return message -> applyOutcome(message, SOURCE_TYPE_DIAGNOSTIC_TEST_RESULT, "TEST");
    }

    private void applyOutcome(Message<byte[]> message, String sourceType, String itemCategory) {
        if (message == null || message.getPayload() == null) {
            return;
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(message.getPayload());
        } catch (Exception e) {
            log.warn("[진료지원→수납][Kafka] JSON 파싱 실패 sourceType={} message={}", sourceType, e.getMessage());
            return;
        }

        // Event 래퍼면 data를 사용, 아니면 루트를 그대로 사용
        JsonNode data = root;
        if (root.hasNonNull("eventType") && root.has("data")) {
            data = root.get("data");
        }

        Long patientId = asLong(data, "patientId");
        if (patientId == null || patientId <= 0L) {
            log.warn("[진료지원→수납][Kafka] patientId 누락 sourceType={} payload={}", sourceType, safeJson(root));
            return;
        }

        Long visitId = asLong(data, "visitId");

        String itemName = firstNonBlank(
                asText(data, "examKind"),
                asText(data, "resultTypeName"),
                asText(data, "detailCode"),
                asText(data, "doseKind"),
                asText(data, "status"),
                "미정"
        );

        Long sourceId = resolveSourceId(data);
        if (sourceId == null || sourceId <= 0L) {
            log.warn("[진료지원→수납][Kafka] sourceId 해석 실패 sourceType={} payload={}", sourceType, safeJson(root));
            return;
        }

        String sourceEventId = asText(root, "eventId");
        if (isBlank(sourceEventId) && root.hasNonNull("key")) {
            sourceEventId = "support-outcome-" + root.get("key").asText();
        }

        BillingFacade.OutcomeCharge charge = new BillingFacade.OutcomeCharge(
                patientId,
                visitId,
                itemName,
                itemCategory,
                1,
                null,
                sourceType,
                sourceId,
                sourceEventId
        );

        BillingFacade.OutcomeAppliedResult result;
        try {
            result = billingFacade.handleMedicalSupportOutcome(charge);
        } catch (Exception e) {
            log.warn("[진료지원→수납][Kafka] 금액 산정/저장 실패 sourceType={} patientId={} sourceId={} message={}",
                    sourceType, patientId, sourceId, e.getMessage());
            return;
        }

        log.info("[진료지원→수납][Kafka] outcome 반영 완료 sourceType={} patientId={} sourceId={} billId={} alreadyProcessed={}",
                sourceType, patientId, sourceId, result.billId(), result.alreadyProcessed());
    }

    private Long resolveSourceId(JsonNode data) {
        // 진료지원 DTO별 후보 필드들
        Long v = asLong(data, "orderItemId");
        if (v != null) return v;
        v = parseFlexibleLong(asText(data, "medicationRecordId"));
        if (v != null) return v;
        v = parseFlexibleLong(asText(data, "treatmentResultId"));
        if (v != null) return v;
        v = parseFlexibleLong(asText(data, "resultId"));
        if (v != null) return v;
        v = parseFlexibleLong(asText(data, "examId"));
        if (v != null) return v;
        v = parseFlexibleLong(asText(data, "testExecutionId"));
        if (v != null) return v;
        return null;
    }

    private Long asLong(JsonNode node, String field) {
        if (node == null || field == null || !node.hasNonNull(field)) {
            return null;
        }
        JsonNode v = node.get(field);
        if (v.isNumber()) {
            return v.asLong();
        }
        if (v.isTextual()) {
            return parseFlexibleLong(v.asText());
        }
        return null;
    }

    private String asText(JsonNode node, String field) {
        if (node == null || field == null || !node.hasNonNull(field)) {
            return null;
        }
        JsonNode v = node.get(field);
        return v.isTextual() ? v.asText() : v.toString();
    }

    private Long parseFlexibleLong(String value) {
        if (isBlank(value)) {
            return null;
        }
        String trimmed = value.trim();
        try {
            return Long.parseLong(trimmed);
        } catch (Exception ignore) {
            return null;
        }
    }

    private String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (!isBlank(v)) return v.trim();
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safeJson(JsonNode node) {
        try {
            return node == null ? "null" : objectMapper.writeValueAsString(node);
        } catch (Exception ignore) {
            return "<unserializable>";
        }
    }
}

