package com.shopinzone.entity;

import com.shopinzone.enums.Gender;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ProfileEntity", schema = "ShopInZone")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProfileId", nullable = false)
    Long profileId;

    @Column(name = "Email")
    String email;

    @Column(name = "Name")
    String name;

    @Column(name = "Mobile")
    String mobile;

    @Column(name = "Gender")
    Gender gender;

    @Column(name = "PinCode")
    String pinCode;

    @Column(name = "City")
    String city;

    @Column(name = "Created")
    OffsetDateTime created;

    @Column(name = "LastUpdated")
    OffsetDateTime lastUpdated;
}
