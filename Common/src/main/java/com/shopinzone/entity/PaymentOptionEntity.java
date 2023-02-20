package com.shopinzone.entity;

import com.shopinzone.enums.PaymentOptionType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "PaymentOptionEntity", schema = "ShopInZone")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentOptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PaymentOptionId", nullable = false)
    Long paymentOptionId;

    @Column(name = "UserId")
    Long userId;

    @Column(name = "paymentOptionName")
    String paymentOptionName;

    @Column(name = "Description")
    String description;

    @Column(name = "PaymentOptionType")
    PaymentOptionType paymentOptionType;

    @Column(name = "Created")
    OffsetDateTime created;

    @Column(name = "LastUpdated")
    OffsetDateTime lastUpdated;
}
