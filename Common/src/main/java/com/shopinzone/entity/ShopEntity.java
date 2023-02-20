package com.shopinzone.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ShopEntity", schema = "ShopInZone")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ShopId", nullable = false)
    Long shopId;

    @Column(name = "ShopName")
    String shopName;

    @Column(name = "Description")
    String description;

    @Column(name = "CityId")
    Long cityId;

    @Column(name = "AddressId")
    Long addressId;

    @Column(name = "Rating")
    String rating;

    @Column(name = "ImageUrl")
    String imageUrl;

    @Column(name = "Created")
    OffsetDateTime created;

    @Column(name = "LastUpdated")
    OffsetDateTime lastUpdated;
}
