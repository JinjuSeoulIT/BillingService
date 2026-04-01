package com.hospital.billing.repository;

import com.hospital.billing.entity.BillItemSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillItemSourceRepository extends JpaRepository<BillItemSource, Long> {

    // 특정 BILL_ITEM 기준 근거 조회
    List<BillItemSource> findByBillItemId(Long billItemId);
}