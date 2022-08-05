package com.example.tracing;

import com.example.tracing.algorithm.Traversal;
import com.example.tracing.model.Connection;
import com.example.tracing.model.Microservice;
import com.example.tracing.util.FileReaderUtil;
import com.example.tracing.util.GraphBuilder;
import java.io.FileNotFoundException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.GraphWalk;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TracingApp {

    private static final Logger LOG = LoggerFactory.getLogger(TracingApp.class);
    private static final Consumer<GraphWalk<Microservice, Connection>> TRACE_PATH_LATENCY = walk -> LOG
        .info("{}", Double.valueOf(walk.getWeight()).intValue());
    private static final Runnable TRACE_NOT_EXISTS = () -> LOG.info("NO SUCH TRACE");
    private static final Consumer<GraphPath<Microservice, Connection>> TRACE_WEIGHT = path -> LOG
        .info("{}", Double.valueOf(path.getWeight()).intValue());

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Application takes exactly one argument - file with input graph.");
        }
        Scanner s = FileReaderUtil.getInitializedScanner(args[0]);
        AbstractMap.SimpleImmutableEntry<SimpleDirectedWeightedGraph<Microservice, Connection>, Integer> graphAndMinimalWeight =
            GraphBuilder.buildGraphFromInput(s);
        SimpleDirectedWeightedGraph<Microservice, Connection> g = graphAndMinimalWeight.getKey();
        // was needed for the com.example.tracing.algorithm.Traversal.getAllPathsWithWeightLessThanSlow
        int minimalEdgeWeight = graphAndMinimalWeight.getValue();

        Traversal.getGraphWalk(g, List.of(new Microservice("A"), new Microservice("B"), new Microservice("C")))
            .ifPresentOrElse(TRACE_PATH_LATENCY, TRACE_NOT_EXISTS);
        Traversal.getGraphWalk(g, List.of(new Microservice("A"), new Microservice("D")))
            .ifPresentOrElse(TRACE_PATH_LATENCY, TRACE_NOT_EXISTS);
        Traversal.getGraphWalk(g, List.of(new Microservice("A"), new Microservice("D"), new Microservice("C")))
            .ifPresentOrElse(TRACE_PATH_LATENCY, TRACE_NOT_EXISTS);
        Traversal.getGraphWalk(g,
            List.of(new Microservice("A"), new Microservice("E"), new Microservice("B"), new Microservice("C"),
                new Microservice("D")))
            .ifPresentOrElse(TRACE_PATH_LATENCY, TRACE_NOT_EXISTS);
        Traversal.getGraphWalk(g, List.of(new Microservice("A"), new Microservice("E"), new Microservice("D")))
            .ifPresentOrElse(TRACE_PATH_LATENCY, TRACE_NOT_EXISTS);

        LOG.info("{}", Traversal.getAllPathsWithPathLengthUpToMax(g, new Microservice("C"), new Microservice("C"), 3).size());
        LOG.info("{}", Traversal.getAllPathsWithExactPathLength(g, new Microservice("A"), new Microservice("C"), 4).size());

        Traversal.getShortestPath(g, new Microservice("A"), new Microservice("C"))
            .ifPresentOrElse(TRACE_WEIGHT, TRACE_NOT_EXISTS);
        Traversal.getShortestPath(g, new Microservice("B"), new Microservice("B"))
            .ifPresentOrElse(TRACE_WEIGHT, TRACE_NOT_EXISTS);

        LOG.info("{}", Traversal.getAllPathsWithWeightLessThan(g, new Microservice("C"), new Microservice("C"), 30).size());
    }

}
