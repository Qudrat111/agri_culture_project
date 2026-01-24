package com.agri.query.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class QueryMetrics {

    private final Counter queryRequests;
    private final Counter queryFailures;

    public QueryMetrics(MeterRegistry registry) {
        this.queryRequests = registry.counter("agri_query_requests_total");
        this.queryFailures = registry.counter("agri_query_failures_total");
    }

    public void incRequest() {
        queryRequests.increment();
    }

    public void incFailure() {
        queryFailures.increment();
    }
}
