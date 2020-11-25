package com.anhndn.assessment.user.exception;

import com.anhndn.assessment.user.domain.ResponseStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerErrorException extends RuntimeException {

    private ResponseStatus errorStatus;

    public ServerErrorException(ResponseStatus errorStatus) {
        super(errorStatus.getMessage());
        this.setErrorStatus(errorStatus);
    }
}
