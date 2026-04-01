package com.hospital.billing.facade;

import com.hospital.billing.dto.integration.ClinicalClaimItemRequest;
import com.hospital.billing.dto.integration.ClinicalCompletedRequest;
import com.hospital.billing.dto.integration.ClinicalCompletedResult;
import com.hospital.billing.entity.Bill;
import com.hospital.billing.entity.BillItem;
import com.hospital.billing.entity.BillItemSource;
import com.hospital.billing.entity.BillingRequest;
import com.hospital.billing.repository.BillItemRepository;
import com.hospital.billing.repository.BillItemSourceRepository;
import com.hospital.billing.repository.BillRepository;
import com.hospital.billing.service.BillingRequestService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class BillingFacade {

    private static final String FIXED_SOURCE_TYPE = "CLINICAL_ORDER_ITEM";

    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final BillItemSourceRepository billItemSourceRepository;
    private final BillingRequestService billingRequestService;

    public BillingFacade(BillRepository billRepository,
                         BillItemRepository billItemRepository,
                         BillItemSourceRepository billItemSourceRepository,
                         BillingRequestService billingRequestService) {
        this.billRepository = billRepository;
        this.billItemRepository = billItemRepository;
        this.billItemSourceRepository = billItemSourceRepository;
        this.billingRequestService = billingRequestService;
    }

    @Transactional
    public ClinicalCompletedResult handleClinicalCompleted(ClinicalCompletedRequest request) {

        // eventId는 요청 이력 저장과 중복 체크의 기준이므로 가장 먼저 최소 검증
        validateEventIdOnly(request);

        // 기존 요청 이력 기준 중복 확인
        BillingRequest existingRequest = billingRequestService
                .findByEventId(request.getEventId())
                .orElse(null);

        if (existingRequest != null) {
            if (existingRequest.getBillId() != null) {
                return new ClinicalCompletedResult(existingRequest.getBillId(), true);
            }

            Bill existingBillByEventId = billRepository
                    .findBySourceEventId(request.getEventId())
                    .orElse(null);

            if (existingBillByEventId != null) {
                billingRequestService.markSuccess(
                        existingRequest.getId(),
                        existingBillByEventId.getId()
                );
                return new ClinicalCompletedResult(existingBillByEventId.getId(), true);
            }

            throw new IllegalStateException("이미 접수된 요청입니다. eventId=" + request.getEventId());
        }

        // 유효성 전체 검증 전에 요청 원본/처리 이력을 먼저 남김
        BillingRequest billingRequest = billingRequestService.saveReceived(request);

        try {
            // 검증 실패해도 catch로 들어가서 BILLING_REQUEST를 FAILED로 남길 수 있음
            validateRequest(request);

            // 1. eventId 기준 기존 bill 존재 여부 확인
            Bill existingByEventId = billRepository
                    .findBySourceEventId(request.getEventId())
                    .orElse(null);

            if (existingByEventId != null) {
                billingRequestService.markSuccess(
                        billingRequest.getId(),
                        existingByEventId.getId()
                );
                return new ClinicalCompletedResult(existingByEventId.getId(), true);
            }

            // 2. visitId 기준 기존 bill 존재 여부 확인
            Bill existingByVisitId = billRepository
                    .findByVisitId(request.getVisitId())
                    .orElse(null);

            if (existingByVisitId != null) {
                billingRequestService.markSuccess(
                        billingRequest.getId(),
                        existingByVisitId.getId()
                );
                return new ClinicalCompletedResult(existingByVisitId.getId(), true);
            }

            // [핵심 변경]
            // clinical에서 전달받은 items를 billing 저장용 임시 모델로 변환
            List<TempBillItem> tempItems = convertRequestItems(request.getItems());

            // 총 금액 계산
            int totalAmount = tempItems.stream()
                    .mapToInt(TempBillItem::getAmount)
                    .sum();

            Timestamp treatmentDate = toTimestamp(request.getOccurredAt());
            Timestamp createdAt = Timestamp.valueOf(LocalDateTime.now());

            // 3. Bill 생성
            Bill bill = new Bill(
                    request.getPatientId(),
                    treatmentDate,
                    totalAmount,
                    createdAt
            );

            // 4. 연동 식별값 세팅
            bill.setVisitId(request.getVisitId());
            bill.setSourceEventId(request.getEventId());

            // 5. Bill 저장
            Bill savedBill = billRepository.save(bill);

            // 6. BillItem 생성 후 저장
            List<BillItem> billItems = new ArrayList<>();
            for (TempBillItem tempItem : tempItems) {
                BillItem billItem = BillItem.create(
                        savedBill,
                        tempItem.getItemName(),
                        tempItem.getAmount()
                );
                billItems.add(billItem);
            }

            List<BillItem> savedBillItems = billItemRepository.saveAll(billItems);

            // 7. BILL_ITEM_SOURCE 저장
            List<BillItemSource> billItemSources = new ArrayList<>();
            for (int i = 0; i < savedBillItems.size(); i++) {
                BillItem savedBillItem = savedBillItems.get(i);
                TempBillItem tempItem = tempItems.get(i);

                BillItemSource billItemSource = BillItemSource.create(
                        savedBillItem,
                        request.getVisitId(),
                        tempItem.getSourceType(),
                        tempItem.getSourceId(),
                        request.getEventId(),
                        Timestamp.valueOf(LocalDateTime.now())
                );
                billItemSources.add(billItemSource);
            }
            billItemSourceRepository.saveAll(billItemSources);

            // 8. 성공 상태 업데이트
            billingRequestService.markSuccess(
                    billingRequest.getId(),
                    savedBill.getId()
            );

            // 9. 결과 반환
            return new ClinicalCompletedResult(savedBill.getId(), false);

        } catch (Exception e) {
            // 실패 상태 업데이트는 별도 트랜잭션으로 처리
            billingRequestService.markFailed(
                    billingRequest.getId(),
                    e.getMessage()
            );
            throw e;
        }
    }

    // 요청 이력 저장 이전에 꼭 필요한 최소 검증만 수행
    private void validateEventIdOnly(ClinicalCompletedRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("요청 값이 없습니다.");
        }

        if (isBlank(request.getEventId())) {
            throw new IllegalArgumentException("eventId는 필수입니다.");
        }
    }

    private void validateRequest(ClinicalCompletedRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("요청 값이 없습니다.");
        }

        if (isBlank(request.getEventId())) {
            throw new IllegalArgumentException("eventId는 필수입니다.");
        }

        if (request.getVisitId() == null) {
            throw new IllegalArgumentException("visitId는 필수입니다.");
        }

        if (request.getPatientId() == null) {
            throw new IllegalArgumentException("patientId는 필수입니다.");
        }

        if (isBlank(request.getStatus())) {
            throw new IllegalArgumentException("status는 필수입니다.");
        }

        // [추가]
        // items 빈 배열은 clinical 쪽에서는 허용 가능하다고 했지만,
        // billing에서는 실제 청구 항목이 없으면 bill 생성 실패로 처리하는 쪽이 더 안전함
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("청구 항목(items)은 최소 1건 이상 필요합니다.");
        }
    }

    private Timestamp toTimestamp(LocalDateTime occurredAt) {
        LocalDateTime baseTime = Objects.requireNonNullElseGet(
                occurredAt,
                LocalDateTime::now
        );
        return Timestamp.valueOf(baseTime);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    // [추가]
    // clinical claims items -> billing 저장용 임시 모델 변환
    private List<TempBillItem> convertRequestItems(List<ClinicalClaimItemRequest> requestItems) {
        List<TempBillItem> result = new ArrayList<>();

        for (ClinicalClaimItemRequest requestItem : requestItems) {
            if (requestItem == null) {
                continue;
            }

            if (requestItem.getSourceId() == null) {
                throw new IllegalArgumentException("청구 항목의 sourceId는 필수입니다.");
            }

            String resolvedItemName = resolveItemName(
                    requestItem.getItemName(),
                    requestItem.getItemCode()
            );

            String resolvedOrderType = normalizeText(requestItem.getOrderType());
            String resolvedSourceType = resolveSourceType(requestItem.getSourceType());

            int resolvedAmount = resolveAmountByOrderType(resolvedOrderType);

            TempBillItem tempItem = new TempBillItem(
                    resolvedItemName,
                    resolvedAmount,
                    resolvedSourceType,
                    requestItem.getSourceId()
            );

            result.add(tempItem);
        }

        if (result.isEmpty()) {
            throw new IllegalArgumentException("유효한 청구 항목이 없습니다.");
        }

        return result;
    }

    // [추가]
    // itemName 비어 있으면 itemCode, 그것도 없으면 '미정'
    private String resolveItemName(String itemName, String itemCode) {
        if (!isBlank(itemName)) {
            return itemName.trim();
        }

        if (!isBlank(itemCode)) {
            return itemCode.trim();
        }

        return "미정";
    }

    // [추가]
    // sourceType은 현재 합의 기준으로 CLINICAL_ORDER_ITEM 고정
    // clinical에서 값이 오더라도 billing 기준으로 한 번 고정해줌
    private String resolveSourceType(String sourceType) {
        return FIXED_SOURCE_TYPE;
    }

    // [추가]
    // amount는 아직 clinical DB에 없으므로 billing 임시 규칙 사용
    // 추후 단가/수가 정책 확정 시 이 메서드만 교체하면 됨
    private int resolveAmountByOrderType(String orderType) {
        if (isBlank(orderType)) {
            return 5000;
        }

        switch (orderType.trim().toUpperCase()) {
            case "PRESCRIPTION":
                return 10000;
            case "BLOOD":
                return 20000;
            default:
                return 5000;
        }
    }

    private String normalizeText(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private static class TempBillItem {
        private final String itemName;
        private final Integer amount;
        private final String sourceType;
        private final Long sourceId;

        public TempBillItem(String itemName,
                            Integer amount,
                            String sourceType,
                            Long sourceId) {
            this.itemName = itemName;
            this.amount = amount;
            this.sourceType = sourceType;
            this.sourceId = sourceId;
        }

        public String getItemName() {
            return itemName;
        }

        public Integer getAmount() {
            return amount;
        }

        public String getSourceType() {
            return sourceType;
        }

        public Long getSourceId() {
            return sourceId;
        }
    }
}