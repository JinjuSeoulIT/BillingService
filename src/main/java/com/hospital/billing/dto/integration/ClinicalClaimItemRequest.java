package com.hospital.billing.dto.integration;

public class ClinicalClaimItemRequest {

    private String itemName;
    private String itemCode;
    private String orderType;
    private Long sourceId;
    private String sourceType;

    public ClinicalClaimItemRequest() {
    }

    public ClinicalClaimItemRequest(String itemName,
                                    String itemCode,
                                    String orderType,
                                    Long sourceId,
                                    String sourceType) {
        this.itemName = itemName;
        this.itemCode = itemCode;
        this.orderType = orderType;
        this.sourceId = sourceId;
        this.sourceType = sourceType;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
}