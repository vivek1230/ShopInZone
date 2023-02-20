package com.shopinzone.repository;

import com.shopinzone.entity.CityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface CityEntityRepository extends JpaRepository<CityEntity, Long> {

    Optional<CityEntity> findByCityId(Long cityId);

    @Transactional
    void deleteByCityId(Long cityId);

}
