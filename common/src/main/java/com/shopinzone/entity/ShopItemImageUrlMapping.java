package com.shopinzone.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ShopItemImageUrlMapping", schema = "ShopInZone")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopItemImageUrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    Long id;

    @Column(name = "ShopId")
    Long shopId;

    @Column(name = "ItemId")
    Long itemId;

    @Column(name = "ImageUrl")
    String imageUrl;

    @Column(name = "Created")
    OffsetDateTime created;

    @Column(name = "LastUpdated")
    OffsetDateTime lastUpdated;
}
