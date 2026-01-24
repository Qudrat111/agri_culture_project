package com.agri.inventory.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Custom business metrics exposed to Prometheus via Spring Boot Actuator.
 */
@Component
public class InventoryMetrics {

    private final Counter reservationsSucceeded;
    private final Counter reservationsFailed;
    private final Counter compensationsExecuted;

    public InventoryMetrics(MeterRegistry registry) {
        this.reservationsSucceeded = registry.counter("agri_inventory_reservations_succeeded_total");
        this.reservationsFailed = registry.counter("agri_inventory_reservations_failed_total");
        this.compensationsExecuted = registry.counter("agri_inventory_compensations_total");
    }

    public void incSuccess() {
        reservationsSucceeded.increment();
    }

    public void incFail() {
        reservationsFailed.increment();
    }

    public void incCompensation() {
        compensationsExecuted.increment();
    }
}
