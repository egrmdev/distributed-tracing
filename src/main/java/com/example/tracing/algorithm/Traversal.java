package com.example.tracing.algorithm;

import com.example.tracing.model.Connection;
import com.example.tracing.model.Microservice;
import com.google.inject.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.alg.shortestpath.EppsteinKShortestPath;
import org.jgrapht.graph.GraphWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Traversal {

    private static final Logger LOG = LoggerFactory.getLogger(Traversal.class);
    public static final String ERROR_VERTICES_CANNOT_BE_EMPTY = "Vertices cannot be empty";

    public static Optional<GraphWalk<Microservice, Connection>> getGraphWalk(
        @NotNull Graph<Microservice, Connection> g,
        @NotNull List<Microservice> vertices)
    {
        Preconditions.checkNotNull(g);
        Preconditions.checkNotNull(vertices);
        Preconditions.checkArgument(!vertices.isEmpty(), ERROR_VERTICES_CANNOT_BE_EMPTY);

        if (vertices.size() == 1) {
            return Optional.of(GraphWalk.singletonWalk(g, vertices.get(0)));
        }
        List<Connection> edgeList = new ArrayList<>();
        int pathWeight = 0;
        for (int j = 0; j < vertices.size() - 1; j++) {
            Connection edge = g.getEdge(vertices.get(j), vertices.get(j + 1));
            if (edge != null) {
                double edgeWeight = g.getEdgeWeight(edge);
                pathWeight += edgeWeight;
                LOG.debug("{}", edge);
                edgeList.add(edge);
            } else {
                String trace = vertices.stream().map(Microservice::toString).collect(Collectors.joining(" -> "));
                LOG.debug("Path does not exist: {}", trace);
                return Optional.empty();
            }
        }
        return Optional.of(new GraphWalk<>(g, vertices.get(0), vertices.get(vertices.size() - 1), edgeList, pathWeight));
    }

    public static List<GraphPath<Microservice, Connection>> getAllPathsWithPathLengthUpToMax(
        @NotNull Graph<Microservice, Connection> g,
        @NotNull Microservice sourceVertex,
        @NotNull Microservice targetVertex,
        int maxPathLength)
    {
        Preconditions.checkNotNull(g);
        Preconditions.checkNotNull(sourceVertex);
        Preconditions.checkNotNull(targetVertex);

        AllDirectedPaths<Microservice, Connection> allPaths = new AllDirectedPaths<>(g);
        return allPaths.getAllPaths(sourceVertex, targetVertex, false, maxPathLength).stream()
            // filter out self-loop like path that will appear if source and target vertices are the same
            .filter(path -> path.getLength() > 0)
            .collect(Collectors.toList());
    }

    public static List<GraphPath<Microservice, Connection>> getAllPathsWithExactPathLength(
        @NotNull Graph<Microservice, Connection> g,
        @NotNull Microservice sourceVertex,
        @NotNull Microservice targetVertex,
        int pathLength)
    {
        return getAllPathsWithPathLengthUpToMax(g, sourceVertex, targetVertex, pathLength).stream()
            .filter(path -> path.getLength() == pathLength)
            .collect(Collectors.toList());
    }

    public static List<GraphPath<Microservice, Connection>> getAllPathsWithWeightLessThanSlow(
        @NotNull Graph<Microservice, Connection> g,
        @NotNull Microservice sourceVertex,
        @NotNull Microservice targetVertex,
        int maxPathWeight,
        int minEdgeWeight
    )
    {
        // this is suboptimal because for larger maxPathWeight / minEdgeWeight (>40) for graphs with loops it will be very slow and blow up memory
        // maxHops is limited to maxPathWeight / minEdgeWeight to reduce amount of returned paths
        // because weights (latency) are positive integers and therefore number of hops can't be greater than maxPathWeight / minEdgeWeight
        return getAllPathsWithPathLengthUpToMax(g, sourceVertex, targetVertex, maxPathWeight / minEdgeWeight).stream()
            .filter(path -> path.getWeight() < maxPathWeight)
            .peek(path -> {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("{}", path.getEdgeList().stream().map(Connection::toString).collect(Collectors.joining(",")));
                }
            })
            .collect(Collectors.toList());
    }

    public static List<GraphPath<Microservice, Connection>> getAllPathsWithWeightLessThan(
        @NotNull Graph<Microservice, Connection> g,
        @NotNull Microservice sourceVertex,
        @NotNull Microservice targetVertex,
        int maxPathWeight
    )
    {
        return new AllSimpleAndNonSimpleDirectedPaths<>(g)
            .getAllPathsWithWeightLessThan(sourceVertex, targetVertex, maxPathWeight);
    }

    public static Optional<GraphPath<Microservice, Connection>> getShortestPath(
        @NotNull Graph<Microservice, Connection> g,
        @NotNull Microservice sourceVertex,
        @NotNull Microservice targetVertex)
    {
        Preconditions.checkNotNull(g);
        Preconditions.checkNotNull(sourceVertex);
        Preconditions.checkNotNull(targetVertex);

        EppsteinKShortestPath<Microservice, Connection> shortestPath = new EppsteinKShortestPath<>(g);
        List<GraphPath<Microservice, Connection>> paths = sourceVertex.equals(targetVertex)
            // it's necessary because if source and target vertices are the same, the 0 length path will be returned
            ? shortestPath.getPaths(sourceVertex, targetVertex, 2)
            : shortestPath.getPaths(sourceVertex, targetVertex, 1);
        if (paths.size() == 2) {
            return paths.get(0).getLength() == 0 ? Optional.of(paths.get(1)) : Optional.of(paths.get(0));
        } else if (paths.size() == 1) {
            return Optional.of(paths.get(0));
        } else {
            return Optional.empty();
        }
    }


}
