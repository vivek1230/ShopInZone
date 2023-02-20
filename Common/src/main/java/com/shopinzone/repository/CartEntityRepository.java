package com.shopinzone.repository;

import com.shopinzone.entity.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartEntityRepository extends JpaRepository<CartEntity, Long> {

}
