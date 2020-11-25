package com.anhndn.assessment.user.exception;

import com.anhndn.assessment.user.domain.ResponseStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientErrorException extends RuntimeException {

    private ResponseStatus errorStatus;

    public ClientErrorException(ResponseStatus errorStatus) {
        super(errorStatus.getMessage());
        this.setErrorStatus(errorStatus);
    }

}
