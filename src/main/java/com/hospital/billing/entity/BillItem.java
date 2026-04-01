package com.hospital.billing.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "BILL_ITEM")
public class BillItem {

    @Id
    // [수정] Oracle DB + 실제 BILL_ITEM 시퀀스 구조에 맞게 변경
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bill_item_seq_generator")
    @SequenceGenerator(
            name = "bill_item_seq_generator",
            sequenceName = "BILL_ITEM_INTG_SEQ",
            allocationSize = 1
    )
    @Column(name = "BILL_ITEM_ID")
    private Long id;

    // 어떤 Bill에 속한 항목인지 이게 핵심
    @ManyToOne(fetch = FetchType.LAZY)
    // 실제 DB 컬럼명에 맞춤
    @JoinColumn(name = "BILL_ID", nullable = false)
    private Bill bill;

    // 진료 항목 이름 (예: 진찰료, X-ray 검사)
    // ITEM_NAME 길이를 200으로 확장
    @Column(name = "ITEM_NAME", nullable = false, length = 200)
    private String itemName;

    // 실제 DB에는 ITEM_AMOUNT 컬럼만 존재하므로 amount만 유지
    @Column(name = "ITEM_AMOUNT", nullable = false)
    private Integer amount;

    protected BillItem() {
    }

    // quantity, unitPrice 제거 후 현재 DB 구조에 맞는 생성자로 정리
    public BillItem(Bill bill, String itemName, Integer amount) {
        this.bill = bill;
        this.itemName = itemName;
        this.amount = amount;
    }

    // 정적 팩토리 메서드 추가
    public static BillItem create(Bill bill, String itemName, Integer amount) {
        return new BillItem(bill, itemName, amount);
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

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }
}