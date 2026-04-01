package com.hospital.billing.repository;

import com.hospital.billing.entity.BillingRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BillingRequestRepository extends JpaRepository<BillingRequest, Long> {

    // [추가] eventId 기준 요청 중복 확인
    Optional<BillingRequest> findByEventId(String eventId);
}