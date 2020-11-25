package com.anhndn.assessment.user.repository;

import com.anhndn.assessment.user.repository.entity.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfileRepository extends JpaRepository<ProfileEntity, Long> {
    List<ProfileEntity> findByEmailAndIsDeleted(String email, Boolean isDeleted);
}
