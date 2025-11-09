package com.fooddelivery.order.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_items_order_id", columnList = "order_id"),
    @Index(name = "idx_order_items_menu_item_id", columnList = "menuItemId")
})
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(exclude = "order")
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    /**
     * Reference to menu item in restaurant-service
     */
    @Column(nullable = false)
    private Long menuItemId;
    
    /**
     * Menu item name (snapshot at time of order)
     */
    @Column(nullable = false, length = 200)
    private String menuItemName;
    
    /**
     * Quantity ordered
     */
    @Column(nullable = false)
    private Integer quantity;
    
    /**
     * Price per unit at time of order (snapshot)
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    /**
     * Subtotal = quantity * price
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    /**
     * Special instructions for this item
     */
    @Column(length = 200)
    private String specialInstructions;
    
    @PrePersist
    @PreUpdate
    protected void calculateSubtotal() {
        if (quantity != null && price != null) {
            this.subtotal = price.multiply(BigDecimal.valueOf(quantity));
        }
    }
}

