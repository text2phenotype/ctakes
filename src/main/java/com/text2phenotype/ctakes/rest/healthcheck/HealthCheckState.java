package com.text2phenotype.ctakes.rest.healthcheck;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.Map;

/**
 * State of health check - singleton
 */
public class HealthCheckState {

    private HealthCheckStatus status = HealthCheckStatus.UNAVAILABLE;
    private Map<String, String> pipelines = new HashMap<>();
    private String error;

    private HealthCheckState() {}

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public synchronized Map<String, String> getPipelines() {
        return pipelines;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public synchronized String getError() {
        return error;
    }

    public synchronized void setError(String error) {
        this.error = error;
        this.pipelines.clear();
    }

    @JsonIgnore
    public synchronized HealthCheckStatus getStatus() {
        return status;
    }

    public synchronized void setStatus(HealthCheckStatus status) {
        this.status = status;
    }
}
