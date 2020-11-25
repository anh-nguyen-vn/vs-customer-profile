package com.anhndn.assessment.user.controller;

import com.anhndn.assessment.user.aspect.LogAround;
import com.anhndn.assessment.user.constant.PageConstant;
import com.anhndn.assessment.user.constant.PayloadConstant;
import com.anhndn.assessment.user.controller.request.VideoShareCreateRequest;
import com.anhndn.assessment.user.controller.response.DefaultCreateResponse;
import com.anhndn.assessment.user.controller.response.PageResponse;
import com.anhndn.assessment.user.controller.response.VideoListResponse;
import com.anhndn.assessment.user.domain.GeneralResponse;
import com.anhndn.assessment.user.domain.Payload;
import com.anhndn.assessment.user.domain.ResponseStatus;
import com.anhndn.assessment.user.exception.BadRequestClientErrorException;
import com.anhndn.assessment.user.factory.ResponseFactory;
import com.anhndn.assessment.user.repository.entity.VideoEntity;
import com.anhndn.assessment.user.service.VideoService;
import com.anhndn.assessment.user.service.model.VideoModel;
import com.anhndn.assessment.user.util.PageResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class VideoSharingController {

    private static final List<String> SEARCH_VIDEO_ALLOW_KEY_LIST = Arrays.asList(
            PageConstant.KeyName.PAGE_INDEX_KEY_NAME,
            PageConstant.KeyName.RECORD_PER_PAGE_KEY_NAME,
            PageConstant.KeyName.PAGING_KEY_NAME
    );

    @Autowired
    private VideoService videoService;

    @Autowired
    private ResponseFactory responseFactory;

    @LogAround(message = "get list of shared videos")
    @GetMapping(value = "v${api.version}/videos")
    public ResponseEntity<GeneralResponse<VideoListResponse>> getSharedVideos(@RequestParam Map<String, String> mapParams) {
        for (Map.Entry<String, String> searchEntry : mapParams.entrySet()) {
            if (!SEARCH_VIDEO_ALLOW_KEY_LIST.contains(searchEntry.getKey())) {
                log.error("Do not allow to search with key [{}]", searchEntry.getKey());
                throw new BadRequestClientErrorException(ResponseStatus.REQUEST_VALIDATION_ERROR);
            }
        }

        Page<VideoEntity> videoEntities = videoService.searchVideos(mapParams);

        List<VideoListResponse.Data> data = videoEntities.stream()
                .map(this::buildVideoListResponseData)
                .collect(Collectors.toList());

        PageResponse page = PageResponseUtil.buildPageResponse(videoEntities);

        VideoListResponse videoListResponse = VideoListResponse.builder()
                .data(data)
                .page(page)
                .build();

        ResponseEntity success = responseFactory.success(videoListResponse, VideoListResponse.class);
        return success;
    }

    @PostMapping(value = "v${api.version}/videos")
    public ResponseEntity<GeneralResponse<DefaultCreateResponse>> shareVideo(@RequestAttribute(PayloadConstant.PAYLOAD_KEY) Payload payload,
                                                                             @RequestBody VideoShareCreateRequest request) {
        VideoModel model = this.buildVideoModel(payload, request);

        VideoEntity entity = videoService.createSharedVideo(model);

        ResponseEntity success = responseFactory.success(new DefaultCreateResponse(entity.getId()), DefaultCreateResponse.class);
        return success;
    }

    @PostMapping(value = "v{api.version}/videos/{video_id}/comments")

    private VideoListResponse.Data buildVideoListResponseData(VideoEntity entity) {
        return VideoListResponse.Data.builder()
                .id(entity.getId())
                .url(entity.getUrl())
                .sharedByUserId(entity.getSharedByUserId())
                .sharedByUserUsername(entity.getSharedByUserUsername())
                .votedUpCount(entity.getVotedUpCount())
                .votedDownCount(entity.getVotedDownCount())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .createdTimestamp(entity.getCreatedTimestamp())
                .build();
    }

    private VideoModel buildVideoModel(Payload payload, VideoShareCreateRequest request) {
        return VideoModel.builder()
                .sharedByUserId(payload.getUserId())
                .sharedByUserUsername(payload.getUsername())
                .url(request.getUrl())
                .title(request.getTitle())
                .description(request.getDescription())
                .build();
    }
}
