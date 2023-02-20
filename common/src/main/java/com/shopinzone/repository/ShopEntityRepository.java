package com.shopinzone.repository;

import com.shopinzone.entity.ShopEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopEntityRepository extends JpaRepository<ShopEntity, Long> {
    List<ShopEntity> findByCityId(Long cityId);

    Optional<ShopEntity> findByCityIdAndShopId(Long cityId, Long shopId);

    @Transactional
    void deleteByCityIdAndShopId(Long cityId, Long shopId);
}
