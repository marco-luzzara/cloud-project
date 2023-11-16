package it.unimi.cloudproject.infrastructure.aspects;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import it.unimi.cloudproject.infrastructure.monitoring.MetricsGenerator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
public class ExecutionTimeAspect {
    // Concurrency here is not necessary for Lambda
    private final ConcurrentHashMap<String, LongHistogram> methodToMetricMap = new ConcurrentHashMap<>();

    @Pointcut("@annotation(it.unimi.cloudproject.infrastructure.annotations.WithMeasuredExecutionTime)")
    public void haveWithMeasuredExecutionTimeAnnotation() {}

    @Pointcut("target(it.unimi.cloudproject.infrastructure.monitoring.MetricsGenerator)")
    public void implementsMetricGenerator() {}

    @Pointcut("haveWithMeasuredExecutionTimeAnnotation() && implementsMetricGenerator()")
    public void methodToWeaveImplementsMetricGenerator() {}

    @Pointcut("haveWithMeasuredExecutionTimeAnnotation() && !implementsMetricGenerator()")
    public void methodToWeaveIsSimple() {}

    @Around("methodToWeaveImplementsMetricGenerator()")
    public Object measureExecutionTimeAndSendToBothLoggerAndMeter(ProceedingJoinPoint joinPoint) throws Throwable {
        var targetCls = joinPoint.getTarget().getClass();
        var methodName = joinPoint.getSignature().getName();
        var logger = LoggerFactory.getLogger(targetCls);

        var executionInfo = measureExecutionTime(joinPoint, logger);

        logExecutionTime(logger, methodName, executionInfo.executionTime());

        var meter = ((MetricsGenerator) joinPoint.getTarget()).getMeter();
        var histogramMeter = this.methodToMetricMap.computeIfAbsent(methodName, mn -> meter
                .histogramBuilder("method.%s.execution.time.".formatted(mn))
                .setDescription("Measure the execution time for the method " + mn)
                .setUnit("ms")
                .ofLongs()
                .build());

        var currentSpan = Span.current();
        var attrs = Attributes.empty();
        if (!Objects.equals(currentSpan, Span.getInvalid())) {
            var spanId = currentSpan.getSpanContext().getSpanId();
            var traceId = currentSpan.getSpanContext().getTraceId();

            attrs = Attributes.of(AttributeKey.stringKey("traceId"), traceId,
                    AttributeKey.stringKey("spanId"), spanId);
        }
        histogramMeter.record(executionInfo.executionTime(), attrs);

        return executionInfo.result();
    }

    @Around("methodToWeaveIsSimple()")
    public Object measureExecutionTimeAndSendToLoggerOnly(ProceedingJoinPoint joinPoint) throws Throwable {
        var targetCls = joinPoint.getTarget().getClass();
        var methodName = joinPoint.getSignature().getName();
        var logger = LoggerFactory.getLogger(targetCls);

        var executionInfo = measureExecutionTime(joinPoint, logger);

        logExecutionTime(logger, methodName, executionInfo.executionTime());

        return executionInfo.result();
    }

    private ExecutionInfo measureExecutionTime(ProceedingJoinPoint joinPoint, Logger logger) throws Throwable {
        var methodName = joinPoint.getSignature().getName();
        logger.debug("Calling the executionTime aspect for " + methodName);

        var startTime = System.currentTimeMillis();
        var result = joinPoint.proceed();
        var endTime = System.currentTimeMillis();

        var executionTime = endTime - startTime;

        return new ExecutionInfo(result, executionTime);
    }

    private void logExecutionTime(Logger logger, String methodName, long executionTime) {
        logger.info("executionTime for {} = {}", methodName, executionTime);
    }

    private record ExecutionInfo(Object result, long executionTime) {}
}