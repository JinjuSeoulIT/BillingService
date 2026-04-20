package com.hospital.billing.dto;

import com.hospital.billing.entity.BillItem;

public class BillItemResponse {

    private Long billItemId;
    private String itemName;
    private String itemCategory;
    private Integer quantity;
    private Integer unitPrice;
    private Integer amount;

    public BillItemResponse() {
    }

    // [추가] 하위 호환용 생성자 유지
    public BillItemResponse(Long billItemId, String itemName, Integer amount) {
        this(
                billItemId,
                itemName,
                "ETC",
                1,
                amount,
                amount
        );
    }

    // [추가] 확장 응답 생성자
    public BillItemResponse(Long billItemId,
                            String itemName,
                            String itemCategory,
                            Integer quantity,
                            Integer unitPrice,
                            Integer amount) {
        this.billItemId = billItemId;
        this.itemName = itemName;
        this.itemCategory = itemCategory;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.amount = amount;
    }

    // [추가] 엔티티 → DTO 변환용 정적 메서드
    public static BillItemResponse from(BillItem billItem) {
        return new BillItemResponse(
                billItem.getId(),
                billItem.getItemName(),
                billItem.getItemCategory(),
                billItem.getQuantity(),
                billItem.getUnitPrice(),
                billItem.getAmount()
        );
    }

    public Long getBillItemId() {
        return billItemId;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemCategory() {
        return itemCategory;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getUnitPrice() {
        return unitPrice;
    }

    public Integer getAmount() {
        return amount;
    }
}
