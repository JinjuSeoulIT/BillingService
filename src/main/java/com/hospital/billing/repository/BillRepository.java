package com.hospital.billing.repository;

import com.hospital.billing.entity.Bill;
import com.hospital.billing.entity.BillingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {

    List<Bill> findByPatientId(Long patientId);

    /**
     * [OB1-63] 환자,상태 기준 청구 목록 조회 (상태 필터용)
     */
    List<Bill> findByPatientIdAndStatus(Long patientId, BillingStatus status);

    List<Bill> findByStatus(BillingStatus status);

    List<Bill> findByRemainingAmountGreaterThan(Integer amount); // 미수금

    // 이벤트 ID 기준 중복 체크
    Optional<Bill> findBySourceEventId(String sourceEventId);

    // visitId 기준 기존 bill 존재 여부 확인
    Optional<Bill> findByVisitId(Long visitId);

    //visitId 기준 청구 목록 조회
    List<Bill> findAllByVisitId(Long visitId);

    // 일자 기준 청구 목록 조회
    List<Bill> findByTreatmentDateGreaterThanEqualAndTreatmentDateLessThan(
            Timestamp start,
            Timestamp end
    );

    // 상태별 건수 조회
    @Query("SELECT COUNT(b) FROM Bill b WHERE b.status = :status")
    long countByStatus(@Param("status") BillingStatus status);
}