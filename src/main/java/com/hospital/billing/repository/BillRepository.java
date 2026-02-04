package com.hospital.billing.repository;

import com.hospital.billing.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Long> {

    // 환자 ID로 진료비(Bill) 목록 조회
    List<Bill> findByPatientId(Long patientId);
}
