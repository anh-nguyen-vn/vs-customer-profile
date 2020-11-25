package com.anhndn.assessment.user.interceptor;

import com.anhndn.assessment.user.constant.HttpHeaderConstant;
import com.anhndn.assessment.user.constant.LoggerConstant;
import com.anhndn.assessment.user.constant.PayloadConstant;
import com.anhndn.assessment.user.domain.Payload;
import com.anhndn.assessment.user.domain.UserType;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class PayloadInterceptor extends HandlerInterceptorAdapter {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.info("Request URI [{}], Remote Host [{}], Remote Port [{}]", request.getRequestURI(), request.getRemoteHost(), request.getRemotePort());

        Payload payload = new Payload();
        Long userId = request.getHeader(PayloadConstant.USER_ID_KEY) == null ? null
                : Long.parseLong(request.getHeader(PayloadConstant.USER_ID_KEY));
        Integer userTypeId = request.getHeader(PayloadConstant.USER_TYPE_ID_KEY) == null ? null
                : Integer.parseInt(request.getHeader(PayloadConstant.USER_TYPE_ID_KEY));
        payload.setUserId(userId);
        payload.setUserType(UserType.fromId(userTypeId));
        payload.setUsername(request.getHeader(PayloadConstant.USERNAME_KEY));
        request.setAttribute(PayloadConstant.PAYLOAD_KEY, payload);

        String correlationId = request.getHeader(HttpHeaderConstant.CORRELATION_ID);
        String xIpAddress = request.getHeader(HttpHeaderConstant.X_IP_ADDRESS);
        MDC.put(LoggerConstant.CORRELATION_ID_LOG_KEY_NAME, correlationId);
        MDC.put(LoggerConstant.IP_ADDRESS_LOG_KEY_NAME, xIpAddress);

        log.info("Request from user id [{}], user type ID [{}], correlationId [{}], xIpAddress [{}]", userId, userTypeId, correlationId, xIpAddress);
        return true;
    }
}
