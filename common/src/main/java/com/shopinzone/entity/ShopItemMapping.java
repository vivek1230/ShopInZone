package com.shopinzone.entity;

import com.shopinzone.enums.PricingType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ShopItemMapping", schema = "ShopInZone")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopItemMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    Long id;

    @Column(name = "ShopId")
    Long shopId;

    @Column(name = "ItemId")
    Long itemId;

    @Column(name = "ItemName")
    String itemName;

    @Column(name = "Description")
    String description;

    @Column(name = "Availability")
    boolean availability;

    @Column(name = "AvailableQuantity")
    int availableQuantity;

    @Column(name = "Price")
    Double price;

    @Column(name = "PricingType")
    PricingType pricingType;

    @Column(name = "Rating")
    String rating;

    @Column(name = "Created")
    OffsetDateTime created;

    @Column(name = "LastUpdated")
    OffsetDateTime lastUpdated;
}
