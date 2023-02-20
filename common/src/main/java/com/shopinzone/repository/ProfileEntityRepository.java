package com.shopinzone.repository;

import com.shopinzone.entity.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ProfileEntityRepository extends JpaRepository<ProfileEntity, Long> {

    Optional<ProfileEntity> findByEmail(String email);

    @Transactional
    void deleteByEmail(String email);

}
