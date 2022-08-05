package com.example.tracing.util;

import com.example.tracing.model.Connection;
import com.example.tracing.model.Microservice;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(GraphBuilder.class);
    private static final String TRACE_INPUT_FORMAT = "([A-Z])([A-Z])(\\d+)";
    private static final Pattern TRACE_INPUT = Pattern.compile(TRACE_INPUT_FORMAT);
    private static final String TRACE_DOES_NOR_MATCH_THE_FORMAT = "Trace doesn't match the format";
    public static final String ERROR_SELF_LOOPS_NOT_ALLOWED = "Self-loops are not allowed";
    public static final String ERROR_TRACE_DOES_NOT_MATCH_FORMAT = TRACE_DOES_NOR_MATCH_THE_FORMAT + " "+ TRACE_INPUT_FORMAT;

    /**
     * Builds simple directed weighted graph from the scanner initialized with the graph input in format ([a-Z])([a-Z])(\d+)
     * where first group is source microservice, the second group is target microservice, and the third one is a connection latency
     * between them
     * @param s Scanner initialized with graph input file
     * @return SimpleImmutableEntry containing a graph build from the scanner and minimal edge weight the graph has
     */
    public static SimpleImmutableEntry<SimpleDirectedWeightedGraph<Microservice, Connection>, Integer> buildGraphFromInput(
        Scanner s
    )
    {
        SimpleDirectedWeightedGraph<Microservice, Connection> g = new SimpleDirectedWeightedGraph<>(Connection.class);
        int minEdgeWeight = Integer.MAX_VALUE;
        while (s.hasNext()) {
            String next = s.next();
            Matcher traceMatcher = TRACE_INPUT.matcher(next);
            if (traceMatcher.matches()) {
                Microservice sourceVertex = new Microservice(traceMatcher.group(1));
                Microservice targetVertex = new Microservice(traceMatcher.group(2));
                if (sourceVertex.equals(targetVertex)) {
                    throw new IllegalArgumentException(ERROR_SELF_LOOPS_NOT_ALLOWED);
                }
                if (!g.containsVertex(sourceVertex)) {
                    g.addVertex(sourceVertex);
                }
                if (!g.containsVertex(targetVertex)) {
                    g.addVertex(targetVertex);
                }
                if (!g.containsEdge(sourceVertex, targetVertex)) {
                    int weight = Integer.parseInt(traceMatcher.group(3));
                    minEdgeWeight = Math.min(minEdgeWeight, weight);
                    Connection c = new Connection(weight);
                    g.addEdge(sourceVertex, targetVertex, c);
                    g.setEdgeWeight(sourceVertex, targetVertex, c.getLatencyInMs());
                } else {
                    LOG.error("Graph already contains edge {}->{}. Duplicate edges are not allowed", sourceVertex,
                        targetVertex);
                    throw new IllegalArgumentException(String
                        .format("Graph already contains edge {%s}->{%s}. Duplicate edges are not allowed", sourceVertex,
                            targetVertex));
                }
            } else {
                LOG.error(TRACE_DOES_NOR_MATCH_THE_FORMAT + ": {}", next);
                throw new IllegalArgumentException(ERROR_TRACE_DOES_NOT_MATCH_FORMAT);
            }
        }
        return new AbstractMap.SimpleImmutableEntry<>(g, minEdgeWeight);
    }
}
