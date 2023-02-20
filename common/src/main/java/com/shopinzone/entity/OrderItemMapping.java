package com.shopinzone.entity;

import com.shopinzone.enums.PricingType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "OrderItemMapping", schema = "ShopInZone")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    Long id;

    @Column(name = "OrderId")
    Long orderId;

    @Column(name = "ItemId")
    Long itemId;

    @Column(name = "ItemName")
    String itemName;

    @Column(name = "Description")
    String description;

    @Column(name = "OrderQuantity")
    int orderQuantity;

    @Column(name = "Price")
    Double price;

    @Column(name = "PricingType")
    PricingType pricingType;

    @Column(name = "Created")
    OffsetDateTime created;

    @Column(name = "LastUpdated")
    OffsetDateTime lastUpdated;
}
