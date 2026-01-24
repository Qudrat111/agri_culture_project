package com.agri.order.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * Custom business metrics (requirement: "add custom metrics to your custom app with prometheus client").
 * These show up on /actuator/prometheus.
 */
@Component
public class OrderMetrics {

    private final Counter ordersCreated;
    private final Counter ordersConfirmed;
    private final Counter ordersCancelled;
    private final Timer orderCommandDuration;

    public OrderMetrics(MeterRegistry registry) {
        this.ordersCreated = registry.counter("agri_orders_created_total");
        this.ordersConfirmed = registry.counter("agri_orders_confirmed_total");
        this.ordersCancelled = registry.counter("agri_orders_cancelled_total");
        this.orderCommandDuration = Timer.builder("agri_order_command_duration_seconds")
                .description("Time spent in order command operations")
                .publishPercentileHistogram()
                .register(registry);
    }

    public void incCreated() {
        ordersCreated.increment();
    }

    public void incConfirmed() {
        ordersConfirmed.increment();
    }

    public void incCancelled() {
        ordersCancelled.increment();
    }

    public <T> T recordCommand(java.util.concurrent.Callable<T> op) {
        try {
            return orderCommandDuration.recordCallable(op);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
