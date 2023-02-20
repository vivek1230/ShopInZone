package com.shopinzone.repository;

import com.shopinzone.entity.CityCategoryMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityCategoryMappingRepository extends JpaRepository<CityCategoryMapping, Long> {

    List<CityCategoryMapping> findByCityId(Long cityId);

    Optional<CityCategoryMapping> findByCityIdAndCategoryId(Long cityId, Long categoryId);

    @Transactional
    void deleteByCityIdAndCategoryId(Long cityId, Long categoryId);
}
