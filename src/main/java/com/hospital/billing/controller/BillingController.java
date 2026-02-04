package com.hospital.billing.controller;

import com.hospital.billing.entity.Bill;
import com.hospital.billing.entity.BillItem;
import com.hospital.billing.repository.BillItemRepository;
import com.hospital.billing.repository.BillRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;

    public BillingController(BillRepository billRepository,
                             BillItemRepository billItemRepository) {
        this.billRepository = billRepository;
        this.billItemRepository = billItemRepository;
    }

    /**
     * 환자 ID로 진료비 조회 (기존 기능 유지)
     * GET /api/billing/{patientId}
     */
    @GetMapping("/{patientId}")
    public List<Map<String, Object>> getBillingByPatientId(
            @PathVariable Long patientId
    ) {
        List<Bill> bills = billRepository.findByPatientId(patientId);

        return bills.stream().map(bill -> {
            Map<String, Object> result = new HashMap<>();
            List<BillItem> items = billItemRepository.findByBillId(bill.getId());

            result.put("bill", bill);
            result.put("items", items);
            return result;
        }).toList();
    }

}
