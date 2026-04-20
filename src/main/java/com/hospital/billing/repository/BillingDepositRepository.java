package com.hospital.billing.repository;

import com.hospital.billing.entity.BillingDeposit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillingDepositRepository extends JpaRepository<BillingDeposit, Long> {

    List<BillingDeposit> findAllByOrderByReceivedAtDescDepositIdDesc();

    List<BillingDeposit> findByPatientIdOrderByReceivedAtDescDepositIdDesc(Long patientId);
}
