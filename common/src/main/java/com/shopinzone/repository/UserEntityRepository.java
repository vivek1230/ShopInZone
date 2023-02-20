package com.shopinzone.repository;

import com.shopinzone.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserEntityRepository extends JpaRepository<UserEntity, Long> {

    // Optional<UserEntity> findByEmail(String email);
    //
    // Optional<UserEntity> findByMobile(String mobile);

    @Query(value = "select * from ShopInZone.UserEntity where UserId=?1 ", nativeQuery = true)
    Optional<UserEntity> findByUserId(Long userId);

    @Query(value = "select * from ShopInZone.UserEntity where Email=?1 ", nativeQuery = true)
    Optional<UserEntity> findByEmail(String email);

    @Query(value = "select * from ShopInZone.UserEntity where Mobile=?1 ", nativeQuery = true)
    Optional<UserEntity> findByMobile(String mobile);

}
