package com.hospital.billing.facade;

import com.hospital.billing.dto.integration.ClinicalClaimItemRequest;
import com.hospital.billing.dto.integration.ClinicalCompletedRequest;
import com.hospital.billing.dto.integration.ClinicalCompletedResult;
import com.hospital.billing.entity.Bill;
import com.hospital.billing.entity.BillItem;
import com.hospital.billing.entity.BillItemSource;
import com.hospital.billing.entity.BillingRequest;
import com.hospital.billing.entity.BillingStatus;
import com.hospital.billing.repository.BillItemRepository;
import com.hospital.billing.repository.BillItemSourceRepository;
import com.hospital.billing.repository.BillRepository;
import com.hospital.billing.service.BillingRequestService;
import com.hospital.billing.service.NoSequenceService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Component
public class BillingFacade {

    private static final String FIXED_SOURCE_TYPE = "CLINICAL_ORDER_ITEM";
    private static final String REQUIRED_STATUS = "COMPLETED";

    private static final String ORDER_TYPE_PRESCRIPTION = "PRESCRIPTION";
    private static final String ORDER_TYPE_PROCEDURE = "PROCEDURE";
    private static final String ORDER_TYPE_IMAGING = "IMAGING";
    private static final String ORDER_TYPE_LAB = "LAB";
    private static final String ORDER_TYPE_MEDICATION = "MEDICATION";
    private static final String ORDER_TYPE_TREATMENT = "TREATMENT";
    private static final String ORDER_TYPE_SPECIMEN = "SPECIMEN";
    private static final String ORDER_TYPE_PATHOLOGY = "PATHOLOGY";
    private static final String ORDER_TYPE_ENDOSCOPY = "ENDOSCOPY";
    private static final String ORDER_TYPE_PHYSIOLOGICAL = "PHYSIOLOGICAL";

    private static final Set<String> ALLOWED_ORDER_TYPES = Set.of(
            ORDER_TYPE_PRESCRIPTION,
            ORDER_TYPE_PROCEDURE,
            ORDER_TYPE_IMAGING,
            ORDER_TYPE_LAB,
            ORDER_TYPE_MEDICATION,
            ORDER_TYPE_TREATMENT,
            ORDER_TYPE_SPECIMEN,
            ORDER_TYPE_PATHOLOGY,
            ORDER_TYPE_ENDOSCOPY,
            ORDER_TYPE_PHYSIOLOGICAL
    );

    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final BillItemSourceRepository billItemSourceRepository;
    private final BillingRequestService billingRequestService;
    private final NoSequenceService noSequenceService;

