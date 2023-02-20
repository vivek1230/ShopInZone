package com.shopinzone.repository;

import com.shopinzone.entity.CityCategoryShopMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityCategoryShopMappingRepository extends JpaRepository<CityCategoryShopMapping, Long> {

    List<CityCategoryShopMapping> findByCityIdAndCategoryId(Long cityId, Long categoryId);

    Optional<CityCategoryShopMapping> findByCityIdAndCategoryIdAndShopId(Long cityId, Long categoryId, Long shopId);

    @Transactional
    void deleteByCityIdAndCategoryIdAndShopId(Long cityId, Long categoryId, Long shopId);
}
