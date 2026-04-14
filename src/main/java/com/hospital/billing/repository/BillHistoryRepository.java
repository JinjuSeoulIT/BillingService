package com.hospital.billing.repository;

import com.hospital.billing.entity.BillHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillHistoryRepository extends JpaRepository<BillHistory, Long> {

    List<BillHistory> findByBill_IdOrderByChangedAtDesc(Long billId);
}