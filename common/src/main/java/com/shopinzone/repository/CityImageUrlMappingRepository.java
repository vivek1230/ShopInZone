package com.shopinzone.repository;

import com.shopinzone.entity.CityImageUrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CityImageUrlMappingRepository extends JpaRepository<CityImageUrlMapping, Long> {

    List<CityImageUrlMapping> findAllByCityId(Long cityId);

    @Transactional
    void deleteAllByCityId(Long cityId);

}
