package com.shopinzone.repository;

import com.shopinzone.entity.OrderItemMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemMappingRepository extends JpaRepository<OrderItemMapping, Long> {

}
