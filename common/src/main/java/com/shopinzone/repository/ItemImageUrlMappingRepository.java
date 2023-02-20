package com.shopinzone.repository;

import com.shopinzone.entity.ItemImageUrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemImageUrlMappingRepository extends JpaRepository<ItemImageUrlMapping, Long> {

}
