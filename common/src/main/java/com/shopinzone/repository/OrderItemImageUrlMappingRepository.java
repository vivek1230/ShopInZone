package com.shopinzone.repository;

import com.shopinzone.entity.OrderItemImageUrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemImageUrlMappingRepository extends JpaRepository<OrderItemImageUrlMapping, Long> {

}
