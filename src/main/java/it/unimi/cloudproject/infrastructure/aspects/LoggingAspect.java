package it.unimi.cloudproject.infrastructure.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.logging.Logger;

@Component
@Aspect
public class LoggingAspect {
    @Pointcut("within(it.unimi.cloudproject.application.services.*)")
    public void logPointcutForServices() {}

    @Before("logPointcutForServices()")
    public void logMethodCallsWithinAdvice(JoinPoint joinPoint) {
        var serviceClass = joinPoint.getTarget().getClass();
        var methodName = joinPoint.getSignature().getName();
        var args = Arrays.toString(joinPoint.getArgs());

        var logger = LoggerFactory.getLogger(serviceClass);
        logger.info("Calling %s.%s(%s)".formatted(serviceClass.getName(), methodName, args));
    }
}
