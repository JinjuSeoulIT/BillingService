package com.hospital.billing.repository;

import com.hospital.billing.entity.Bill;
import com.hospital.billing.entity.BillingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Long> {

    List<Bill> findByPatientId(Long patientId);

    /**
     * [OB1-63] 환자,상태 기준 청구 목록 조회 (상태 필터용)
     */
    List<Bill> findByPatientIdAndStatus(Long patientId, BillingStatus status);

    List<Bill> findByStatus(BillingStatus status);

    List<Bill> findByRemainingAmountGreaterThan(Integer amount); //미수금


    // 상태별 건수 조회
    @Query("SELECT COUNT(b) FROM Bill b WHERE b.status = :status")
    long countByStatus(@Param("status") BillingStatus status);
}