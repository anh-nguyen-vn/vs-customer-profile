package com.anhndn.assessment.user.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.client.ClientHttpResponse;

@Getter
@Setter
@AllArgsConstructor
public class ClientHttpException extends RuntimeException {
    private ClientHttpResponse clientHttpResponse;
}
