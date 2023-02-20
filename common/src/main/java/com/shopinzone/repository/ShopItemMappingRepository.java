package com.shopinzone.repository;

import com.shopinzone.entity.ShopItemMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopItemMappingRepository extends JpaRepository<ShopItemMapping, Long> {

}
