package com.hospital.billing.messaging;

import com.hospital.billing.dto.integration.ClinicalCompletedRequest;
import com.hospital.billing.dto.integration.ClinicalCompletedResult;
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
public class ClinicalCompletedConsumerConfig {

    private final BillingFacade billingFacade;

    @Bean
    public Consumer<Message<Event<Object, ClinicalCompletedRequest>>> clinicalCompletedConsumer() {
        return message -> {
            if (message == null || message.getPayload() == null) {
                return;
            }
            Event<Object, ClinicalCompletedRequest> event = message.getPayload();
            if (event.getEventType() == null || event.getData() == null) {
                return;
            }
            if (!Event.Type.CREATE.equals(event.getEventType())) {
                return;
            }
            ClinicalCompletedRequest request = event.getData();

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

