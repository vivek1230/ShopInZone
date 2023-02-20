package com.shopinzone.repository;

import com.shopinzone.entity.ItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemEntityRepository extends JpaRepository<ItemEntity, Long> {

}
