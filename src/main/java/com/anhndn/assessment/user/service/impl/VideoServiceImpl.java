package com.anhndn.assessment.user.service.impl;

import com.anhndn.assessment.user.repository.VideoRepository;
import com.anhndn.assessment.user.repository.entity.VideoEntity;
import com.anhndn.assessment.user.repository.entity.VideoEntity_;
import com.anhndn.assessment.user.service.VideoService;
import com.anhndn.assessment.user.service.model.VideoModel;
import com.anhndn.assessment.user.util.PageRequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class VideoServiceImpl implements VideoService {

    @Autowired
    private VideoRepository videoRepository;

    public Page<VideoEntity> searchVideos(Map<String, String> searchCriteriaMap) {
        log.info("search shared videos");

        Sort sort = new Sort(Sort.Direction.DESC, VideoEntity_.createdTimestamp.getName());
        PageRequest pageable = PageRequestUtil.buildPageRequest(searchCriteriaMap, sort);
        log.info("Searching shared videos in database");
        Page<VideoEntity> videoEntities = videoRepository.findAll(pageable);
        log.info("Found [{}] shared video(s)", videoEntities.getTotalElements());

        return videoEntities;
    }

    public VideoEntity createSharedVideo(VideoModel model) {
        log.info("user id [{}] share video with url [{}]", model.getSharedByUserId(), model.getUrl());
        VideoEntity entity = VideoEntity.builder()
                .url(model.getUrl())
                .sharedByUserId(model.getSharedByUserId())
                .sharedByUserUsername(model.getSharedByUserUsername())
                .title(model.getTitle())
                .description(model.getDescription())
                .votedUpCount(0L)
                .votedDownCount(0L)
                .build();
        videoRepository.save(entity);
        log.info("user id [{}] share video with url [{}] successfully, videoId [{}]", model.getSharedByUserId(), model.getUrl(), entity.getId());

        return entity;
    }

}
