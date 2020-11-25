package com.anhndn.assessment.user.repository;

import com.anhndn.assessment.user.repository.entity.IdentityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IdentityRepository extends JpaRepository<IdentityEntity, Long> {
    List<IdentityEntity> findByUserNameAndIsDeleted(String username, Boolean isDeleted);
}
