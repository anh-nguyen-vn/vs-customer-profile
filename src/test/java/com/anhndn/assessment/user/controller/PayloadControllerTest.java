package com.anhndn.assessment.user.controller;

import com.anhndn.assessment.user.UserApplication;
import com.anhndn.assessment.user.config.RsaConfiguration;
import com.anhndn.assessment.user.config.WebMvcConfiguration;
import com.anhndn.assessment.user.controller.request.PayloadCreateRequest;
import com.anhndn.assessment.user.domain.GeneralResponse;
import com.anhndn.assessment.user.domain.Payload;
import com.anhndn.assessment.user.domain.ResponseStatus;
import com.anhndn.assessment.user.domain.UserType;
import com.anhndn.assessment.user.exception.BadRequestClientErrorException;
import com.anhndn.assessment.user.exception.RsaDecryptException;
import com.anhndn.assessment.user.factory.ResponseFactory;
import com.anhndn.assessment.user.interceptor.PayloadInterceptor;
import com.anhndn.assessment.user.repository.entity.IdentityEntity;
import com.anhndn.assessment.user.repository.entity.ProfileEntity;
import com.anhndn.assessment.user.service.impl.IdentityServiceImpl;
import com.anhndn.assessment.user.service.impl.RsaDecryptServiceImpl;
import com.anhndn.assessment.user.service.impl.UserServiceImpl;
import com.anhndn.assessment.user.util.EncryptionUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
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

@RunWith(SpringRunner.class)
@WebMvcTest(
        value = PayloadController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE),
        excludeAutoConfiguration = MockMvcSecurityAutoConfiguration.class)
@ContextConfiguration(
        classes = {
                UserApplication.class,
                WebMvcConfiguration.class,
                RsaConfiguration.class,
                PayloadInterceptor.class,
        })
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
public class PayloadControllerTest {

