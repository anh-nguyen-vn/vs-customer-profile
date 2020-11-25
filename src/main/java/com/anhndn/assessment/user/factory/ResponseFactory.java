package com.anhndn.assessment.user.factory;

import com.anhndn.assessment.user.domain.GeneralResponse;
import com.anhndn.assessment.user.domain.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ResponseFactory {

    public ResponseEntity success() {
        GeneralResponse<Object> responseObject = new GeneralResponse();
        responseObject.setStatus(ResponseStatus.SUCCESS);
        return ResponseEntity.ok(responseObject);
    }

    public ResponseEntity success(Object data, Class clazz) {
        GeneralResponse<Object> responseObject = new GeneralResponse();
        responseObject.setStatus(ResponseStatus.SUCCESS);
        responseObject.setData(clazz.cast(data));
        return ResponseEntity.ok(responseObject);
    }

    public ResponseEntity error(HttpStatus httpStatus, ResponseStatus responseStatus, String ... params) {
        GeneralResponse<Object> responseObject = new GeneralResponse();
        if (params.length > 0) {
            String code = responseStatus.getCode();
            String message = String.format(responseStatus.getMessage(), params);
            responseObject.setStatus(new ResponseStatus(code, message));
        } else {
            responseObject.setStatus(responseStatus);
        }

        return new ResponseEntity(responseObject, httpStatus);
    }

    public ResponseEntity error(HttpStatus httpStatus, ResponseStatus responseStatus, Object data, Class clazz) {
        GeneralResponse<Object> responseObject = new GeneralResponse();
        responseObject.setStatus(responseStatus);
        responseObject.setData(clazz.cast(data));
        ResponseEntity responseEntity = new ResponseEntity(responseObject, httpStatus);
        return responseEntity;
    }
}
