package com.hospital.billing.payment.service;

import com.hospital.billing.payment.dto.PaymentResponse;
import com.hospital.billing.entity.Bill;
import com.hospital.billing.entity.BillingStatus;
import com.hospital.billing.payment.entity.Payment;
import com.hospital.billing.payment.entity.PaymentMethod;
import com.hospital.billing.payment.entity.PaymentStatus;
import com.hospital.billing.exception.InvalidPaymentStatusException;
import com.hospital.billing.exception.InvalidRefundAmountException;
import com.hospital.billing.exception.PaymentNotFoundException;
import com.hospital.billing.repository.BillRepository;
import com.hospital.billing.payment.repository.PaymentRepository;
import com.hospital.billing.toss.client.TossPaymentClient;
import com.hospital.billing.toss.dto.TossCancelRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Transactional
public class PaymentService {

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
    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;
    private final TossPaymentClient tossPaymentClient;
    private final JdbcTemplate jdbcTemplate;

    // [추가] 이름 조회 캐시
    private final ConcurrentMap<String, String> staffNameCache = new ConcurrentHashMap<>();
    private volatile String resolvedStaffNameColumn;

    public PaymentService(PaymentRepository paymentRepository,
                          BillRepository billRepository,
                          TossPaymentClient tossPaymentClient,
                          JdbcTemplate jdbcTemplate) {
        this.paymentRepository = paymentRepository;
        this.billRepository = billRepository;

        this.tossPaymentClient = tossPaymentClient;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * [수정] 현재 담당 직원 ID 조회
     * - 현재 Oracle FK(FK_PAYMENT_CREATED_BY)에 실제 존재하는 STAFF_ID를 반환해야 함
     * - 기존 성공 이력 기준 staffId 형식에 맞춰 임시 고정
     * - 추후 실제 인증/세션 또는 Oracle STAFF 조회 흐름으로 교체
     */
    private String getCurrentStaffId() {
        return "ADM-2026-0001";
    }

    /**
     * 수납 생성 (기존 방식 유지)
     */
    public PaymentResponse createPayment(Long billId, Integer amount, PaymentMethod method) {
        return createPayment(billId, amount, method, null, null, getCurrentStaffId());
    }

    /**
     * 수납 생성 (직원 ID 포함)
     */
    public PaymentResponse createPayment(Long billId,
                                         Integer amount,
                                         PaymentMethod method,
                                         String staffId) {
        return createPayment(billId, amount, method, null, null, staffId);
    }

    /**
     * 수납 생성 (토스 원거래 정보 포함)
     */
    public PaymentResponse createPayment(Long billId,
                                         Integer amount,
                                         PaymentMethod method,
                                         String paymentKey,
                                         String orderId) {
        return createPayment(billId, amount, method, paymentKey, orderId, getCurrentStaffId());
    }

    /**
     * 수납 생성 (토스 원거래 정보 + 직원 ID 포함)
     */
    public PaymentResponse createPayment(Long billId,
                                         Integer amount,
                                         PaymentMethod method,
                                         String paymentKey,
                                         String orderId,
                                         String staffId) {

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() ->
                        new IllegalArgumentException("청구가 존재하지 않습니다. billId=" + billId));

        if (bill.getStatus() == BillingStatus.PAID) {
            throw new IllegalStateException("이미 수납 완료된 청구입니다.");
        }

        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("결제 금액은 0보다 커야 합니다.");
        }

        if (amount > bill.getRemainingAmount()) {
            throw new IllegalArgumentException("결제 금액이 남은 금액보다 클 수 없습니다.");
        }

        if (method == null) {
            throw new IllegalArgumentException("결제 수단이 필요합니다.");
        }

        Payment payment;

        // 카드 결제이면서 paymentKey / orderId 가 있으면 토스 원거래 정보까지 저장
        if (method == PaymentMethod.CARD
                && paymentKey != null && !paymentKey.isBlank()
                && orderId != null && !orderId.isBlank()) {

            payment = new Payment(bill, amount, method, paymentKey, orderId);

        } else {
            payment = new Payment(bill, amount, method);
        }

        if (staffId != null && !staffId.isBlank()) {
            payment.setCreatedBy(staffId);
        }