    private static final Long USER_ID = 1L;
    private static final Long IDENTITY_ID = 2L;
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String CREATE_PAYLOAD_URL = "/internal/users/payloads";

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
    public void test__createPayload__shouldReturnSuccess() throws Exception {
        PayloadCreateRequest request = new PayloadCreateRequest(USERNAME, PASSWORD);
        when(rsaDecryptServiceMock.decrypt(anyString())).thenReturn(PASSWORD);
        IdentityEntity identityEntity = mock(IdentityEntity.class);
        when(identityEntity.getId()).thenReturn(IDENTITY_ID);
        when(identityEntity.getPassword()).thenReturn(EncryptionUtil.sha256(PASSWORD));
        when(identityEntity.getProfileId()).thenReturn(USER_ID);
        when(identityServiceMock.getByUsername(anyString())).thenReturn(identityEntity);
        ProfileEntity profileEntity = mock(ProfileEntity.class);
        when(profileEntity.getId()).thenReturn(USER_ID);
        when(profileEntity.getIsDeleted()).thenReturn(false);
        when(userServiceMock.getById(anyLong())).thenReturn(profileEntity);

        ResultMatcher successExpectation = MockMvcResultMatchers.status().isOk();

        MockHttpServletResponse response = mockMvc.perform(
                MockMvcRequestBuilders.post(CREATE_PAYLOAD_URL)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .servletPath(CREATE_PAYLOAD_URL)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .headers(new HttpHeaders())
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(successExpectation)
                .andDo(print())
                .andReturn().getResponse();

        GeneralResponse<Payload> generalResponse = objectMapper.readValue(response.getContentAsString(), new TypeReference<GeneralResponse<Payload>>() {
        });
        assertEquals(ResponseStatus.SUCCESS.getCode(), generalResponse.getStatus().getCode());
        assertEquals(ResponseStatus.SUCCESS.getMessage(), generalResponse.getStatus().getMessage());
        assertEquals(USER_ID, generalResponse.getData().getUserId());
        assertEquals(UserType.USER, generalResponse.getData().getUserType());
        verify(userServiceMock, times(1)).getById(USER_ID);
        verify(identityServiceMock, times(1)).getByUsername(USERNAME);
    }

    @Test
    public void test__createPayload__passwordNotInRsaFormat__shouldReturnError() throws Exception {
        PayloadCreateRequest request = new PayloadCreateRequest(USERNAME, PASSWORD);
        when(rsaDecryptServiceMock.decrypt(anyString())).thenThrow(mock(RsaDecryptException.class));

        Exception exception = mockMvc.perform(
                MockMvcRequestBuilders.post(CREATE_PAYLOAD_URL)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .servletPath(CREATE_PAYLOAD_URL)
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
        verify(identityServiceMock, never()).getByUsername(anyString());
        verify(userServiceMock, never()).getById(anyLong());
    }

    @Test
    public void test__createPayload__usernameNotFound__shouldReturnError() throws Exception {
        PayloadCreateRequest request = new PayloadCreateRequest(USERNAME, PASSWORD);
        when(rsaDecryptServiceMock.decrypt(anyString())).thenReturn(PASSWORD);
        when(identityServiceMock.getByUsername(anyString())).thenReturn(null);

        Exception exception = mockMvc.perform(
                MockMvcRequestBuilders.post(CREATE_PAYLOAD_URL)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .servletPath(CREATE_PAYLOAD_URL)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .headers(new HttpHeaders())
                        .content(objectMapper.writeValueAsString(request))
        )
                .andDo(print())
                .andReturn()
                .getResolvedException();

        assertTrue(exception instanceof BadRequestClientErrorException);
        BadRequestClientErrorException badRequestClientErrorException = (BadRequestClientErrorException) exception;
        assertEquals(ResponseStatus.INVALID_CREDENTIAL, badRequestClientErrorException.getErrorStatus());
        verify(identityServiceMock, times(1)).getByUsername(USERNAME);
        verify(userServiceMock, never()).getById(anyLong());
    }

    @Test
    public void test__createPayload__passwordMismatch__shouldReturnError() throws Exception {
        PayloadCreateRequest request = new PayloadCreateRequest(USERNAME, PASSWORD);
        when(rsaDecryptServiceMock.decrypt(anyString())).thenReturn(PASSWORD);
        IdentityEntity identityEntity = mock(IdentityEntity.class);
        when(identityEntity.getId()).thenReturn(IDENTITY_ID);
        when(identityEntity.getPassword()).thenReturn("mismatch_password");
        when(identityEntity.getProfileId()).thenReturn(USER_ID);
        when(identityServiceMock.getByUsername(anyString())).thenReturn(identityEntity);

        Exception exception = mockMvc.perform(
                MockMvcRequestBuilders.post(CREATE_PAYLOAD_URL)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .servletPath(CREATE_PAYLOAD_URL)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .headers(new HttpHeaders())
                        .content(objectMapper.writeValueAsString(request))
        )
                .andDo(print())
                .andReturn()
                .getResolvedException();

        assertTrue(exception instanceof BadRequestClientErrorException);
        BadRequestClientErrorException badRequestClientErrorException = (BadRequestClientErrorException) exception;
        assertEquals(ResponseStatus.INVALID_CREDENTIAL, badRequestClientErrorException.getErrorStatus());
        verify(identityServiceMock, times(1)).getByUsername(USERNAME);
        verify(userServiceMock, never()).getById(anyLong());
    }

    @Test
    public void test__createPayload__userProfileDeleted__shouldReturnError() throws Exception {
        PayloadCreateRequest request = new PayloadCreateRequest(USERNAME, PASSWORD);
        when(rsaDecryptServiceMock.decrypt(anyString())).thenReturn(PASSWORD);
        IdentityEntity identityEntity = mock(IdentityEntity.class);
        when(identityEntity.getId()).thenReturn(IDENTITY_ID);
        when(identityEntity.getPassword()).thenReturn(EncryptionUtil.sha256(PASSWORD));
        when(identityEntity.getProfileId()).thenReturn(USER_ID);
        when(identityServiceMock.getByUsername(anyString())).thenReturn(identityEntity);
        ProfileEntity profileEntity = mock(ProfileEntity.class);
        when(profileEntity.getId()).thenReturn(USER_ID);
        when(profileEntity.getIsDeleted()).thenReturn(true);
        when(userServiceMock.getById(anyLong())).thenReturn(profileEntity);

        Exception exception = mockMvc.perform(
                MockMvcRequestBuilders.post(CREATE_PAYLOAD_URL)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .servletPath(CREATE_PAYLOAD_URL)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .headers(new HttpHeaders())
                        .content(objectMapper.writeValueAsString(request))
        )
                .andDo(print())
                .andReturn()
                .getResolvedException();

        assertTrue(exception instanceof BadRequestClientErrorException);
        BadRequestClientErrorException badRequestClientErrorException = (BadRequestClientErrorException) exception;
        assertEquals(ResponseStatus.INVALID_CREDENTIAL, badRequestClientErrorException.getErrorStatus());
        verify(identityServiceMock, times(1)).getByUsername(USERNAME);
        verify(userServiceMock, times(1)).getById(USER_ID);
    }
}
