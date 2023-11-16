package it.unimi.cloudproject.infrastructure.monitoring;

import io.opentelemetry.api.metrics.Meter;

/**
 * This interface is helpful when paired with the {@code @WithMeasureExecutionTime} annotation. In this case,
 * the aspect that computes the execution time, which is {@code ExecutionTimeAspect} detects that a
 * meter is available in the same class containing the method to weave and it is used to send the
 * generated metrics.
 */
public interface MetricsGenerator {
    Meter getMeter();
}
