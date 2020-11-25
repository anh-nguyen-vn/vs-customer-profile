package com.anhndn.assessment.user.controller;

import com.anhndn.assessment.user.UserApplication;
import com.anhndn.assessment.user.config.RsaConfiguration;
import com.anhndn.assessment.user.config.WebMvcConfiguration;
import com.anhndn.assessment.user.controller.request.IdentityCreateRequest;
import com.anhndn.assessment.user.controller.request.ProfileCreateRequest;
import com.anhndn.assessment.user.controller.request.UserCreateRequest;
import com.anhndn.assessment.user.controller.response.DefaultCreateResponse;
import com.anhndn.assessment.user.domain.GeneralResponse;
import com.anhndn.assessment.user.domain.ResponseStatus;
import com.anhndn.assessment.user.exception.BadRequestClientErrorException;
import com.anhndn.assessment.user.exception.RsaDecryptException;
import com.anhndn.assessment.user.factory.ResponseFactory;
import com.anhndn.assessment.user.interceptor.PayloadInterceptor;
import com.anhndn.assessment.user.repository.entity.IdentityEntity;
import com.anhndn.assessment.user.repository.entity.ProfileEntity;
import com.anhndn.assessment.user.service.impl.IdentityServiceImpl;
import com.anhndn.assessment.user.service.impl.RsaDecryptServiceImpl;
import com.anhndn.assessment.user.service.impl.UserServiceImpl;
import com.anhndn.assessment.user.service.model.IdentityModel;
import com.anhndn.assessment.user.service.model.ProfileModel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
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
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@RunWith(SpringRunner.class)
@WebMvcTest(
        value = UserController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE),
        excludeAutoConfiguration = MockMvcSecurityAutoConfiguration.class)
@ContextConfiguration(
        classes = {
                UserApplication.class,
                RsaConfiguration.class,
                WebMvcConfiguration.class,
                PayloadInterceptor.class,
        })
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
public class UserControllerTest {

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "email@email.com";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String CREATE_USER_URL = "/v1.0/users";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserServiceImpl userServiceMock;

    @MockBean
    private IdentityServiceImpl identityServiceMock;

    @MockBean
    private RsaDecryptServiceImpl rsaDecryptServiceMock;

    @MockBean
    private ResponseFactory responseFactoryMock;

    @Before
    public void setUp() {
        when(responseFactoryMock.success()).thenCallRealMethod();
        when(responseFactoryMock.success(any(), any(Class.class))).thenCallRealMethod();
    }

