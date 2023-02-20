package com.shopinzone.repository;

import com.shopinzone.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface CategoryEntityRepository extends JpaRepository<CategoryEntity, Long> {

    Optional<CategoryEntity> findByCategoryId(Long categoryId);

    @Transactional
    void deleteByCategoryId(Long categoryId);
}
