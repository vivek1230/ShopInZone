package com.shopinzone.entity;

import com.shopinzone.enums.UserRoleType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "UserEntity", schema = "ShopInZone")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserId", nullable = false)
    Long userId;

    @Column(name = "Email")
    String email;

    @Column(name = "Mobile")
    String mobile;

    @Column(name = "Password")
    String password;

    @Column(name = "UserRoleType")
    UserRoleType userRoleType;

    @Column(name = "Created")
    OffsetDateTime created;

    @Column(name = "LastUpdated")
    OffsetDateTime lastUpdated;
}
