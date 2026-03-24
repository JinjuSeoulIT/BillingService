package com.hospital.billing.toss.client;

import com.hospital.billing.toss.config.TossPaymentProperties;
import com.hospital.billing.toss.dto.TossApproveRequest;
import com.hospital.billing.toss.dto.TossApproveResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class TossPaymentClient {

    private static final Logger log = LoggerFactory.getLogger(TossPaymentClient.class);

    private final TossPaymentProperties tossPaymentProperties;
    private final RestTemplate restTemplate;

    public TossPaymentClient(TossPaymentProperties tossPaymentProperties) {
        this.tossPaymentProperties = tossPaymentProperties;
        this.restTemplate = new RestTemplate();
    }

    public TossApproveResponse confirmPayment(TossApproveRequest request) {
        String url = tossPaymentProperties.getBaseUrl() + "/v1/payments/confirm";

        log.info("[toss] confirmPayment start");
        log.info("[toss] url={}", url);
        log.info("[toss] orderId={}", request.getOrderId());
        log.info("[toss] amount={}", request.getAmount());
        log.info("[toss] paymentKey={}", maskPaymentKey(request.getPaymentKey()));
        log.info("[toss] secretKeyLoaded={}",
                tossPaymentProperties.getSecretKey() != null && !tossPaymentProperties.getSecretKey().isBlank());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(createEncodedSecretKey());

        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", request.getPaymentKey());
        body.put("orderId", request.getOrderId());
        body.put("amount", request.getAmount());

        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    httpEntity,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();

            log.info("[toss] confirmPayment success status={}", response.getStatusCode());

            if (responseBody == null) {
                throw new RuntimeException("토스 승인 응답이 비어 있습니다.");
            }

            TossApproveResponse approveResponse = new TossApproveResponse();
            approveResponse.setPaymentKey((String) responseBody.get("paymentKey"));
            approveResponse.setOrderId((String) responseBody.get("orderId"));
            approveResponse.setStatus((String) responseBody.get("status"));
            approveResponse.setMethod((String) responseBody.get("method"));

            Object totalAmount = responseBody.get("totalAmount");
            if (totalAmount instanceof Number number) {
                approveResponse.setAmount(number.longValue());
            }

            return approveResponse;

        } catch (HttpStatusCodeException e) {
            log.error("[toss] confirmPayment http error status={}", e.getStatusCode());
            log.error("[toss] confirmPayment response body={}", e.getResponseBodyAsString());
            log.error("[toss] confirmPayment response headers={}", e.getResponseHeaders());
            throw e;
        } catch (Exception e) {
            log.error("[toss] confirmPayment unexpected error", e);
            throw e;
        }
    }

    private String createEncodedSecretKey() {
        String secretKey = tossPaymentProperties.getSecretKey();
        String value = secretKey + ":";
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String maskPaymentKey(String paymentKey) {
        if (paymentKey == null || paymentKey.length() < 8) {
            return paymentKey;
        }
        return paymentKey.substring(0, 6) + "..." + paymentKey.substring(paymentKey.length() - 4);
    }
}