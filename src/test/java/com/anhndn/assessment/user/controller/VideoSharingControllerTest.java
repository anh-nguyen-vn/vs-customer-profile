package com.anhndn.assessment.user.controller;

import com.anhndn.assessment.user.UserApplication;
import com.anhndn.assessment.user.config.WebMvcConfiguration;
import com.anhndn.assessment.user.constant.PayloadConstant;
import com.anhndn.assessment.user.controller.request.VideoShareCreateRequest;
import com.anhndn.assessment.user.controller.response.DefaultCreateResponse;
import com.anhndn.assessment.user.controller.response.VideoListResponse;
import com.anhndn.assessment.user.domain.GeneralResponse;
import com.anhndn.assessment.user.domain.ResponseStatus;
import com.anhndn.assessment.user.exception.BadRequestClientErrorException;
import com.anhndn.assessment.user.factory.ResponseFactory;
import com.anhndn.assessment.user.interceptor.PayloadInterceptor;
import com.anhndn.assessment.user.repository.entity.VideoEntity;
import com.anhndn.assessment.user.service.impl.VideoServiceImpl;
import com.anhndn.assessment.user.service.model.VideoModel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ImportAutoConfiguration(ExceptionHandlerController.class)
@RunWith(SpringRunner.class)
@WebMvcTest(
        value = VideoSharingController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE),
        excludeAutoConfiguration = MockMvcSecurityAutoConfiguration.class)
@ContextConfiguration(
        classes = {
                UserApplication.class,
                WebMvcConfiguration.class,
                PayloadInterceptor.class,
                ExceptionHandlerController.class,
        })
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
public class VideoSharingControllerTest {

    private static final Long USER_ID = 1L;
    private static final String USERNAME = "username";
    private static final int PAGE_NUMBER = 0;
    private static final int TOTAL_PAGE = 1;
    private static final boolean HAS_NEXT = false;
    private static final boolean HAS_PREVIOUS = false;
    private static final int PAGE_SIZE = 10;
    private static final long TOTAL_ELEMENT = 5;
    private static final String SEARCH_VIDEOS_URL = "/v1.0/videos";
    private static final String SHARE_VIDEOS_URL = "/v1.0/videos";
    private static final Long VIDEO_ID = 5L;
    private static final String VIDEO_URL = "video_url";
    private static final String VIDEO_TITLE = "video_title";
    private static final String VIDEO_DESCRIPTION = "video_description";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VideoServiceImpl videoServiceMock;

    @MockBean
    private ResponseFactory responseFactoryMock;

    @Before
    public void setUp() {
        when(responseFactoryMock.success()).thenCallRealMethod();
        when(responseFactoryMock.success(any(), any(Class.class))).thenCallRealMethod();
    }

    @Test
    public void test__getSharedVideos__shouldReturnSuccess() throws Exception {
        Page<VideoEntity> videoEntityPage = mock(Page.class);
        when(videoEntityPage.getNumber()).thenReturn(PAGE_NUMBER);
        when(videoEntityPage.getTotalPages()).thenReturn(TOTAL_PAGE);
        when(videoEntityPage.hasNext()).thenReturn(HAS_NEXT);
        when(videoEntityPage.hasPrevious()).thenReturn(HAS_PREVIOUS);
        when(videoEntityPage.getSize()).thenReturn(PAGE_SIZE);
        when(videoEntityPage.getTotalElements()).thenReturn(TOTAL_ELEMENT);
        VideoEntity videoEntity = mock(VideoEntity.class);
        when(videoEntity.getId()).thenReturn(VIDEO_ID);
        when(videoEntity.getUrl()).thenReturn(VIDEO_URL);
        when(videoEntityPage.stream()).thenReturn(Stream.of(videoEntity));
        when(videoServiceMock.searchVideos(any(Map.class))).thenReturn(videoEntityPage);

        ResultMatcher successExpectation = MockMvcResultMatchers.status().isOk();

        MockHttpServletResponse response = mockMvc.perform(
                MockMvcRequestBuilders.get(SEARCH_VIDEOS_URL)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .servletPath(SEARCH_VIDEOS_URL)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .headers(new HttpHeaders())
        )
                .andExpect(successExpectation)
                .andDo(print())
                .andReturn().getResponse();

        GeneralResponse<VideoListResponse> generalResponse = objectMapper.readValue(response.getContentAsString(), new TypeReference<GeneralResponse<VideoListResponse>>() {
        });
        assertEquals(ResponseStatus.SUCCESS.getCode(), generalResponse.getStatus().getCode());
        assertEquals(ResponseStatus.SUCCESS.getMessage(), generalResponse.getStatus().getMessage());
        assertEquals(PAGE_NUMBER + 1, generalResponse.getData().getPage().getCurrentPage().intValue());
        assertEquals(TOTAL_PAGE, generalResponse.getData().getPage().getTotalPages().intValue());
        assertEquals(HAS_NEXT, generalResponse.getData().getPage().getHasNext());
        assertEquals(HAS_PREVIOUS, generalResponse.getData().getPage().getHasPrevious());
        assertEquals(PAGE_SIZE, generalResponse.getData().getPage().getPageSize().intValue());
        assertEquals(TOTAL_ELEMENT, generalResponse.getData().getPage().getTotalElements().longValue());
        assertEquals(VIDEO_ID, generalResponse.getData().getData().get(0).getId());
        assertEquals(VIDEO_URL, generalResponse.getData().getData().get(0).getUrl());
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(videoServiceMock, times(1)).searchVideos(captor.capture());
        assertEquals(0, captor.getValue().size());
    }

