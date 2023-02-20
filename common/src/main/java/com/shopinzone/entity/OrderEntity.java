package com.shopinzone.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "OrderEntity", schema = "ShopInZone")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AddressId", nullable = false)
    Long orderId;

    @Column(name = "UserId")
    Long userId;

    @Column(name = "ShopId")
    Long shopId;

    @Column(name = "OrderDate")
    OffsetDateTime orderDate;

    @Column(name = "TotalAmount")
    Double totalAmount;

    @Column(name = "OrderAddressId")
    Long orderAddressId;

    @Column(name = "OrderPaymentOptionId")
    Long orderPaymentOptionId;

    @Column(name = "Created")
    OffsetDateTime created;

    @Column(name = "LastUpdated")
    OffsetDateTime lastUpdated;
}
