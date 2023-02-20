package com.shopinzone.repository;

import com.shopinzone.entity.CartItemImageUrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemImageUrlMappingRepository extends JpaRepository<CartItemImageUrlMapping, Long> {

}