package com.hospital.billing.service;

import com.hospital.billing.dto.BillDetailResponse;
import com.hospital.billing.dto.BillHistoryResponse;
import com.hospital.billing.dto.BillItemResponse;
import com.hospital.billing.dto.BillStatusResponse;
import com.hospital.billing.dto.BillSummaryResponse;
import com.hospital.billing.dto.BillingStatsResponse;
import com.hospital.billing.dto.CalculatedBillResponse;
import com.hospital.billing.entity.Bill;
import com.hospital.billing.entity.BillHistory;
import com.hospital.billing.entity.BillItem;
import com.hospital.billing.entity.BillingStatus;
import com.hospital.billing.payment.entity.Payment;
import com.hospital.billing.payment.entity.PaymentStatus;
import com.hospital.billing.repository.BillHistoryRepository;
import com.hospital.billing.repository.BillItemRepository;
import com.hospital.billing.repository.BillRepository;
import com.hospital.billing.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class BillingService {

    private static final Logger log =
            LoggerFactory.getLogger(BillingService.class);

    private static final String STAFF_TABLE_OWNER = "CMH";
    private static final String STAFF_TABLE_NAME = "STAFF";
    private static final String STAFF_ID_COLUMN = "STAFF_ID";

    private static final List<String> STAFF_NAME_COLUMN_CANDIDATES = List.of(
            "FULL_NAME",
            "STAFF_NAME",
            "NAME",
            "EMP_NAME",
            "USER_NAME",
            "KOR_NAME",
            "STAFF_NM"
    );

    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final BillItemRepository billItemRepository;
    private final BillHistoryRepository billHistoryRepository;
    private final JdbcTemplate jdbcTemplate;

    // [추가] 이름 조회 캐시
    private final ConcurrentMap<String, String> staffNameCache = new ConcurrentHashMap<>();
    private volatile String resolvedStaffNameColumn;

    public BillingService(BillRepository billRepository,
                          PaymentRepository paymentRepository,
                          BillItemRepository billItemRepository,
                          BillHistoryRepository billHistoryRepository,
                          JdbcTemplate jdbcTemplate) {
        this.billRepository = billRepository;
        this.paymentRepository = paymentRepository;
        this.billItemRepository = billItemRepository;
        this.billHistoryRepository = billHistoryRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public Bill confirm(Long billId, String staffId) {

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new IllegalArgumentException("청구 정보를 찾을 수 없습니다."));

        if (bill.getStatus() == BillingStatus.CANCELED) {
            throw new IllegalStateException("취소된 청구는 확정할 수 없습니다.");
        }

        if (bill.getStatus() == BillingStatus.CONFIRMED) {
            throw new IllegalStateException("이미 확정된 청구입니다.");
        }

        validateCalculatedAmountForConfirm(bill);

        if (bill.getRemainingAmount() == null || bill.getRemainingAmount() > 0) {
            throw new IllegalStateException("완납된 청구만 확정할 수 있습니다.");
        }

        if (staffId == null || staffId.isBlank()) {
            throw new IllegalArgumentException("직원 ID가 필요합니다.");
        }

        BillingStatus oldStatus = bill.getStatus();

        bill.setStatus(BillingStatus.CONFIRMED);

        Bill savedBill = billRepository.save(bill);

        saveBillHistory(
                savedBill,
                oldStatus,
                BillingStatus.CONFIRMED,
                staffId,
                "청구 확정"
        );

        return savedBill;
    }

    @Transactional
    public Bill cancel(Long billId, String staffId) {

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new IllegalArgumentException("청구 정보를 찾을 수 없습니다."));

        if (bill.getStatus() != BillingStatus.CONFIRMED
                || bill.getRemainingAmount() == null
                || bill.getRemainingAmount() > 0) {
            throw new IllegalStateException("청구 확정된 건만 취소할 수 있습니다.");
        }

        if (staffId == null || staffId.isBlank()) {
            throw new IllegalArgumentException("직원 ID가 필요합니다.");
        }

        BillingStatus oldStatus = bill.getStatus();

        bill.setStatus(BillingStatus.CANCELED);

        Bill savedBill = billRepository.save(bill);

        saveBillHistory(
                savedBill,
                oldStatus,
                BillingStatus.CANCELED,
                staffId,
                "청구 취소"
        );

        return savedBill;
    }

    @Transactional
    public Bill unconfirm(Long billId, String staffId) {

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new IllegalArgumentException("청구 정보를 찾을 수 없습니다."));

        if (bill.getStatus() == BillingStatus.CANCELED) {
            throw new IllegalStateException("취소된 청구는 확정 해제할 수 없습니다.");
        }

        if (bill.getStatus() != BillingStatus.CONFIRMED
                || bill.getRemainingAmount() == null
                || bill.getRemainingAmount() > 0) {
            throw new IllegalStateException("청구 확정된 건만 확정 해제할 수 있습니다.");
        }

        if (staffId == null || staffId.isBlank()) {
            throw new IllegalArgumentException("직원 ID가 필요합니다.");
        }

        BillingStatus oldStatus = bill.getStatus();

        bill.setStatus(BillingStatus.PAID);

        Bill savedBill = billRepository.save(bill);

        saveBillHistory(
                savedBill,
                oldStatus,
                BillingStatus.PAID,
                staffId,
                "청구 확정 해제"
        );

        return savedBill;
    }

    @Transactional
    public Bill restore(Long billId, String staffId) {

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new IllegalArgumentException("청구 정보를 찾을 수 없습니다."));

        if (bill.getStatus() != BillingStatus.CANCELED) {
            throw new IllegalStateException("취소된 청구만 복원할 수 있습니다.");
        }

        if (bill.getRemainingAmount() == null || bill.getRemainingAmount() > 0) {
            throw new IllegalStateException("완납된 취소 청구만 복원할 수 있습니다.");
        }

        if (staffId == null || staffId.isBlank()) {
            throw new IllegalArgumentException("직원 ID가 필요합니다.");
        }

        BillingStatus oldStatus = bill.getStatus();

        bill.setStatus(BillingStatus.PAID);

        Bill savedBill = billRepository.save(bill);

        saveBillHistory(
                savedBill,
                oldStatus,
                BillingStatus.PAID,
                staffId,
                "청구 복원"
        );

        return savedBill;
    }

    @Transactional(readOnly = true)
    public BillDetailResponse getBillDetail(Long billId) {

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new IllegalArgumentException("청구 정보가 없습니다."));

        List<BillItemResponse> billItems = getBillItemDetails(billId);

        List<Object[]> paymentSummaries =
                paymentRepository.findPaymentStatusAndAmountByBillId(bill.getId());

        BillingStatus calculatedStatus =
                calculateBillingStatus(bill, paymentSummaries);

        System.out.println(
                "[bill-detail] billId=" + bill.getId()
                        + ", paidAmount=" + bill.getPaidAmount()
                        + ", remainingAmount=" + bill.getRemainingAmount()
                        + ", storedStatus=" + bill.getStatus()
                        + ", calculatedStatus=" + calculatedStatus
        );

        return new BillDetailResponse(bill, calculatedStatus, billItems);
    }

    @Transactional(readOnly = true)
    public CalculatedBillResponse getCalculatedBill(Long billId) {

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new IllegalArgumentException("청구 정보를 찾을 수 없습니다. billId=" + billId));

        List<BillItem> billItems = billItemRepository.findByBillId(billId);

        int originalAmount = safeAmount(bill.getTotalAmount());

        int calculatedAmount = billItems.stream()
                .map(BillItem::getAmount)
                .filter(amount -> amount != null)
                .mapToInt(Integer::intValue)
                .sum();

        String calculationNote;
        if (billItems.isEmpty()) {
            calculatedAmount = originalAmount;
            calculationNote = "청구 항목이 없어 청구 총액 기준으로 계산했습니다.";
        } else if (originalAmount == calculatedAmount) {
            calculationNote = "청구 항목 합계 기준 자동 계산 금액입니다.";
        } else {
            calculationNote = "청구 총액과 항목 합계가 달라 항목 합계 기준 자동 계산 금액을 조회했습니다.";
        }

        return new CalculatedBillResponse(
                billId,
                originalAmount,
                calculatedAmount,
                calculationNote
        );
    }

    @Transactional(readOnly = true)
    public List<BillHistoryResponse> getBillHistory(Long billId) {

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new IllegalArgumentException("청구 정보를 찾을 수 없습니다. billId=" + billId));

        List<Payment> payments = paymentRepository.findByBill_IdOrderByPaidAtDesc(billId);
        List<BillHistory> billHistories = billHistoryRepository.findByBill_IdOrderByChangedAtDesc(billId);

        List<BillHistoryResponse> histories = new ArrayList<>();

        histories.add(new BillHistoryResponse(
                resolveBillOccurredAt(bill),
                "BILL_CREATED",
                "청구 생성",
                "청구가 생성되었습니다.",
                safeAmount(bill.getTotalAmount()),
                null,
                null
        ));

        for (Payment payment : payments) {
            PaymentStatus status = payment.getStatus();
            int amount = safeAmount(payment.getPaymentAmount());
            LocalDateTime occurredAt = toLocalDateTime(payment.getPaidAt());

            if (status == PaymentStatus.COMPLETED) {
                histories.add(new BillHistoryResponse(
                        occurredAt,
                        "PAYMENT_COMPLETED",
                        "수납 완료",
                        "수납이 완료되었습니다.",
                        amount,
                        payment.getCreatedBy(),
                        resolveStaffName(payment.getCreatedBy())
                ));
            } else if (status == PaymentStatus.CANCELED) {
                histories.add(new BillHistoryResponse(
                        occurredAt,
                        "PAYMENT_CANCELED",
                        "수납 취소",
                        "수납이 취소되었습니다.",
                        amount,
                        payment.getCanceledBy(),
                        resolveStaffName(payment.getCanceledBy())
                ));
            } else if (status == PaymentStatus.REFUNDED) {
                histories.add(new BillHistoryResponse(
                        occurredAt,
                        "PAYMENT_REFUNDED",
                        "부분 환불",
                        "부분 환불이 처리되었습니다.",
                        amount,
                        payment.getCreatedBy(),
                        resolveStaffName(payment.getCreatedBy())
                ));
            }
        }

        for (BillHistory history : billHistories) {
            histories.add(toBillHistoryResponse(history, bill));
        }

        histories.sort(
                Comparator.comparing(
                                BillHistoryResponse::getOccurredAt,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        )
                        .reversed()
        );

        return histories;
    }

    @Transactional(readOnly = true)
    public List<BillItemResponse> getBillItemDetails(Long billId) {
        List<BillItem> billItems = billItemRepository.findByBillId(billId);

        return billItems.stream()
                .map(BillItemResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BillSummaryResponse> getBillsByPatient(Long patientId,
                                                       BillingStatus status,
                                                       boolean confirmedOnly,
                                                       boolean partialOnly) {
        List<Bill> bills = billRepository.findByPatientId(patientId);
        Map<Long, PaymentRepository.BillPaymentSummaryProjection> paymentSummaryMap =
                getPaymentSummaryMap(bills);

        return bills.stream()
                .map(bill -> {
                    PaymentAmounts paymentAmounts = extractPaymentAmounts(paymentSummaryMap.get(bill.getId()));

                    BillingStatus calculatedStatus =
                            calculateBillingStatus(
                                    bill,
                                    paymentAmounts.completedAmount(),
                                    paymentAmounts.refundedAmount()
                            );

                    return new BillSummaryResponse(bill, calculatedStatus);
                })
                .filter(response -> matchesStatusFilter(response, status, confirmedOnly, partialOnly))
                .toList();
    }

    /**
     * [수정] 전체 청구 목록 조회 + 일일 청구 조회 공통 처리
     */
    @Transactional(readOnly = true)
    public List<BillSummaryResponse> getBills(BillingStatus status,
                                              boolean confirmedOnly,
                                              boolean partialOnly,
                                              LocalDate billingDate) {

        List<Bill> bills = findBillsForList(billingDate);

        Map<Long, PaymentRepository.BillPaymentSummaryProjection> paymentSummaryMap =
                getPaymentSummaryMap(bills);

        return bills.stream()
                .map(bill -> {
                    PaymentAmounts paymentAmounts = extractPaymentAmounts(paymentSummaryMap.get(bill.getId()));

                    BillingStatus calculatedStatus =
                            calculateBillingStatus(
                                    bill,
                                    paymentAmounts.completedAmount(),
                                    paymentAmounts.refundedAmount()
                            );

                    return new BillSummaryResponse(bill, calculatedStatus);
                })
                .filter(response -> matchesStatusFilter(response, status, confirmedOnly, partialOnly))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BillSummaryResponse> getBillsByEncounter(Long encounterId) {
        List<Bill> bills = billRepository.findAllByVisitId(encounterId);
        Map<Long, PaymentRepository.BillPaymentSummaryProjection> paymentSummaryMap =
                getPaymentSummaryMap(bills);

        return bills.stream()
                .map(bill -> {
                    PaymentAmounts paymentAmounts = extractPaymentAmounts(paymentSummaryMap.get(bill.getId()));

                    BillingStatus calculatedStatus =
                            calculateBillingStatus(
                                    bill,
                                    paymentAmounts.completedAmount(),
                                    paymentAmounts.refundedAmount()
                            );

                    return new BillSummaryResponse(bill, calculatedStatus);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BillSummaryResponse> getOutstandingBills() {
        List<Bill> bills = billRepository.findByRemainingAmountGreaterThan(0);
        Map<Long, PaymentRepository.BillPaymentSummaryProjection> paymentSummaryMap =
                getPaymentSummaryMap(bills);

        return bills.stream()
                .map(bill -> {
                    PaymentAmounts paymentAmounts = extractPaymentAmounts(paymentSummaryMap.get(bill.getId()));

                    BillingStatus calculatedStatus =
                            calculateBillingStatus(
                                    bill,
                                    paymentAmounts.completedAmount(),
                                    paymentAmounts.refundedAmount()
                            );

                    return new BillSummaryResponse(bill, calculatedStatus);
                })
                .filter(response ->
                        response.getStatus() == BillingStatus.READY
                                || response.getStatus() == BillingStatus.CONFIRMED
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public BillingStatsResponse getStats() {

        List<Bill> bills = billRepository.findAll();
        Map<Long, PaymentRepository.BillPaymentSummaryProjection> paymentSummaryMap =
                getPaymentSummaryMap(bills);

        long readyCount = 0;
        long confirmedCount = 0;
        long paidCount = 0;
        long finalConfirmedCount = 0;

        for (Bill bill : bills) {
            PaymentAmounts paymentAmounts = extractPaymentAmounts(paymentSummaryMap.get(bill.getId()));

            BillingStatus calculatedStatus =
                    calculateBillingStatus(
                            bill,
                            paymentAmounts.completedAmount(),
                            paymentAmounts.refundedAmount()
                    );

            Integer remainingAmount = bill.getRemainingAmount();
            int safeRemainingAmount = remainingAmount == null ? 0 : remainingAmount;

            if (calculatedStatus == BillingStatus.READY) {
                readyCount++;
            } else if (calculatedStatus == BillingStatus.PAID) {
                paidCount++;
            } else if (calculatedStatus == BillingStatus.CONFIRMED) {
                if (safeRemainingAmount == 0) {
                    finalConfirmedCount++;
                } else if (safeRemainingAmount > 0) {
                    confirmedCount++;
                }
            }
        }

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        Timestamp start = Timestamp.valueOf(startOfDay);
        Timestamp end = Timestamp.valueOf(endOfDay);

        long todayCompleted =
                paymentRepository.sumTodayByStatus(
                        PaymentStatus.COMPLETED,
                        start,
                        end
                );

        long todayRefunded =
                paymentRepository.sumTodayByStatus(
                        PaymentStatus.REFUNDED,
                        start,
                        end
                );

        long todayNet = todayCompleted - todayRefunded;

        long totalCompleted =
                paymentRepository.sumByStatus(
                        PaymentStatus.COMPLETED
                );

        long totalRefunded =
                paymentRepository.sumByStatus(
                        PaymentStatus.REFUNDED
                );

        long totalNet = totalCompleted - totalRefunded;

        return new BillingStatsResponse(
                readyCount,
                confirmedCount,
                paidCount,
                finalConfirmedCount,
                todayCompleted,
                todayRefunded,
                totalCompleted,
                totalRefunded,
                todayNet,
                totalNet
        );
    }

    @Transactional(readOnly = true)
    public BillStatusResponse getBillStatus(Long billId) {
        log.info("청구 상태 조회 처리 시작 - billId={}", billId);

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new IllegalArgumentException("청구 정보를 찾을 수 없습니다. billId=" + billId));

        List<Object[]> paymentSummaries =
                paymentRepository.findPaymentStatusAndAmountByBillId(bill.getId());

        BillingStatus calculatedStatus =
                calculateBillingStatus(bill, paymentSummaries);

        log.info("청구 상태 조회 결과 - billId={}, status={}", billId, calculatedStatus.name());

        return new BillStatusResponse(billId, calculatedStatus.name());
    }

    @Transactional(readOnly = true)
    public BillingStatus getCalculatedBillingStatus(Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new IllegalArgumentException("청구 정보를 찾을 수 없습니다. billId=" + billId));

        List<Object[]> paymentSummaries =
                paymentRepository.findPaymentStatusAndAmountByBillId(bill.getId());

        return calculateBillingStatus(bill, paymentSummaries);
    }

    @Transactional(readOnly = true)
    public BillingStatus getCalculatedBillingStatus(Bill bill) {
        List<Object[]> paymentSummaries =
                paymentRepository.findPaymentStatusAndAmountByBillId(bill.getId());

        return calculateBillingStatus(bill, paymentSummaries);
    }

    public BillingStatus calculateBillingStatus(Bill bill, List<Object[]> paymentSummaries) {
        int completedAmount = 0;
        int refundedAmount = 0;

        for (Object[] row : paymentSummaries) {
            PaymentStatus status = (PaymentStatus) row[0];
            Integer amount = (Integer) row[1];

            if (amount == null) {
                amount = 0;
            }

            if (status == PaymentStatus.COMPLETED) {
                completedAmount += amount;
            } else if (status == PaymentStatus.REFUNDED) {
                refundedAmount += amount;
            }
        }

        return calculateBillingStatus(bill, completedAmount, refundedAmount);
    }

    public BillingStatus calculateBillingStatus(Bill bill, int completedAmount, int refundedAmount) {
        if (bill.getStatus() == BillingStatus.CANCELED) {
            return BillingStatus.CANCELED;
        }

        int effectivePaidAmount = completedAmount - refundedAmount;

        if (effectivePaidAmount < 0) {
            log.warn(
                    "청구 상태 조회 중 유효 결제 금액이 음수입니다. billId={}, completedAmount={}, refundedAmount={}, effectivePaidAmount={}",
                    bill.getId(),
                    completedAmount,
                    refundedAmount,
                    effectivePaidAmount
            );
            effectivePaidAmount = 0;
        }

        int totalAmount = bill.getTotalAmount();
        int remainingAmount = totalAmount - effectivePaidAmount;

        if (remainingAmount < 0) {
            log.warn(
                    "청구 상태 조회 중 남은 금액이 음수입니다. billId={}, totalAmount={}, effectivePaidAmount={}, remainingAmount={}",
                    bill.getId(),
                    totalAmount,
                    effectivePaidAmount,
                    remainingAmount
            );
            remainingAmount = 0;
        }

        BillingStatus calculatedStatus;

        if (bill.getStatus() == BillingStatus.CONFIRMED && remainingAmount == 0) {
            calculatedStatus = BillingStatus.CONFIRMED;
        } else if (remainingAmount == totalAmount) {
            calculatedStatus = BillingStatus.READY;
        } else if (remainingAmount == 0) {
            calculatedStatus = BillingStatus.PAID;
        } else {
            calculatedStatus = BillingStatus.CONFIRMED;
        }

        log.info(
                "청구 상태 계산 결과 - billId={}, completedAmount={}, refundedAmount={}, effectivePaidAmount={}, remainingAmount={}, status={}",
                bill.getId(),
                completedAmount,
                refundedAmount,
                effectivePaidAmount,
                remainingAmount,
                calculatedStatus.name()
        );

        return calculatedStatus;
    }

    private boolean matchesStatusFilter(BillSummaryResponse response,
                                        BillingStatus status,
                                        boolean confirmedOnly,
                                        boolean partialOnly) {
        if (confirmedOnly) {
            return response.getStatus() == BillingStatus.CONFIRMED
                    && response.getRemainingAmount() != null
                    && response.getRemainingAmount() == 0;
        }

        if (partialOnly) {
            return response.getStatus() == BillingStatus.CONFIRMED
                    && response.getRemainingAmount() != null
                    && response.getRemainingAmount() > 0;
        }

        return status == null || response.getStatus() == status;
    }

    /**
     * [추가] billingDate가 있으면 해당 일자의 청구만 조회
     */
    private List<Bill> findBillsForList(LocalDate billingDate) {
        if (billingDate == null) {
            return billRepository.findAll();
        }

        Timestamp start = Timestamp.valueOf(billingDate.atStartOfDay());
        Timestamp end = Timestamp.valueOf(billingDate.plusDays(1).atStartOfDay());

        return billRepository.findByTreatmentDateGreaterThanEqualAndTreatmentDateLessThan(
                start,
                end
        );
    }

    private Map<Long, PaymentRepository.BillPaymentSummaryProjection> getPaymentSummaryMap(List<Bill> bills) {
        if (bills == null || bills.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> billIds = bills.stream()
                .map(Bill::getId)
                .toList();

        return paymentRepository.findBillPaymentSummaries(
                        billIds,
                        PaymentStatus.COMPLETED,
                        PaymentStatus.REFUNDED
                )
                .stream()
                .collect(Collectors.toMap(
                        PaymentRepository.BillPaymentSummaryProjection::getBillId,
                        Function.identity()
                ));
    }

    private PaymentAmounts extractPaymentAmounts(PaymentRepository.BillPaymentSummaryProjection summary) {
        int completedAmount = 0;
        int refundedAmount = 0;

        if (summary != null) {
            if (summary.getCompletedAmount() != null) {
                completedAmount = summary.getCompletedAmount();
            }
            if (summary.getRefundedAmount() != null) {
                refundedAmount = summary.getRefundedAmount();
            }
        }

        return new PaymentAmounts(completedAmount, refundedAmount);
    }

    private record PaymentAmounts(int completedAmount, int refundedAmount) {
    }

    private void validateCalculatedAmountForConfirm(Bill bill) {
        int originalAmount = safeAmount(bill.getTotalAmount());
        int calculatedAmount = calculateBillItemsAmount(bill.getId(), originalAmount);

        if (originalAmount != calculatedAmount) {
            throw new IllegalStateException("청구 총액과 자동 계산 금액이 일치하지 않아 확정할 수 없습니다.");
        }
    }

    private int calculateBillItemsAmount(Long billId, int originalAmount) {
        List<BillItem> billItems = billItemRepository.findByBillId(billId);

        if (billItems == null || billItems.isEmpty()) {
            return originalAmount;
        }

        return billItems.stream()
                .map(BillItem::getAmount)
                .filter(amount -> amount != null)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private void saveBillHistory(Bill bill,
                                 BillingStatus oldStatus,
                                 BillingStatus newStatus,
                                 String changedBy,
                                 String changeReason) {
        BillHistory history = BillHistory.create(
                bill,
                oldStatus,
                newStatus,
                changedBy,
                changeReason,
                new Timestamp(System.currentTimeMillis())
        );

        billHistoryRepository.save(history);
    }

    private BillHistoryResponse toBillHistoryResponse(BillHistory history, Bill bill) {
        String historyType;
        String title;
        String description;

        BillingStatus oldStatus = history.getOldStatus();
        BillingStatus newStatus = history.getNewStatus();

        if (oldStatus == BillingStatus.CONFIRMED && newStatus == BillingStatus.CANCELED) {
            historyType = "BILL_CANCELED";
            title = "청구 취소";
            description = "청구가 취소되었습니다.";
        } else if (oldStatus == BillingStatus.CANCELED && newStatus == BillingStatus.PAID) {
            historyType = "BILL_RESTORED";
            title = "청구 복원";
            description = "취소된 청구가 복원되었습니다.";
        } else if (oldStatus == BillingStatus.PAID && newStatus == BillingStatus.CONFIRMED) {
            historyType = "BILL_CONFIRMED";
            title = "청구 확정";
            description = "청구가 확정되었습니다.";
        } else if (oldStatus == BillingStatus.CONFIRMED && newStatus == BillingStatus.PAID) {
            historyType = "BILL_UNCONFIRMED";
            title = "청구 확정 해제";
            description = "청구 확정이 해제되었습니다.";
        } else {
            historyType = "BILL_STATUS_CHANGED";
            title = "청구 상태 변경";
            description = "청구 상태가 변경되었습니다.";
        }

        return new BillHistoryResponse(
                toLocalDateTime(history.getChangedAt()),
                historyType,
                title,
                description,
                safeAmount(bill.getTotalAmount()),
                history.getChangedBy(),
                resolveStaffName(history.getChangedBy())
        );
    }

    private LocalDateTime resolveBillOccurredAt(Bill bill) {
        try {
            Object createdAt = bill.getCreatedAt();
            LocalDateTime createdDateTime = toLocalDateTime(createdAt);
            if (createdDateTime != null) {
                return createdDateTime;
            }
        } catch (Exception ignored) {
        }

        try {
            Object treatmentDate = bill.getTreatmentDate();
            LocalDateTime treatmentDateTime = toLocalDateTime(treatmentDate);
            if (treatmentDateTime != null) {
                return treatmentDateTime;
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }

        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }

        if (value instanceof java.util.Date date) {
            return new Timestamp(date.getTime()).toLocalDateTime();
        }

        return null;
    }

    private int safeAmount(Integer amount) {
        return amount == null ? 0 : amount;
    }

    // =========================
    // [추가] STAFF 이름 조회 로직
    // =========================

    private String resolveStaffName(String staffId) {
        if (staffId == null || staffId.isBlank()) {
            return null;
        }

        return staffNameCache.computeIfAbsent(staffId, this::queryStaffNameSafely);
    }

    private String queryStaffNameSafely(String staffId) {
        try {
            String staffNameColumn = getResolvedStaffNameColumn();

            if (staffNameColumn == null || staffNameColumn.isBlank()) {
                return null;
            }

            String sql = """
                    SELECT %s
                    FROM %s.%s
                    WHERE %s = ?
                    """.formatted(
                    staffNameColumn,
                    STAFF_TABLE_OWNER,
                    STAFF_TABLE_NAME,
                    STAFF_ID_COLUMN
            );

            List<String> result = jdbcTemplate.query(
                    sql,
                    (rs, rowNum) -> rs.getString(1),
                    staffId
            );

            if (result.isEmpty()) {
                return null;
            }

            String name = result.get(0);
            return (name == null || name.isBlank()) ? null : name;
        } catch (Exception e) {
            return null;
        }
    }

    private String getResolvedStaffNameColumn() {
        if (resolvedStaffNameColumn != null) {
            return resolvedStaffNameColumn;
        }

        synchronized (this) {
            if (resolvedStaffNameColumn != null) {
                return resolvedStaffNameColumn;
            }

            resolvedStaffNameColumn = detectStaffNameColumn();
            return resolvedStaffNameColumn;
        }
    }

    private String detectStaffNameColumn() {
        try {
            List<Map<String, Object>> ownerColumns = jdbcTemplate.queryForList("""
                    SELECT COLUMN_NAME
                    FROM ALL_TAB_COLUMNS
                    WHERE OWNER = ?
                      AND TABLE_NAME = ?
                    """, STAFF_TABLE_OWNER, STAFF_TABLE_NAME);

            String resolved = pickStaffNameColumn(ownerColumns);
            if (resolved != null) {
                return resolved;
            }
        } catch (Exception ignored) {
        }

        try {
            List<Map<String, Object>> userColumns = jdbcTemplate.queryForList("""
                    SELECT COLUMN_NAME
                    FROM USER_TAB_COLUMNS
                    WHERE TABLE_NAME = ?
                    """, STAFF_TABLE_NAME);

            return pickStaffNameColumn(userColumns);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String pickStaffNameColumn(List<Map<String, Object>> columns) {
        if (columns == null || columns.isEmpty()) {
            return null;
        }

        List<String> actualColumns = columns.stream()
                .map(row -> row.get("COLUMN_NAME"))
                .filter(value -> value != null)
                .map(value -> String.valueOf(value).toUpperCase())
                .toList();

        for (String candidate : STAFF_NAME_COLUMN_CANDIDATES) {
            if (actualColumns.contains(candidate)) {
                return candidate;
            }
        }

        return null;
    }
}