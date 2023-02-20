package com.shopinzone.repository;

import com.shopinzone.entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressEntityRepository extends JpaRepository<AddressEntity, Long> {

    List<AddressEntity> findByEmail(String email);

    Optional<AddressEntity> findByAddressIdAndEmail(Long addressId, String email);

    @Transactional
    void deleteByAddressIdAndEmail(Long addressId, String email);
}
