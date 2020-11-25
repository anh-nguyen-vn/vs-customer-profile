package com.anhndn.assessment.user.exception;

import com.anhndn.assessment.user.domain.ResponseStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InternalServerErrorException extends ServerErrorException {

    public InternalServerErrorException(ResponseStatus errorStatus) {
        super(errorStatus);
    }
}
