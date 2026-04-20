package com.hospital.billing.messaging;

import com.hospital.billing.dto.integration.ClinicalCompletedRequest;
import com.hospital.billing.dto.integration.ClinicalCompletedResult;
import com.hospital.billing.facade.BillingFacade;
import com.hospital.common.event.Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ClinicalCompletedConsumerConfig {

    private final BillingFacade billingFacade;
    private final ObjectMapper objectMapper;

    @Bean
    public Consumer<Message<byte[]>> clinicalCompletedConsumer() {
        return message -> {
            if (message == null || message.getPayload() == null) {
                return;
            }
            // MSA 표준: Event 래퍼를 우선 파싱
            ClinicalCompletedRequest request = null;
            try {
                Event<Object, ClinicalCompletedRequest> event =
                        objectMapper.readValue(message.getPayload(), objectMapper.getTypeFactory()
                                .constructParametricType(Event.class, Object.class, ClinicalCompletedRequest.class));
                if (event != null && Event.Type.CREATE.equals(event.getEventType())) {
                    request = event.getData();
                }
            } catch (Exception ignore) {
                // fall through
            }

            // 하위호환: 기존에 토픽에 쌓인 "DTO 단독" 메시지도 처리 (CREATE로 간주)
            if (request == null) {
                try {
                    request = objectMapper.readValue(message.getPayload(), ClinicalCompletedRequest.class);
                } catch (Exception e) {
                    log.warn("[진료→수납][Kafka] 메시지 파싱 실패 message={}", e.getMessage());
                    return;
                }
            }
            if (request == null) {
                return;
            }

            ClinicalCompletedResult result = billingFacade.handleClinicalCompleted(request);
            log.info(
                    "[진료→수납][Kafka] 청구 생성 처리 완료 eventId={} visitId={} alreadyProcessed={} billId={}",
                    request.getEventId(),
                    request.getVisitId(),
                    result != null && result.isAlreadyProcessed(),
                    result != null ? result.getBillId() : null
            );
        };
    }
}