    @Test
    public void test__getSharedVideos__invalidSearchCriteria__shouldReturnError() throws Exception {
        // Query parameters
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(SEARCH_VIDEOS_URL)
                // Add query parameter
                .queryParam("invalid_key", "123");
        Exception exception = mockMvc.perform(
                MockMvcRequestBuilders.get(builder.toUriString())
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .servletPath(SEARCH_VIDEOS_URL)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .headers(new HttpHeaders())
        )
                .andDo(print())
                .andReturn()
                .getResolvedException();

        assertTrue(exception instanceof BadRequestClientErrorException);
        BadRequestClientErrorException badRequestClientErrorException = (BadRequestClientErrorException) exception;
        assertEquals(ResponseStatus.REQUEST_VALIDATION_ERROR, badRequestClientErrorException.getErrorStatus());
        verify(videoServiceMock, never()).searchVideos(any());
    }

    @Test
    public void test__shareVideo__shouldReturnSuccess() throws Exception {
        VideoShareCreateRequest request = new VideoShareCreateRequest(VIDEO_URL, VIDEO_TITLE, VIDEO_DESCRIPTION);
        VideoEntity videoEntity = mock(VideoEntity.class);
        when(videoEntity.getId()).thenReturn(VIDEO_ID);
        when(videoEntity.getUrl()).thenReturn(VIDEO_URL);
        when(videoServiceMock.createSharedVideo(any(VideoModel.class))).thenReturn(videoEntity);

        ResultMatcher successExpectation = MockMvcResultMatchers.status().isOk();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(PayloadConstant.USER_ID_KEY, String.valueOf(USER_ID));
        httpHeaders.add(PayloadConstant.USERNAME_KEY, USERNAME);
        MockHttpServletResponse response = mockMvc.perform(
                MockMvcRequestBuilders.post(SHARE_VIDEOS_URL)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .servletPath(SHARE_VIDEOS_URL)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .headers(httpHeaders)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(successExpectation)
                .andDo(print())
                .andReturn().getResponse();

        GeneralResponse<DefaultCreateResponse> generalResponse = objectMapper.readValue(response.getContentAsString(), new TypeReference<GeneralResponse<DefaultCreateResponse>>() {
        });
        assertEquals(ResponseStatus.SUCCESS.getCode(), generalResponse.getStatus().getCode());
        assertEquals(ResponseStatus.SUCCESS.getMessage(), generalResponse.getStatus().getMessage());
        assertEquals(VIDEO_ID, generalResponse.getData().getId());
        ArgumentCaptor<VideoModel> captor = ArgumentCaptor.forClass(VideoModel.class);
        verify(videoServiceMock, times(1)).createSharedVideo(captor.capture());
        assertEquals(USER_ID, captor.getValue().getSharedByUserId());
        assertEquals(USERNAME, captor.getValue().getSharedByUserUsername());
        assertEquals(VIDEO_URL, captor.getValue().getUrl());
        assertEquals(VIDEO_TITLE, captor.getValue().getTitle());
        assertEquals(VIDEO_DESCRIPTION, captor.getValue().getDescription());
    }
}