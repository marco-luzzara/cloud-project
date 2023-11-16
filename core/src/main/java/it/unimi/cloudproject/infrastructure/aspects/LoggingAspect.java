package it.unimi.cloudproject.infrastructure.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

@Aspect
public class LoggingAspect {
    @Pointcut("execution(public * it.unimi.cloudproject.services.services.*.*(..))")
    public void logPointcutForServices() {}

    @Around("logPointcutForServices()")
    public Object logMethodInfo(ProceedingJoinPoint joinPoint) throws Throwable {
        var serviceClass = joinPoint.getTarget().getClass();
        var methodName = joinPoint.getSignature().getName();
        var args = Arrays.toString(joinPoint.getArgs());

        var logger = LoggerFactory.getLogger(serviceClass);
        var serviceCall = "%s.%s(%s)".formatted(serviceClass.getName(), methodName, args);

        var result = joinPoint.proceed();
        logger.info("%s -> %s".formatted(serviceCall, result));

        return result;
    }
}
