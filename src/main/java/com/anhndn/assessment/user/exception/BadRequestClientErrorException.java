package com.anhndn.assessment.user.exception;

import com.anhndn.assessment.user.domain.ResponseStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BadRequestClientErrorException extends ClientErrorException {
    public BadRequestClientErrorException(ResponseStatus errorStatus) {
        super(errorStatus);
    }
}
