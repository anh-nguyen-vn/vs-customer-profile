package com.anhndn.assessment.user.controller;

import com.anhndn.assessment.user.domain.GeneralResponse;
import com.anhndn.assessment.user.domain.ResponseStatus;
import com.anhndn.assessment.user.exception.BadRequestClientErrorException;
import com.anhndn.assessment.user.exception.ClientErrorException;
import com.anhndn.assessment.user.exception.InternalServerErrorException;
import com.anhndn.assessment.user.exception.ServerErrorException;
import com.anhndn.assessment.user.factory.ResponseFactory;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExceptionHandlerControllerTest {

    private static final String FIELD_NAME = "field_name";
    private static final String CODE = "code";
    private static final String MESSAGE = "message";

    @InjectMocks
    private ExceptionHandlerController exceptionHandlerController;

    @Mock
    private ResponseFactory responseFactoryMock;

    @Before
    public void setUp() {
        when(responseFactoryMock.error(any(HttpStatus.class), any(ResponseStatus.class))).thenCallRealMethod();
        when(responseFactoryMock.error(any(HttpStatus.class), any(ResponseStatus.class), anyString())).thenCallRealMethod();
    }

    @Test
    public void test__handleHttpMessageNotReadableException__shouldReturnError() {
        HttpMessageNotReadableException httpMessageNotReadableException = mock(HttpMessageNotReadableException.class);
        JsonMappingException jsonMappingException = mock(JsonMappingException.class);
        JsonMappingException.Reference reference = mock(JsonMappingException.Reference.class);
        when(reference.getFieldName()).thenReturn(FIELD_NAME);
        when(jsonMappingException.getPath()).thenReturn(Arrays.asList(reference));
        when(httpMessageNotReadableException.getCause()).thenReturn(jsonMappingException);

        ResponseEntity<GeneralResponse> responseEntity = exceptionHandlerController.handleHttpMessageNotReadableException(httpMessageNotReadableException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(ResponseStatus.INVALID_DATA_TYPE.getCode(), responseEntity.getBody().getStatus().getCode());
        assertEquals(String.format(ResponseStatus.INVALID_DATA_TYPE.getMessage(), FIELD_NAME), responseEntity.getBody().getStatus().getMessage());

    }

    @Test
    public void test__handleBadRequestClientErrorException__shouldReturnError() {
        ClientErrorException clientErrorException  = mock(BadRequestClientErrorException.class);
        ResponseStatus responseStatus = mock(ResponseStatus.class);
        when(responseStatus.getCode()).thenReturn(CODE);
        when(responseStatus.getMessage()).thenReturn(MESSAGE);
        when(clientErrorException.getErrorStatus()).thenReturn(responseStatus);

        ResponseEntity<GeneralResponse> responseEntity = exceptionHandlerController.handleClientErrorException(clientErrorException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(CODE, responseEntity.getBody().getStatus().getCode());
        assertEquals(MESSAGE, responseEntity.getBody().getStatus().getMessage());
    }

    @Test
    public void test__handleClientErrorException__shouldReturnError() {
        ClientErrorException clientErrorException  = mock(ClientErrorException.class);

        ResponseEntity<GeneralResponse> responseEntity = exceptionHandlerController.handleClientErrorException(clientErrorException);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(ResponseStatus.GENERAL_ERROR, responseEntity.getBody().getStatus());
    }

    @Test
    public void test__handleInternalServerErrorException__shouldReturnError() {
        ServerErrorException serverErrorException  = mock(InternalServerErrorException.class);
        ResponseStatus responseStatus = mock(ResponseStatus.class);
        when(responseStatus.getCode()).thenReturn(CODE);
        when(responseStatus.getMessage()).thenReturn(MESSAGE);
        when(serverErrorException.getErrorStatus()).thenReturn(responseStatus);

        ResponseEntity<GeneralResponse> responseEntity = exceptionHandlerController.handleServerErrorException(serverErrorException);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(CODE, responseEntity.getBody().getStatus().getCode());
        assertEquals(MESSAGE, responseEntity.getBody().getStatus().getMessage());
    }

    @Test
    public void test__handleServerErrorException__shouldReturnError() {
        ServerErrorException serverErrorException  = mock(ServerErrorException.class);

        ResponseEntity<GeneralResponse> responseEntity = exceptionHandlerController.handleServerErrorException(serverErrorException);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(ResponseStatus.GENERAL_ERROR, responseEntity.getBody().getStatus());
    }

    @Test
    public void test__handleException__shouldReturnError() {
        Exception exception  = mock(Exception.class);

        ResponseEntity<GeneralResponse> responseEntity = exceptionHandlerController.handleException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(ResponseStatus.GENERAL_ERROR, responseEntity.getBody().getStatus());
    }
}