    public BillingFacade(BillRepository billRepository,
                         BillItemRepository billItemRepository,
                         BillItemSourceRepository billItemSourceRepository,
                         BillingRequestService billingRequestService,
                         NoSequenceService noSequenceService) {
        this.billRepository = billRepository;
        this.billItemRepository = billItemRepository;
        this.billItemSourceRepository = billItemSourceRepository;
        this.billingRequestService = billingRequestService;
        this.noSequenceService = noSequenceService;
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

            // 추가: 프로시저로 청구번호 생성
            String billingNo = noSequenceService.getNextNo("BILLING_NO");
            bill.setBillingNo(billingNo);

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
                        tempItem.getItemCategory(),
                        tempItem.getQuantity(),
                        tempItem.getUnitPrice(),
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

    @Transactional
    public OutcomeAppliedResult handleMedicalSupportOutcome(OutcomeCharge charge) {
        if (charge == null) {
            throw new IllegalArgumentException("요청 값이 없습니다.");
        }
        if (charge.patientId() == null || charge.patientId() <= 0L) {
            throw new IllegalArgumentException("patientId는 필수입니다.");
        }
        if (isBlank(charge.sourceType())) {
            throw new IllegalArgumentException("sourceType은 필수입니다.");
        }
        if (charge.sourceId() == null || charge.sourceId() <= 0L) {
            throw new IllegalArgumentException("sourceId는 필수입니다.");
        }

        boolean alreadyProcessed = billItemSourceRepository.existsBySourceTypeAndSourceId(
                charge.sourceType(),
                charge.sourceId()
        );
        if (alreadyProcessed) {
            return new OutcomeAppliedResult(null, true);
        }

        Bill bill = null;
        Long visitId = charge.visitId();
        if (visitId != null && visitId > 0L) {
            bill = billRepository.findByVisitId(visitId).orElse(null);
        }
        if (bill == null) {
            bill = billRepository.findByPatientIdAndStatus(charge.patientId(), BillingStatus.READY).stream()
                    .max(Comparator.comparing(Bill::getId))
                    .orElse(null);
        }

        if (bill == null) {
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            bill = new Bill(charge.patientId(), now, 0, now);
            bill.setBillingNo(noSequenceService.getNextNo("BILLING_NO"));
            if (visitId != null && visitId > 0L) {
                bill.setVisitId(visitId);
            }
            bill = billRepository.save(bill);
        } else if (bill.getVisitId() == null && visitId != null && visitId > 0L) {
            // 기존 READY bill을 재사용하는 경우에도 visitId가 확인되면 연결
            bill.setVisitId(visitId);
            bill = billRepository.save(bill);
        }

        int quantity = charge.quantity() != null && charge.quantity() > 0 ? charge.quantity() : 1;
        int unitPrice = charge.unitPrice() != null && charge.unitPrice() > 0
                ? charge.unitPrice()
                : resolveUnitPriceByItemCategory(charge.itemCategory());
        int amount = quantity * unitPrice;

        BillItem billItem = BillItem.create(
                bill,
                isBlank(charge.itemName()) ? "미정" : charge.itemName().trim(),
                isBlank(charge.itemCategory()) ? "ETC" : charge.itemCategory().trim(),
                quantity,
                unitPrice,
                amount
        );
        BillItem savedBillItem = billItemRepository.save(billItem);

        BillItemSource source = BillItemSource.create(
                savedBillItem,
                visitId != null && visitId > 0L ? visitId : bill.getVisitId(),
                charge.sourceType().trim(),
                charge.sourceId(),
                charge.sourceEventId(),
                Timestamp.valueOf(LocalDateTime.now())
        );
        billItemSourceRepository.save(source);

        // bill 총액/잔액 누적 (paid는 그대로)
        int newTotal = (bill.getTotalAmount() == null ? 0 : bill.getTotalAmount()) + amount;
        int newRemaining = (bill.getRemainingAmount() == null ? 0 : bill.getRemainingAmount()) + amount;
        bill.setTotalAmount(newTotal);
        bill.setRemainingAmount(newRemaining);
        billRepository.save(bill);

        return new OutcomeAppliedResult(bill.getId(), false);
    }

    private int resolveUnitPriceByItemCategory(String itemCategory) {
        String normalized = normalizeUpperText(itemCategory);
        if (normalized == null) {
            return 1000;
        }
        switch (normalized) {
            case "MEDICATION":
                return 10000;
            case "PROCEDURE":
                return 15000;
            case "TEST":
                return 5000;
            default:
                return 1000;
        }
    }

    public record OutcomeCharge(
            Long patientId,
            Long visitId,
            String itemName,
            String itemCategory,
            Integer quantity,
            Integer unitPrice,
            String sourceType,
            Long sourceId,
            String sourceEventId
    ) {
    }

    public record OutcomeAppliedResult(
            Long billId,
            boolean alreadyProcessed
    ) {
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

        if (request.getVisitId() <= 0L) {
            throw new IllegalArgumentException("visitId는 0보다 커야 합니다.");
        }

        if (request.getPatientId() == null) {
            throw new IllegalArgumentException("patientId는 필수입니다.");
        }

        if (request.getPatientId() <= 0L) {
            throw new IllegalArgumentException("patientId는 0보다 커야 합니다.");
        }

        String normalizedStatus = normalizeText(request.getStatus());
        if (normalizedStatus == null) {
            throw new IllegalArgumentException("status는 필수입니다.");
        }

        if (!REQUIRED_STATUS.equalsIgnoreCase(normalizedStatus)) {
            throw new IllegalArgumentException("status는 COMPLETED만 허용됩니다.");
        }

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
            if (requestItem.getSourceId() <= 0L) {
                throw new IllegalArgumentException("청구 항목의 sourceId는 0보다 커야 합니다.");
            }

            String resolvedItemName = resolveItemName(
                    requestItem.getItemName(),
                    requestItem.getItemCode()
            );
            String resolvedOrderType = resolveOrderType(requestItem.getOrderType());
            String resolvedSourceType = resolveSourceType(requestItem.getSourceType());

            String resolvedItemCategory = resolveItemCategory(resolvedOrderType);
            int resolvedQuantity = resolveQuantity(requestItem);
            int resolvedUnitPrice = resolveUnitPriceByOrderType(resolvedOrderType);
            int resolvedAmount = resolvedQuantity * resolvedUnitPrice;

            TempBillItem tempItem = new TempBillItem(
                    resolvedItemName,
                    resolvedItemCategory,
                    resolvedQuantity,
                    resolvedUnitPrice,
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

    private String resolveOrderType(String orderType) {
        String normalizedOrderType = normalizeUpperText(orderType);

        if (normalizedOrderType == null) {
            throw new IllegalArgumentException("청구 항목의 orderType은 필수입니다.");
        }

        if (!ALLOWED_ORDER_TYPES.contains(normalizedOrderType)) {
            throw new IllegalArgumentException(
                    "허용되지 않은 orderType입니다. allowed=" + ALLOWED_ORDER_TYPES + ", input=" + orderType
            );
        }

        return normalizedOrderType;
    }


    // sourceType은 현재 합의 기준으로 CLINICAL_ORDER_ITEM 고정
    // clinical에서 값이 오더라도 billing 기준으로 한 번 고정해줌
    private String resolveSourceType(String sourceType) {
        String normalizedSourceType = normalizeUpperText(sourceType);

        if (normalizedSourceType == null) {
            return FIXED_SOURCE_TYPE;
        }

        if (!FIXED_SOURCE_TYPE.equals(normalizedSourceType)) {
            throw new IllegalArgumentException(
                    "sourceType은 CLINICAL_ORDER_ITEM만 허용됩니다. input=" + sourceType
            );
        }

        return FIXED_SOURCE_TYPE;
    }


    // [추가] orderType -> 화면용 항목 분류 매핑
    private String resolveItemCategory(String orderType) {
        switch (orderType) {
            case ORDER_TYPE_PRESCRIPTION:
            case ORDER_TYPE_MEDICATION:
                return "MEDICATION";
            case ORDER_TYPE_LAB:
            case ORDER_TYPE_IMAGING:
            case ORDER_TYPE_SPECIMEN:
            case ORDER_TYPE_PATHOLOGY:
            case ORDER_TYPE_ENDOSCOPY:
            case ORDER_TYPE_PHYSIOLOGICAL:
                return "TEST";
            case ORDER_TYPE_PROCEDURE:
            case ORDER_TYPE_TREATMENT:
                return "PROCEDURE";
            default:
                return "ETC";
        }
    }

    // [추가] 현재 임시 규칙에서는 수량 1건으로 고정
    private int resolveQuantity(ClinicalClaimItemRequest requestItem) {
        return 1;
    }

    // [추가] orderType 기준 임시 단가
    private int resolveUnitPriceByOrderType(String orderType) {
        switch (orderType) {
            case ORDER_TYPE_PRESCRIPTION:
            case ORDER_TYPE_MEDICATION:
                return 10000;
            case ORDER_TYPE_PROCEDURE:
            case ORDER_TYPE_TREATMENT:
                return 15000;
            case ORDER_TYPE_IMAGING:
                return 20000;
            case ORDER_TYPE_LAB:
            case ORDER_TYPE_SPECIMEN:
            case ORDER_TYPE_PATHOLOGY:
            case ORDER_TYPE_ENDOSCOPY:
            case ORDER_TYPE_PHYSIOLOGICAL:
                return 5000;
            default:
                throw new IllegalArgumentException("허용되지 않은 orderType입니다. input=" + orderType);
        }
    }

    private String normalizeText(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private String normalizeUpperText(String value) {
        String normalized = normalizeText(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private static class TempBillItem {
        private final String itemName;
        private final String itemCategory;
        private final Integer quantity;
        private final Integer unitPrice;
        private final Integer amount;
        private final String sourceType;
        private final Long sourceId;

        public TempBillItem(String itemName,
                            String itemCategory,
                            Integer quantity,
                            Integer unitPrice,
                            Integer amount,
                            String sourceType,
                            Long sourceId) {
            this.itemName = itemName;
            this.itemCategory = itemCategory;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.amount = amount;
            this.sourceType = sourceType;
            this.sourceId = sourceId;
        }

        public String getItemName() {
            return itemName;
        }

        public String getItemCategory() {
            return itemCategory;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public Integer getUnitPrice() {
            return unitPrice;
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