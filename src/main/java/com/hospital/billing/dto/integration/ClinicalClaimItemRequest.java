package com.hospital.billing.dto.integration;

public class ClinicalClaimItemRequest {

    // clinical ORDER_ITEM 기준 항목명
    private String itemName;

    // clinical ORDER_ITEM 코드
    private String itemCode;

    // clinical ORDER 헤더의 orderType
    // 예: PRESCRIPTION, BLOOD
    private String orderType;

    // sourceId = ORDER_ITEM_ID
    private Long sourceId;

    // sourceType = "CLINICAL_ORDER_ITEM" 고정
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