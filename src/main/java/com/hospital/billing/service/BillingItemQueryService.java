package com.hospital.billing.service;

import com.hospital.billing.dto.BillItemResponse;
import com.hospital.billing.entity.BillItem;
import com.hospital.billing.repository.BillItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BillingItemQueryService {

    // 실제 BILL_ITEM 조회를 위해 Repository 주입
    private final BillItemRepository billItemRepository;

    public BillingItemQueryService(BillItemRepository billItemRepository) {
        this.billItemRepository = billItemRepository;
    }

    /**
     * 특정 Bill에 대한 항목별 청구 금액 상세 조회
     * (OB1-62)
     */
    public List<BillItemResponse> getBillItemDetails(Long billId) {
        // [수정] TODO / List.of() 제거 후 실제 조회 로직 반영
        List<BillItem> billItems = billItemRepository.findByBillId(billId);

        return billItems.stream()
                .map(BillItemResponse::from)
                .collect(Collectors.toList());
    }
}