package com.fooddelivery.order.entity;

import com.fooddelivery.order.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_user_id", columnList = "userId"),
    @Index(name = "idx_orders_restaurant_id", columnList = "restaurantId"),
    @Index(name = "idx_orders_status", columnList = "status"),
    @Index(name = "idx_orders_created_at", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(exclude = "items")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private Long restaurantId;
    
    @Column(nullable = false)
    private Long deliveryAddressId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;
    
    /**
     * Total amount = subtotal + deliveryFee - discount
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    /**
     * Subtotal of all order items (without delivery fee and discounts)
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    /**
     * Delivery fee for this order
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryFee;
    
    /**
     * Discount applied to the order
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;
    
    /**
     * Promo code used for discount
     */
    @Column(length = 50)
    private String promoCode;
    
    /**
     * Special instructions for the restaurant or courier
     */
    @Column(length = 500)
    private String specialInstructions;
    
    /**
     * Estimated delivery time in minutes
     */
    @Column
    private Integer estimatedDeliveryTime;
    
    /**
     * Actual delivery time (set when status changes to DELIVERED)
     */
    @Column
    private LocalDateTime deliveredAt;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderHistory> history = new ArrayList<>();
    
    @Column(nullable = false, updatable = false, columnDefinition = "DATETIME2 DEFAULT GETDATE()")
    private LocalDateTime createdAt;
    
    @Column(columnDefinition = "DATETIME2")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = OrderStatus.CREATED;
        }
        if (discount == null) {
            discount = BigDecimal.ZERO;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Calculate total amount: subtotal + deliveryFee - discount
     */
    public void calculateTotal() {
        this.totalAmount = this.subtotal
                .add(this.deliveryFee)
                .subtract(this.discount);
    }
    
    /**
     * Add item to order and recalculate subtotal
     */
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        recalculateSubtotal();
    }
    
    /**
     * Recalculate subtotal from all items
     */
    public void recalculateSubtotal() {
        this.subtotal = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        calculateTotal();
    }
    
    /**
     * Add status change to history
     */
    public void addHistory(OrderStatus newStatus, Long changedBy, String comment) {
        OrderHistory historyEntry = new OrderHistory();
        historyEntry.setOrder(this);
        historyEntry.setStatus(newStatus);
        historyEntry.setChangedBy(changedBy);
        historyEntry.setComment(comment);
        history.add(historyEntry);
        
        this.status = newStatus;
        
        if (newStatus == OrderStatus.DELIVERED) {
            this.deliveredAt = LocalDateTime.now();
        }
    }
}

