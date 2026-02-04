package com.hospital.billing.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "bill_items")
public class BillItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 Bill에 속한 항목인지 이게 핵심
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    // 진료 항목 이름 (예: 진찰료, X-ray 검사)
    @Column(nullable = false)
    private String itemName;

    // 수량 (보통 1, 검사 여러 번이면 2 이상 가능)
    @Column(nullable = false)
    private Integer quantity;

    // 단가
    @Column(nullable = false)
    private Integer unitPrice;

    // 항목 금액 = quantity * unitPrice
    @Column(nullable = false)
    private Integer amount;

    protected BillItem() {
    }

    public BillItem(Bill bill, String itemName, Integer quantity, Integer unitPrice, Integer amount) {
        this.bill = bill;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.amount = amount;
    }

    // ===== getter / setter =====
    public Long getId() {
        return id;
    }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Integer unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }
}
