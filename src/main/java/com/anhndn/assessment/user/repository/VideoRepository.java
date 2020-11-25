package com.anhndn.assessment.user.repository;

import com.anhndn.assessment.user.repository.entity.VideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<VideoEntity, Long>, JpaSpecificationExecutor<VideoEntity> {
}
