package com.anhndn.assessment.user.service;

import com.anhndn.assessment.user.constant.PageConstant;
import com.anhndn.assessment.user.repository.VideoRepository;
import com.anhndn.assessment.user.repository.entity.VideoEntity;
import com.anhndn.assessment.user.repository.entity.VideoEntity_;
import com.anhndn.assessment.user.service.impl.VideoServiceImpl;
import com.anhndn.assessment.user.service.model.VideoModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.metamodel.SingularAttribute;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VideoServiceTest {

    private static final Long USER_ID = 5L;
    private static final String USERNAME = "username";
    private static final String VIDEO_URL = "video_url";
    private static final String VIDEO_TITLE = "video_title";
    private static final String VIDEO_DESCRIPTION = "video_description";

    @InjectMocks
    private VideoServiceImpl videoService;

    @Mock
    private VideoRepository videoRepository;

    @Test
    public void test__searchVideos__withDefaultPageRequest__shouldReturnPageOfEntity() {
        VideoEntity_.createdTimestamp = mock(SingularAttribute.class);
        when(VideoEntity_.createdTimestamp.getName()).thenReturn("createdTimestamp");
        Page<VideoEntity> videoEntityPageMock = mock(Page.class);
        when(videoRepository.findAll(any(Pageable.class))).thenReturn(videoEntityPageMock);
        Page<VideoEntity> page = videoService.searchVideos(new HashMap<>());

        assertEquals(videoEntityPageMock, page);
        ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
        verify(videoRepository, times(1)).findAll(captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(PageConstant.DEFAULT_RECORD_PER_PAGE.intValue(), captor.getValue().getPageSize());
        Sort sort = new Sort(Sort.Direction.DESC, VideoEntity_.createdTimestamp.getName());
        assertEquals(sort, captor.getValue().getSort());
    }

    @Test
    public void test__searchVideos__withPaging__shouldReturnPageOfEntity() {
        VideoEntity_.createdTimestamp = mock(SingularAttribute.class);
        when(VideoEntity_.createdTimestamp.getName()).thenReturn("createdTimestamp");
        Page<VideoEntity> videoEntityPageMock = mock(Page.class);
        when(videoRepository.findAll(any(Pageable.class))).thenReturn(videoEntityPageMock);
        Map<String, String> map = new HashMap<>();
        map.put(PageConstant.KeyName.PAGING_KEY_NAME, "true");
        map.put(PageConstant.KeyName.PAGE_INDEX_KEY_NAME, "5");
        map.put(PageConstant.KeyName.RECORD_PER_PAGE_KEY_NAME, "20");
        Page<VideoEntity> page = videoService.searchVideos(map);

        assertEquals(videoEntityPageMock, page);
        ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
        verify(videoRepository, times(1)).findAll(captor.capture());
        assertEquals(4, captor.getValue().getPageNumber());
        assertEquals(20, captor.getValue().getPageSize());
        Sort sort = new Sort(Sort.Direction.DESC, VideoEntity_.createdTimestamp.getName());
        assertEquals(sort, captor.getValue().getSort());
    }

    @Test
    public void test__searchVideos__withoutPaging__shouldReturnPageOfEntity() {
        VideoEntity_.createdTimestamp = mock(SingularAttribute.class);
        when(VideoEntity_.createdTimestamp.getName()).thenReturn("createdTimestamp");
        Page<VideoEntity> videoEntityPageMock = mock(Page.class);
        when(videoRepository.findAll(any(Pageable.class))).thenReturn(videoEntityPageMock);
        Map<String, String> map = new HashMap<>();
        map.put(PageConstant.KeyName.PAGING_KEY_NAME, "false");
        Page<VideoEntity> page = videoService.searchVideos(map);

        assertEquals(videoEntityPageMock, page);
        ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
        verify(videoRepository, times(1)).findAll(captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(Integer.MAX_VALUE, captor.getValue().getPageSize());
        Sort sort = new Sort(Sort.Direction.DESC, VideoEntity_.createdTimestamp.getName());
        assertEquals(sort, captor.getValue().getSort());
    }

    @Test
    public void test__createSharedVideo__shouldReturnCreatedEntity() {
        VideoModel model = VideoModel.builder()
                .url(VIDEO_URL)
                .sharedByUserId(USER_ID)
                .sharedByUserUsername(USERNAME)
                .title(VIDEO_TITLE)
                .description(VIDEO_DESCRIPTION)
                .build();
        when(videoRepository.save(any(VideoEntity.class))).then(AdditionalAnswers.returnsFirstArg());

        VideoEntity videoEntity = videoService.createSharedVideo(model);

        ArgumentCaptor<VideoEntity> captor = ArgumentCaptor.forClass(VideoEntity.class);
        verify(videoRepository, times(1)).save(captor.capture());
        assertEquals(captor.getValue(), videoEntity);
        assertEquals(USER_ID, captor.getValue().getSharedByUserId());
        assertEquals(USERNAME, captor.getValue().getSharedByUserUsername());
        assertEquals(VIDEO_URL, captor.getValue().getUrl());
        assertEquals(VIDEO_TITLE, captor.getValue().getTitle());
        assertEquals(VIDEO_DESCRIPTION, captor.getValue().getDescription());
    }

}