        int newPaidAmount = bill.getPaidAmount() + amount;
        applyBillAmounts(bill, newPaidAmount);

        Payment saved = paymentRepository.save(payment);

        return new PaymentResponse(
                saved.getId(),
                saved.getBill().getId(),
                saved.getPaymentAmount(),
                resolvePaymentStatus(saved),
                resolvePaymentMethod(saved),
                saved.getPaidAt(),
                saved.getCreatedBy(),
                saved.getCanceledBy(),
                resolveStaffName(saved.getCreatedBy()),
                resolveStaffName(saved.getCanceledBy())
        );
    }

    /**
     * 수납 취소 (전체 취소)
     */
    public void cancelPayment(Long paymentId) {
        cancelPayment(paymentId, getCurrentStaffId());
    }

    /**
     * 수납 취소 (전체 취소, 직원 ID 포함)
     */
    public void cancelPayment(Long paymentId, String staffId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new PaymentNotFoundException(paymentId));

        // 이미 취소된 결제 방지
        if (payment.getStatus() == PaymentStatus.CANCELED) {
            throw new IllegalStateException("이미 취소된 결제입니다.");
        }

        // 완료된 결제만 취소 허용
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("취소 가능한 결제 상태가 아닙니다.");
        }

        Bill bill = payment.getBill();

        // 현재 구조에서는 환불이 어느 원결제에 연결되는지 추적하지 못하므로
        // 같은 청구에 REFUNDED 이력이 하나라도 있으면 전체 취소를 막는다.
        boolean hasRefundHistory =
                paymentRepository.countByBillIdAndStatus(bill.getId(), PaymentStatus.REFUNDED) > 0;

        if (hasRefundHistory) {
            throw new IllegalStateException("부분 환불 이력이 있는 청구는 전체 수납 취소를 할 수 없습니다.");
        }

        // 카드 결제면 토스 취소 먼저 호출
        if (payment.getMethod() == PaymentMethod.CARD) {
            if (payment.getPaymentKey() == null || payment.getPaymentKey().isBlank()) {
                throw new IllegalStateException("카드 결제의 paymentKey가 없어 토스 취소를 진행할 수 없습니다.");
            }

            TossCancelRequest cancelRequest = new TossCancelRequest();
            cancelRequest.setPaymentKey(payment.getPaymentKey());
            cancelRequest.setCancelReason("사용자 요청에 의한 전체 취소");
            cancelRequest.setCancelAmount(Long.valueOf(payment.getPaymentAmount()));

            tossPaymentClient.cancelPayment(cancelRequest);
        }

        int newPaidAmount = bill.getPaidAmount() - payment.getPaymentAmount();

        if (newPaidAmount < 0) {
            throw new IllegalStateException("취소 처리 후 결제 금액이 음수가 되어 취소를 진행할 수 없습니다.");
        }

        payment.cancel();

        if (staffId != null && !staffId.isBlank()) {
            payment.setCanceledBy(staffId);
        }

        applyBillAmounts(bill, newPaidAmount);
    }

    /**
     * 부분 환불 기능
     */
    public PaymentResponse refundPayment(Long paymentId, Integer refundAmount) {
        return refundPayment(paymentId, refundAmount, getCurrentStaffId());
    }

    /**
     * 부분 환불 기능 (직원 ID 포함)
     */
    public PaymentResponse refundPayment(Long paymentId, Integer refundAmount, String staffId) {

        Payment originalPayment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new PaymentNotFoundException(paymentId));

        if (originalPayment.getStatus() != PaymentStatus.COMPLETED) {
            throw new InvalidPaymentStatusException("환불 가능한 결제가 아닙니다.");
        }

        if (refundAmount == null || refundAmount <= 0) {
            throw new InvalidRefundAmountException("환불 금액은 0보다 커야 합니다.");
        }

        if (refundAmount > originalPayment.getPaymentAmount()) {
            throw new InvalidRefundAmountException("환불 금액이 결제 금액보다 클 수 없습니다.");
        }

        Bill bill = originalPayment.getBill();

        // 현재 청구의 유효 결제 금액보다 더 많이 환불되면 안 됨
        if (refundAmount > bill.getPaidAmount()) {
            throw new InvalidRefundAmountException("환불 금액이 현재 유효 결제 금액보다 클 수 없습니다.");
        }

        // 카드 결제 환불이면 토스 부분 취소 먼저 호출
        if (originalPayment.getMethod() == PaymentMethod.CARD) {
            if (originalPayment.getPaymentKey() == null || originalPayment.getPaymentKey().isBlank()) {
                throw new IllegalStateException("카드 결제의 paymentKey가 없어 토스 부분 환불을 진행할 수 없습니다.");
            }

            TossCancelRequest cancelRequest = new TossCancelRequest();
            cancelRequest.setPaymentKey(originalPayment.getPaymentKey());
            cancelRequest.setCancelReason("사용자 요청에 의한 부분 환불");
            cancelRequest.setCancelAmount(Long.valueOf(refundAmount));

            tossPaymentClient.cancelPayment(cancelRequest);
        }

        int newPaidAmount = bill.getPaidAmount() - refundAmount;

        // 음수 방지
        if (newPaidAmount < 0) {
            throw new InvalidRefundAmountException("환불 처리 후 결제 금액이 음수가 될 수 없습니다.");
        }

        applyBillAmounts(bill, newPaidAmount);

        Payment refund = new Payment(bill, refundAmount, originalPayment.getMethod());
        refund.setStatus(PaymentStatus.REFUNDED);

        if (staffId != null && !staffId.isBlank()) {
            refund.setCreatedBy(staffId);
        }

        Payment saved = paymentRepository.save(refund);

        return new PaymentResponse(
                saved.getId(),
                saved.getBill().getId(),
                saved.getPaymentAmount(),
                resolvePaymentStatus(saved),
                resolvePaymentMethod(saved),
                saved.getPaidAt(),
                saved.getCreatedBy(),
                saved.getCanceledBy(),
                resolveStaffName(saved.getCreatedBy()),
                resolveStaffName(saved.getCanceledBy())
        );
    }

    /**
     * 전체 결제 조회
     */
    public List<PaymentResponse> getPaymentsAsResponse() {
        return paymentRepository.findAll().stream()
                .map(this::toPaymentResponse)
                .toList();
    }

    /**
     * 청구 기준 결제 내역 조회
     */
    public List<PaymentResponse> getPaymentsByBill(Long billId) {

        return paymentRepository
                .findByBill_IdOrderByPaidAtDesc(billId)
                .stream()
                .map(this::toPaymentResponse)
                .toList();
    }

    private PaymentResponse toPaymentResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getBill().getId(),
                payment.getPaymentAmount(),
                resolvePaymentStatus(payment),
                resolvePaymentMethod(payment),
                payment.getPaidAt(),
                payment.getCreatedBy(),
                payment.getCanceledBy(),
                resolveStaffName(payment.getCreatedBy()),
                resolveStaffName(payment.getCanceledBy())
        );
    }

    private String resolvePaymentStatus(Payment payment) {
        return payment.getStatus() != null
                ? payment.getStatus().name()
                : "UNKNOWN";
    }

    private String resolvePaymentMethod(Payment payment) {
        return payment.getMethod() != null
                ? payment.getMethod().name()
                : "UNKNOWN";
    }

    /**
     * Bill 금액/상태 재계산 공통 처리
     */
    private void applyBillAmounts(Bill bill, int newPaidAmount) {
        if (newPaidAmount < 0) {
            throw new IllegalStateException("결제 금액은 음수가 될 수 없습니다.");
        }

        int totalAmount = bill.getTotalAmount();
        int newRemainingAmount = totalAmount - newPaidAmount;

        if (newRemainingAmount < 0) {
            throw new IllegalStateException("남은 금액은 음수가 될 수 없습니다.");
        }

        if (newRemainingAmount > totalAmount) {
            throw new IllegalStateException("남은 금액이 총 청구 금액보다 클 수 없습니다.");
        }

        bill.setPaidAmount(newPaidAmount);
        bill.setRemainingAmount(newRemainingAmount);

        if (newRemainingAmount == 0) {
            bill.setStatus(BillingStatus.PAID);
        } else if (newPaidAmount == 0) {
            bill.setStatus(BillingStatus.READY);
        } else {
            bill.setStatus(BillingStatus.CONFIRMED);
        }
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
