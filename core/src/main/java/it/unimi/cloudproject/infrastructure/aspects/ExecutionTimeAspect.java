package it.unimi.cloudproject.infrastructure.aspects;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Aspect
@Component
public class ExecutionTimeAspect {
    @Autowired(required=false)
    private Meter meter;

    // Concurrency here is not necessary for Lambda
    private final ConcurrentHashMap<String, LongHistogram> methodToMetricMap = new ConcurrentHashMap<>();

    @Around("@annotation(it.unimi.cloudproject.infrastructure.annotations.WithMeasuredExecutionTime)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        var methodName = joinPoint.getSignature().getName();

        var startTime = System.currentTimeMillis();
        var result = joinPoint.proceed();
        var endTime = System.currentTimeMillis();

        var executionTime = endTime - startTime;

        if (Objects.isNull(meter)) {
            System.out.println("executionTime = " + executionTime);
            return result;
        }

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
        histogramMeter.record(executionTime, attrs);

        return result;
    }
}