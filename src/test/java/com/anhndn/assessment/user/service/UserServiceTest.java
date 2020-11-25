package com.anhndn.assessment.user.service;

import com.anhndn.assessment.user.repository.ProfileRepository;
import com.anhndn.assessment.user.repository.IdentityRepository;
import com.anhndn.assessment.user.repository.entity.IdentityEntity;
import com.anhndn.assessment.user.repository.entity.ProfileEntity;
import com.anhndn.assessment.user.service.impl.UserServiceImpl;
import com.anhndn.assessment.user.service.model.IdentityModel;
import com.anhndn.assessment.user.service.model.ProfileModel;
import com.anhndn.assessment.user.util.EncryptionUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    private static final Long PROFILE_ID = 1L;
    private static final String EMAIL = "email";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private ProfileRepository profileRepositoryMock;

    @Mock
    private IdentityRepository identityRepositoryMock;

    @Test
    public void test__getById__foundOneRecord__shouldReturnEntity() {
        ProfileEntity profileEntityMock = mock(ProfileEntity.class);
        when(profileRepositoryMock.findById(anyLong())).thenReturn(Optional.ofNullable(profileEntityMock));

        ProfileEntity profileEntity = userService.getById(PROFILE_ID);

        assertEquals(profileEntityMock, profileEntity);
        verify(profileRepositoryMock, times(1)).findById(PROFILE_ID);
    }

    @Test
    public void test__getById__foundZeroRecord__shouldReturnNull() {
        when(profileRepositoryMock.findById(anyLong())).thenReturn(Optional.ofNullable(null));

        ProfileEntity profileEntity = userService.getById(PROFILE_ID);

        assertNull(profileEntity);
        verify(profileRepositoryMock, times(1)).findById(PROFILE_ID);
    }

    @Test
    public void test__getByEmail__foundOneRecord__shouldReturnEntity() {
        ProfileEntity profileEntityMock = mock(ProfileEntity.class);
        when(profileRepositoryMock.findByEmailAndIsDeleted(anyString(), anyBoolean())).thenReturn(Arrays.asList(profileEntityMock));

        ProfileEntity profileEntity = userService.getByEmail(EMAIL);

        assertEquals(profileEntityMock, profileEntity);
        verify(profileRepositoryMock, times(1)).findByEmailAndIsDeleted(EMAIL, false);
    }

    @Test
    public void test__getByEmail__foundZeroRecord__shouldReturnNull() {
        when(profileRepositoryMock.findByEmailAndIsDeleted(anyString(), anyBoolean())).thenReturn(new ArrayList<>());

        ProfileEntity profileEntity = userService.getByEmail(EMAIL);

        assertNull(profileEntity);
        verify(profileRepositoryMock, times(1)).findByEmailAndIsDeleted(EMAIL, false);
    }

    @Test
    public void test__createUser__shouldReturnCreatedEntity() {
        ProfileModel profileModelMock = mock(ProfileModel.class);
        when(profileModelMock.getEmail()).thenReturn(EMAIL);
        IdentityModel identityModelMock = mock(IdentityModel.class);
        when(identityModelMock.getUsername()).thenReturn(USERNAME);
        when(identityModelMock.getPassword()).thenReturn(PASSWORD);
        when(profileRepositoryMock.save(any(ProfileEntity.class))).then(AdditionalAnswers.returnsFirstArg());
        when(identityRepositoryMock.save(any(IdentityEntity.class))).then(AdditionalAnswers.returnsFirstArg());

        ProfileEntity profileEntity = userService.createUser(profileModelMock, identityModelMock);

        ArgumentCaptor<ProfileEntity> profileEntityArgumentCaptor = ArgumentCaptor.forClass(ProfileEntity.class);
        verify(profileRepositoryMock, times(1)).save(profileEntityArgumentCaptor.capture());
        assertEquals(profileEntityArgumentCaptor.getValue(), profileEntity);
        assertEquals(EMAIL, profileEntityArgumentCaptor.getValue().getEmail());
        ArgumentCaptor<IdentityEntity> identityEntityArgumentCaptor = ArgumentCaptor.forClass(IdentityEntity.class);
        verify(identityRepositoryMock, times(1)).save(identityEntityArgumentCaptor.capture());
        assertEquals(USERNAME, identityEntityArgumentCaptor.getValue().getUserName());
        assertEquals(EncryptionUtil.sha256(PASSWORD), identityEntityArgumentCaptor.getValue().getPassword());
    }

}