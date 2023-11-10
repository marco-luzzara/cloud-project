package it.unimi.cloudproject.lambda.shop.configurations;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import it.unimi.cloudproject.ShopApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MonitorConfiguration {
    // the opentelemetry object exists because already initialized by the javaagent
    @Bean
    public OpenTelemetry openTelemetry() {
        return GlobalOpenTelemetry.get();
    }

    @Bean
    public Meter meter(OpenTelemetry openTelemetry) {
        return openTelemetry.meterBuilder(ShopApi.class.getName())
                .setInstrumentationVersion("1.0.0")
                .build();
    }
}
