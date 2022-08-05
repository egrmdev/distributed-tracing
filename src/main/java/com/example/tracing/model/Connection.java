package com.example.tracing.model;

import com.google.inject.internal.util.Preconditions;
import org.jgrapht.graph.DefaultWeightedEdge;

public class Connection extends DefaultWeightedEdge {

    public static final String ERROR_LATENCY_MUST_BE_POSITIVE = "Latency must be a positive integer";
    private final int latencyInMs;

    public Connection(int latencyInMs) {
        Preconditions.checkArgument(latencyInMs > 0, ERROR_LATENCY_MUST_BE_POSITIVE);
        this.latencyInMs = latencyInMs;
    }

    public int getLatencyInMs() {
        return latencyInMs;
    }

    @Override
    protected double getWeight() {
        return latencyInMs;
    }

    @Override
    public String toString() {
        return String.format("{%s}-(%d)->{%s}", getSource(), latencyInMs, getTarget());
    }
}
