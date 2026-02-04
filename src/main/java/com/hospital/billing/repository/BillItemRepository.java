package com.hospital.billing.repository;

import com.hospital.billing.entity.BillItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillItemRepository extends JpaRepository<BillItem, Long> {

    // 특정 Bill에 속한 항목들 조회
    List<BillItem> findByBillId(Long billId);
}
