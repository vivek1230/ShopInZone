package com.shopinzone.repository;

import com.shopinzone.entity.CategoryImageUrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryImageUrlMappingRepository extends JpaRepository<CategoryImageUrlMapping, Long> {

}
