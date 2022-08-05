package com.example.tracing.algorithm;

import com.google.inject.internal.util.Preconditions;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.GraphTests;
import org.jgrapht.graph.GraphWalk;

/**
 * An algorithm that uses BFS and labeling to find all paths between two sets of nodes in a weighted directed graph
 * with non-simple paths and with a path weight limit
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class AllSimpleAndNonSimpleDirectedPaths<V, E> {

    private final Graph<V, E> graph;

    /**
     * Creates a new instance.
     *
     * @param graph the input graph that must be a directed, weighted graph with no self-loops.
     */
    public AllSimpleAndNonSimpleDirectedPaths(@NotNull Graph<V, E> graph) {
        Preconditions.checkArgument(!GraphTests.hasSelfLoops(graph), "Graph may not have self-loops");
        GraphTests.requireDirected(graph);
        GraphTests.requireWeighted(graph);
        this.graph = graph;
    }

    /**
     * Calculates and returns all the paths including non-simple ones from the source vertex to the target vertex.
     *
     * @param sourceVertex the source vertex
     * @param targetVertex the target vertex
     * @param weightLimit weight limit of the path, only the paths that have weight less than {@code weightLimit} will be returned
     * @return all paths from the source vertex to the target vertex
     */
    public List<GraphPath<V, E>> getAllPathsWithWeightLessThan(
        @NotNull V sourceVertex,
        @NotNull V targetVertex,
        double weightLimit)
    {
        Preconditions.checkNotNull(sourceVertex);
        Preconditions.checkNotNull(targetVertex);

        Queue<Label<V>> openLabels = new ArrayDeque<>(); // queue containing the labels that will be expanded
        List<Label<V>> targetVertexLabels = new ArrayList<>(); // list of the labels encoding the path from the target to the source vertex
        openLabels.add(new Label<>(null, sourceVertex));

        while (!openLabels.isEmpty()) {
            Label<V> expandedLabel = openLabels.poll();
            // expand each label by finding all the outgoing edges for the vertex associated with the label
            for (E outgoingEdge : graph.outgoingEdgesOf(expandedLabel.getAssociatedNode())) {
                V neighbourVertex = graph.getEdgeTarget(outgoingEdge);
                double partialPathWeight = expandedLabel.getWeight() + graph.getEdgeWeight(outgoingEdge);
                if (partialPathWeight >= weightLimit) {
                    continue;
                } else if (neighbourVertex.equals(targetVertex)) {
                    targetVertexLabels.add(new Label<>(expandedLabel, neighbourVertex, partialPathWeight));
                }
                openLabels.add(new Label<>(expandedLabel, neighbourVertex, partialPathWeight));
            }
        }

        return buildPathsFromLabels(targetVertexLabels);
    }

    /**
     * Creates a path from source vertex to the target vertex. The path is built by unfolding each of the labels until label
     * with {@code null} as preceding label is reached.
     *
     * @param targetVertexLabels list of labels, each of which encodes a path from target vertex to the source vertex
     * @return a list of paths from source vertex to the target vertex
     */
    private List<GraphPath<V, E>> buildPathsFromLabels(List<Label<V>> targetVertexLabels) {
        List<GraphPath<V, E>> allPaths = new ArrayList<>();
        for (Label<V> l : targetVertexLabels) {
            double pathWeight = l.getWeight();
            V targetVertex = l.getAssociatedNode();
            List<E> edges = new ArrayList<>();
            Label<V> precedingLabel = l.getPrecedingLabel();
            do {
                edges.add(graph.getEdge(precedingLabel.getAssociatedNode(), l.getAssociatedNode()));
                l = precedingLabel;
                precedingLabel = precedingLabel.getPrecedingLabel();
            } while (precedingLabel != null);
            // reverse because edges were collected in the order from the target vertex to the source vertex
            Collections.reverse(edges);
            allPaths.add(new GraphWalk<>(graph, l.getAssociatedNode(), targetVertex, edges, pathWeight));
        }
        return allPaths;
    }

    public Graph<V, E> getGraph() {
        return graph;
    }

    /**
     * Encodes partial path from the source node to the associated node
     * @param <V> the graph vertex type
     */
    private static class Label<V> {

        private final Label<V> precedingLabel;
        private final V associatedNode;
        private final double weight;

        public Label(Label<V> precedingLabel, @NotNull V associatedNode) {
            this(precedingLabel, associatedNode, 0.);
        }

        public Label(Label<V> precedingLabel, @NotNull V associatedNode, double weight) {
            this.precedingLabel = precedingLabel;
            this.associatedNode = Preconditions.checkNotNull(associatedNode);
            Preconditions.checkArgument(weight >= 0);
            this.weight = weight;
        }

        public Label<V> getPrecedingLabel() {
            return precedingLabel;
        }

        public V getAssociatedNode() {
            return associatedNode;
        }

        public double getWeight() {
            return weight;
        }

        @Override
        public String toString() {
            return "{" +
                "precedingLabel=" + precedingLabel +
                ", associatedNode=" + associatedNode +
                ", weight=" + weight +
                '}';
        }
    }

}
