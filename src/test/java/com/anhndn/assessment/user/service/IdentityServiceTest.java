package com.anhndn.assessment.user.service;

import com.anhndn.assessment.user.repository.IdentityRepository;
import com.anhndn.assessment.user.repository.entity.IdentityEntity;
import com.anhndn.assessment.user.service.impl.IdentityServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IdentityServiceTest {

    private static final String USERNAME = "username";

    @InjectMocks
    private IdentityServiceImpl identityService;

    @Mock
    private IdentityRepository identityRepositoryMock;

    @Test
    public void test__getByUsername__foundOneRecord__shouldReturnEntity() {
        IdentityEntity identityEntityMock = mock(IdentityEntity.class);
        List<IdentityEntity> identityEntityListMock = Arrays.asList(identityEntityMock);
        when(identityRepositoryMock.findByUserNameAndIsDeleted(anyString(), anyBoolean())).thenReturn(identityEntityListMock);

        IdentityEntity identityEntity = identityService.getByUsername(USERNAME);
        assertEquals(identityEntityMock, identityEntity);
        verify(identityRepositoryMock, times(1)).findByUserNameAndIsDeleted(USERNAME, false);
    }

    @Test
    public void test__getByUsername__foundZeroRecord__shouldReturnNull() {
        List<IdentityEntity> identityEntityListMock = Arrays.asList();
        when(identityRepositoryMock.findByUserNameAndIsDeleted(anyString(), anyBoolean())).thenReturn(identityEntityListMock);

        IdentityEntity identityEntity = identityService.getByUsername(USERNAME);
        assertNull(identityEntity);
        verify(identityRepositoryMock, times(1)).findByUserNameAndIsDeleted(USERNAME, false);
    }
}