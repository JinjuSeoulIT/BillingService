package com.hospital.billing.dto;

import com.hospital.billing.entity.BillItem;

public class BillItemResponse {

    private Long billItemId;
    private String itemName;
    private Integer amount;

    public BillItemResponse() {
    }

    // [수정] 현재 DB 구조에 맞게 quantity, unitPrice 제거
    public BillItemResponse(Long billItemId, String itemName, Integer amount) {
        this.billItemId = billItemId;
        this.itemName = itemName;
        this.amount = amount;
    }

    // [추가] 엔티티 → DTO 변환용 정적 메서드
    public static BillItemResponse from(BillItem billItem) {
        return new BillItemResponse(
                billItem.getId(),
                billItem.getItemName(),
                billItem.getAmount()
        );
    }

    public Long getBillItemId() {
        return billItemId;
    }

    public String getItemName() {
        return itemName;
    }

    public Integer getAmount() {
        return amount;
    }
}