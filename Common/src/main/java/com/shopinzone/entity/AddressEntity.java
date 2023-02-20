package com.shopinzone.entity;

import com.shopinzone.enums.AddressType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "AddressEntity", schema = "ShopInZone")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AddressId", nullable = false)
    Long addressId;

    @Column(name = "email")
    String email;

    @Column(name = "Name")
    String name;

    @Column(name = "Mobile")
    String mobile;

    @Column(name = "AddressType")
    AddressType addressType;

    @Column(name = "PinCode")
    String pinCode;

    @Column(name = "City")
    String city;

    @Column(name = "LandMark")
    String landMark;

    @Column(name = "State")
    String state;

    @Column(name = "AddressText")
    String addressText;

    @Column(name = "Created")
    OffsetDateTime created;

    @Column(name = "LastUpdated")
    OffsetDateTime lastUpdated;
}
