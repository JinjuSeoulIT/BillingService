package com.hospital.billing.dto;

public class BillItemResponse {

    private Long billItemId;
    private String itemName;
    private Integer quantity;
    private Integer unitPrice;
    private Integer amount;

    public BillItemResponse() {
    }

    public BillItemResponse(Long billItemId, String itemName,
                            Integer quantity, Integer unitPrice, Integer amount) {
        this.billItemId = billItemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.amount = amount;
    }

    public Long getBillItemId() {
        return billItemId;
    }

    public String getItemName() {
        return itemName;
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
