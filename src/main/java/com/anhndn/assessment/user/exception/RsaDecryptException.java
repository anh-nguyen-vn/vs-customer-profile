package com.anhndn.assessment.user.exception;

public class RsaDecryptException extends RuntimeException {

    public RsaDecryptException(Throwable cause) {
        super(cause.getMessage(), cause);
    }

}
