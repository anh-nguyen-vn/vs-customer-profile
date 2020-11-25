package com.anhndn.assessment.user.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
public class LoggerAspect {

    @Around("within(com.anhndn.assessment.user.*) && @annotation(LogAround)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("===== Start: {} =====", this.getMessage(joinPoint));
        try {
            Object returning = joinPoint.proceed();
            log.info("===== End with success: {} =====", this.getMessage(joinPoint));
            return returning;
        } catch (Throwable e) {
            log.error("There is an error occur in process", e);
            log.info("===== End with failure: {} =====", this.getMessage(joinPoint));
            throw e;
        }
    }

    private String getMessage(JoinPoint joinPoint) throws NoSuchMethodException {
        Method interfaceMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Method implementationMethod = joinPoint.getTarget().getClass().getMethod(interfaceMethod.getName(), interfaceMethod.getParameterTypes());

        String message = null;
        if (implementationMethod.isAnnotationPresent(LogAround.class)) {
            LogAround logAround = implementationMethod.getAnnotation(LogAround.class);
            message = logAround.message();
        }

        return message;
    }

}
