package com.hospital.billing.repository;

import com.hospital.billing.entity.Payment;
import com.hospital.billing.entity.PaymentStatus;
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
}