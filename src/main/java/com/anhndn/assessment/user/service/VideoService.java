package com.anhndn.assessment.user.service;

import com.anhndn.assessment.user.repository.entity.VideoEntity;
import com.anhndn.assessment.user.service.model.VideoModel;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface VideoService {
    Page<VideoEntity> searchVideos(Map<String, String> searchCriteriaMap);
    VideoEntity createSharedVideo(VideoModel videoModel);
}
