package com.hospital.billing.paymentmethod.repository;

import com.hospital.billing.paymentmethod.entity.PaymentMethodMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentMethodMasterRepository extends JpaRepository<PaymentMethodMaster, String> {

    List<PaymentMethodMaster> findByUseYn(String useYn);
}