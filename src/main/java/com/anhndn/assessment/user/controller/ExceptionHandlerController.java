package com.anhndn.assessment.user.controller;

import com.anhndn.assessment.user.controller.response.FieldValidationErrorResponse;
import com.anhndn.assessment.user.domain.ResponseStatus;
import com.anhndn.assessment.user.exception.BadRequestClientErrorException;
import com.anhndn.assessment.user.exception.ClientErrorException;
import com.anhndn.assessment.user.exception.InternalServerErrorException;
import com.anhndn.assessment.user.exception.ServerErrorException;
import com.anhndn.assessment.user.factory.ResponseFactory;
import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
@RestController
@Slf4j
public class ExceptionHandlerController {

    @Autowired
    private ResponseFactory responseFactory;

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public ResponseEntity handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        JsonMappingException jme = (JsonMappingException) e.getCause();
        String errorField = jme.getPath().get(jme.getPath().size() - 1).getFieldName();
        log.error("Invalid data type for field [{}]", errorField, e);
        return responseFactory.error(HttpStatus.BAD_REQUEST, ResponseStatus.INVALID_DATA_TYPE, errorField);
    }

    @ExceptionHandler(ClientErrorException.class)
    @ResponseBody
    public ResponseEntity handleClientErrorException(ClientErrorException e) {
        log.error("handle client error exception", e);
        if (e instanceof BadRequestClientErrorException) {
            return responseFactory.error(HttpStatus.BAD_REQUEST, e.getErrorStatus());
        }
        return responseFactory.error(HttpStatus.INTERNAL_SERVER_ERROR, ResponseStatus.GENERAL_ERROR);
    }

    @ExceptionHandler(ServerErrorException.class)
    @ResponseBody
    public ResponseEntity handleServerErrorException(ServerErrorException e) {
        log.error("handle server error exception", e);
        if (e instanceof InternalServerErrorException) {
            return responseFactory.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getErrorStatus());
        }
        return responseFactory.error(HttpStatus.INTERNAL_SERVER_ERROR, ResponseStatus.GENERAL_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("handle method argument not valid exception", e);
        List<FieldValidationErrorResponse.FieldError> errors = new ArrayList<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.add(new FieldValidationErrorResponse.FieldError(fieldName, errorMessage));
        });

        return responseFactory.error(HttpStatus.BAD_REQUEST, ResponseStatus.REQUEST_VALIDATION_ERROR,
                new FieldValidationErrorResponse(errors), FieldValidationErrorResponse.class);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity handleException(Exception e) {
        log.error("handle general exception", e);
        return responseFactory.error(HttpStatus.INTERNAL_SERVER_ERROR, ResponseStatus.GENERAL_ERROR);
    }
}