    @Test
    public void test__createUserProfile__shouldReturnSuccess() throws Exception {
        ProfileCreateRequest profileCreateRequest = new ProfileCreateRequest(EMAIL);
        IdentityCreateRequest identityCreateRequest = new IdentityCreateRequest(USERNAME, PASSWORD);
        UserCreateRequest request = new UserCreateRequest(profileCreateRequest, identityCreateRequest);
        when(userServiceMock.getByEmail(anyString())).thenReturn(null);
        when(rsaDecryptServiceMock.decrypt(anyString())).thenReturn(PASSWORD);
        when(identityServiceMock.getByUsername(anyString())).thenReturn(null);
        ProfileEntity profileEntity = mock(ProfileEntity.class);
        when(profileEntity.getId()).thenReturn(USER_ID);
        when(userServiceMock.createUser(any(ProfileModel.class), any(IdentityModel.class))).thenReturn(profileEntity);

        ResultMatcher successExpectation = MockMvcResultMatchers.status().isOk();

        MockHttpServletResponse response = mockMvc.perform(
                MockMvcRequestBuilders.post(CREATE_USER_URL)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .servletPath(CREATE_USER_URL)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .headers(new HttpHeaders())
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(successExpectation)
                .andDo(print())
                .andReturn().getResponse();

        GeneralResponse<DefaultCreateResponse> generalResponse = objectMapper.readValue(response.getContentAsString(), new TypeReference<GeneralResponse<DefaultCreateResponse>>() {
        });
        assertEquals(ResponseStatus.SUCCESS.getCode(), generalResponse.getStatus().getCode());
        assertEquals(ResponseStatus.SUCCESS.getMessage(), generalResponse.getStatus().getMessage());
        assertEquals(USER_ID, generalResponse.getData().getId());
        verify(userServiceMock, times(1)).getByEmail(EMAIL);
        verify(identityServiceMock, times(1)).getByUsername(USERNAME);
        ArgumentCaptor<ProfileModel> profileModelArgumentCaptor = ArgumentCaptor.forClass(ProfileModel.class);
        ArgumentCaptor<IdentityModel> identityModelArgumentCaptor = ArgumentCaptor.forClass(IdentityModel.class);
        verify(userServiceMock, times(1)).createUser(profileModelArgumentCaptor.capture(), identityModelArgumentCaptor.capture());
        assertEquals(EMAIL, profileModelArgumentCaptor.getValue().getEmail());
        assertEquals(USERNAME, identityModelArgumentCaptor.getValue().getUsername());
        assertEquals(PASSWORD, identityModelArgumentCaptor.getValue().getPassword());
    }

    @Test
    public void test__createUserProfile__profileRequestEmailNull__shouldReturnError() throws Exception {
        ProfileCreateRequest profileCreateRequest = new ProfileCreateRequest();
        IdentityCreateRequest identityCreateRequest = new IdentityCreateRequest(USERNAME, PASSWORD);
        UserCreateRequest request = new UserCreateRequest(profileCreateRequest, identityCreateRequest);

        Exception exception = mockMvc.perform(
                MockMvcRequestBuilders.post(CREATE_USER_URL)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .servletPath(CREATE_USER_URL)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .headers(new HttpHeaders())
                        .content(objectMapper.writeValueAsString(request))
        )
                .andDo(print())
                .andReturn()
                .getResolvedException();

        assertTrue(exception instanceof MethodArgumentNotValidException);
        MethodArgumentNotValidException methodArgumentNotValidException = (MethodArgumentNotValidException) exception;
        assertEquals(1, methodArgumentNotValidException.getBindingResult().getErrorCount());
        verify(userServiceMock, never()).getByEmail(anyString());
        verify(identityServiceMock, never()).getByUsername(anyString());
        verify(userServiceMock, never()).createUser(any(), any());
    }

    @Test
    public void test__createUserProfile__profileRequestEmailDuplicated__shouldReturnError() throws Exception {
        ProfileCreateRequest profileCreateRequest = new ProfileCreateRequest(EMAIL);
        IdentityCreateRequest identityCreateRequest = new IdentityCreateRequest(USERNAME, PASSWORD);
        UserCreateRequest request = new UserCreateRequest(profileCreateRequest, identityCreateRequest);
        when(userServiceMock.getByEmail(anyString())).thenReturn(mock(ProfileEntity.class));

        Exception exception = mockMvc.perform(
                MockMvcRequestBuilders.post(CREATE_USER_URL)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .servletPath(CREATE_USER_URL)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .headers(new HttpHeaders())
                        .content(objectMapper.writeValueAsString(request))
        )
                .andDo(print())
                .andReturn()
                .getResolvedException();

        assertTrue(exception instanceof BadRequestClientErrorException);
        BadRequestClientErrorException badRequestClientErrorException = (BadRequestClientErrorException) exception;
        assertEquals(ResponseStatus.EMAIL_DUPLICATED, badRequestClientErrorException.getErrorStatus());
        verify(userServiceMock, times(1)).getByEmail(EMAIL);
        verify(identityServiceMock, never()).getByUsername(anyString());
        verify(userServiceMock, never()).createUser(any(), any());
    }

    @Test
    public void test__createUserProfile__identityUsernameAndPasswordNull__shouldReturnError() throws Exception {
        ProfileCreateRequest profileCreateRequest = new ProfileCreateRequest(EMAIL);
        IdentityCreateRequest identityCreateRequest = new IdentityCreateRequest(null, null);
        UserCreateRequest request = new UserCreateRequest(profileCreateRequest, identityCreateRequest);
        when(userServiceMock.getByEmail(anyString())).thenReturn(mock(ProfileEntity.class));

        Exception exception = mockMvc.perform(
                MockMvcRequestBuilders.post(CREATE_USER_URL)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .servletPath(CREATE_USER_URL)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .headers(new HttpHeaders())
                        .content(objectMapper.writeValueAsString(request))
        )
                .andDo(print())
                .andReturn()
                .getResolvedException();

        assertTrue(exception instanceof MethodArgumentNotValidException);
        MethodArgumentNotValidException methodArgumentNotValidException = (MethodArgumentNotValidException) exception;
        assertEquals(2, methodArgumentNotValidException.getBindingResult().getErrorCount());
        verify(userServiceMock, never()).getByEmail(anyString());
        verify(identityServiceMock, never()).getByUsername(anyString());
        verify(userServiceMock, never()).createUser(any(), any());
    }

    @Test
    public void test__createUserProfile__identityPasswordNotInRsaFormat__shouldReturnError() throws Exception {
        ProfileCreateRequest profileCreateRequest = new ProfileCreateRequest(EMAIL);
        IdentityCreateRequest identityCreateRequest = new IdentityCreateRequest(USERNAME, PASSWORD);
        UserCreateRequest request = new UserCreateRequest(profileCreateRequest, identityCreateRequest);
        when(userServiceMock.getByEmail(anyString())).thenReturn(null);
        when(rsaDecryptServiceMock.decrypt(anyString())).thenThrow(mock(RsaDecryptException.class));

        Exception exception = mockMvc.perform(
                MockMvcRequestBuilders.post(CREATE_USER_URL)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .servletPath(CREATE_USER_URL)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .headers(new HttpHeaders())
                        .content(objectMapper.writeValueAsString(request))
        )
                .andDo(print())
                .andReturn()
                .getResolvedException();

        assertTrue(exception instanceof BadRequestClientErrorException);
        BadRequestClientErrorException badRequestClientErrorException = (BadRequestClientErrorException) exception;
        assertEquals(ResponseStatus.RSA_DECRYPT_ERROR, badRequestClientErrorException.getErrorStatus());
        verify(userServiceMock, times(1)).getByEmail(EMAIL);
        verify(identityServiceMock, never()).getByUsername(anyString());
        verify(userServiceMock, never()).createUser(any(), any());
    }

    @Test
    public void test__createUserProfile__plainPasswordEmpty__shouldReturnError() throws Exception {
        ProfileCreateRequest profileCreateRequest = new ProfileCreateRequest(EMAIL);
        IdentityCreateRequest identityCreateRequest = new IdentityCreateRequest(USERNAME, PASSWORD);
        UserCreateRequest request = new UserCreateRequest(profileCreateRequest, identityCreateRequest);
        when(userServiceMock.getByEmail(anyString())).thenReturn(null);
        when(rsaDecryptServiceMock.decrypt(anyString())).thenReturn("");

        Exception exception = mockMvc.perform(
                MockMvcRequestBuilders.post(CREATE_USER_URL)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .servletPath(CREATE_USER_URL)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .headers(new HttpHeaders())
                        .content(objectMapper.writeValueAsString(request))
        )
                .andDo(print())
                .andReturn()
                .getResolvedException();

        assertTrue(exception instanceof BadRequestClientErrorException);
        BadRequestClientErrorException badRequestClientErrorException = (BadRequestClientErrorException) exception;
        assertEquals(ResponseStatus.PASSWORD_REQUIRED, badRequestClientErrorException.getErrorStatus());
        verify(userServiceMock, times(1)).getByEmail(EMAIL);
        verify(identityServiceMock, never()).getByUsername(anyString());
        verify(userServiceMock, never()).createUser(any(), any());
    }

    @Test
    public void test__createUserProfile__identityUsernameDuplicated__shouldReturnError() throws Exception {
        ProfileCreateRequest profileCreateRequest = new ProfileCreateRequest(EMAIL);
        IdentityCreateRequest identityCreateRequest = new IdentityCreateRequest(USERNAME, PASSWORD);
        UserCreateRequest request = new UserCreateRequest(profileCreateRequest, identityCreateRequest);
        when(userServiceMock.getByEmail(anyString())).thenReturn(null);
        when(rsaDecryptServiceMock.decrypt(anyString())).thenReturn(PASSWORD);
        when(identityServiceMock.getByUsername(anyString())).thenReturn(mock(IdentityEntity.class));

        Exception exception = mockMvc.perform(
                MockMvcRequestBuilders.post(CREATE_USER_URL)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .servletPath(CREATE_USER_URL)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .headers(new HttpHeaders())
                        .content(objectMapper.writeValueAsString(request))
        )
                .andDo(print())
                .andReturn()
                .getResolvedException();

        assertTrue(exception instanceof BadRequestClientErrorException);
        BadRequestClientErrorException badRequestClientErrorException = (BadRequestClientErrorException) exception;
        assertEquals(ResponseStatus.USERNAME_DUPLICATED, badRequestClientErrorException.getErrorStatus());
        verify(userServiceMock, times(1)).getByEmail(EMAIL);
        verify(identityServiceMock, times(1)).getByUsername(USERNAME);
        verify(userServiceMock, never()).createUser(any(), any());
    }
}
