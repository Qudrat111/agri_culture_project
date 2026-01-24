package com.agri.saga.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class SagaMetrics {

    private final Counter sagasStarted;
    private final Counter sagasFailed;
    private final Counter sagasCompleted;

    public SagaMetrics(MeterRegistry registry) {
        this.sagasStarted = registry.counter("agri_sagas_started_total");
        this.sagasFailed = registry.counter("agri_sagas_failed_total");
        this.sagasCompleted = registry.counter("agri_sagas_completed_total");
    }

    public void incStarted() { sagasStarted.increment(); }
    public void incFailed() { sagasFailed.increment(); }
    public void incCompleted() { sagasCompleted.increment(); }
}
