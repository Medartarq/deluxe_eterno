package com.deluxeterno.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "stock_movements")
public class StockMovement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "product_id") private Product product;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private StockMovementType type;
    @Column(nullable = false) private int quantity;
    @Column(nullable = false) private int physicalBefore;
    @Column(nullable = false) private int physicalAfter;
    @Column(nullable = false) private int reservedBefore;
    @Column(nullable = false) private int reservedAfter;
    @Column(nullable = false, length = 255) private String reason;
    @Column(nullable = false, length = 80) private String actor;
    @Column(nullable = false) private Instant createdAt;
    protected StockMovement() {}
    public StockMovement(Product product, StockMovementType type, int quantity, int physicalBefore, int reservedBefore, String reason, String actor) {
        this.product=product; this.type=type; this.quantity=quantity; this.physicalBefore=physicalBefore;
        this.reservedBefore=reservedBefore; this.physicalAfter=product.getStockQuantity(); this.reservedAfter=product.getReservedQuantity();
        this.reason=reason; this.actor=actor; this.createdAt=Instant.now();
    }
    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public StockMovementType getType() { return type; }
    public int getQuantity() { return quantity; }
    public int getPhysicalBefore() { return physicalBefore; }
    public int getPhysicalAfter() { return physicalAfter; }
    public int getReservedBefore() { return reservedBefore; }
    public int getReservedAfter() { return reservedAfter; }
    public String getReason() { return reason; }
    public String getActor() { return actor; }
    public Instant getCreatedAt() { return createdAt; }
}
