package com.hospital.billing.payment.repository;

import com.hospital.billing.payment.entity.Payment;
import com.hospital.billing.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // 전체 누적 결제 금액
    @Query("""
        SELECT COALESCE(SUM(p.paymentAmount), 0)
        FROM Payment p
        WHERE p.status = :status
    """)
    long sumByStatus(@Param("status") PaymentStatus status);

    // 오늘 결제 금액
    @Query("""
        SELECT COALESCE(SUM(p.paymentAmount), 0)
        FROM Payment p
        WHERE p.status = :status
          AND p.paidAt >= :start
          AND p.paidAt < :end
    """)
    long sumTodayByStatus(@Param("status") PaymentStatus status,
                          @Param("start") Timestamp start,
                          @Param("end") Timestamp end);

    // 특정 청구 + 상태 조회
    Optional<Payment> findByBill_IdAndStatus(Long billId, PaymentStatus status);

    // [추가] 특정 청구에 특정 상태 이력이 몇 건 있는지 조회
    @Query("""
        SELECT COUNT(p)
        FROM Payment p
        WHERE p.bill.id = :billId
          AND p.status = :status
    """)
    long countByBillIdAndStatus(@Param("billId") Long billId,
                                @Param("status") PaymentStatus status);

    // 청구 기준 결제 내역 조회
    List<Payment> findByBill_IdOrderByPaidAtDesc(Long billId);

    /**
     * [기존]
     * 청구 상태 계산용 요약 조회
     * - PAYMENT_METHOD 컬럼을 읽지 않아서 enum 매핑 오류를 우회할 수 있음
     * - row[0] = PaymentStatus
     * - row[1] = paymentAmount
     */
    @Query("""
        SELECT p.status, p.paymentAmount
        FROM Payment p
        WHERE p.bill.id = :billId
        ORDER BY p.paidAt DESC
    """)
    List<Object[]> findPaymentStatusAndAmountByBillId(@Param("billId") Long billId);

    /**
     * [2차 최적화]
     * 목록 조회용 payment 요약 배치 조회
     * - bill_id 기준으로 한 번에 group by
     * - BillingStatusQueryService 실제 계산식에 맞춰 COMPLETED / REFUNDED 합계만 조회
     * - CANCELED payment 는 현재 실제 상태 계산식에서 사용하지 않으므로 제외
     */
    @Query("""
        SELECT
            p.bill.id AS billId,
            COALESCE(SUM(CASE WHEN p.status = :completedStatus THEN p.paymentAmount ELSE 0 END), 0) AS completedAmount,
            COALESCE(SUM(CASE WHEN p.status = :refundedStatus THEN p.paymentAmount ELSE 0 END), 0) AS refundedAmount
        FROM Payment p
        WHERE p.bill.id IN :billIds
        GROUP BY p.bill.id
    """)
    List<BillPaymentSummaryProjection> findBillPaymentSummaries(@Param("billIds") List<Long> billIds,
                                                                @Param("completedStatus") PaymentStatus completedStatus,
                                                                @Param("refundedStatus") PaymentStatus refundedStatus);

    interface BillPaymentSummaryProjection {
        Long getBillId();
        Integer getCompletedAmount();
        Integer getRefundedAmount();
    }
}