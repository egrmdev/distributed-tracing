package com.example.tracing.algorithm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.example.tracing.model.Connection;
import com.example.tracing.model.Microservice;
import java.util.List;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.junit.jupiter.api.Test;

class AllSimpleAndNonSimpleDirectedPathsTest {

    @Test
    public void testGetAllPathsWithWeightLessThan_simpleGraph_onePathsFound() {
        SimpleDirectedWeightedGraph<Microservice, Connection> g = new SimpleDirectedWeightedGraph<>(Connection.class);
        Microservice vA = new Microservice("A");
        Microservice vB = new Microservice("B");
        Microservice vC = new Microservice("C");
        g.addVertex(vA);
        g.addVertex(vB);
        g.addVertex(vC);
        Connection vAvB = new Connection(1);
        g.addEdge(vA, vB, vAvB);
        g.setEdgeWeight(vA, vB, 1);
        Connection vBvC = new Connection(3);
        g.addEdge(vB, vC, vBvC);
        g.setEdgeWeight(vB, vC, 3);
        Connection vAvC = new Connection(5);
        g.addEdge(vA, vC, vAvC);
        g.setEdgeWeight(vA, vC, 5);

        AllSimpleAndNonSimpleDirectedPaths<Microservice, Connection> allPathsFinder = new AllSimpleAndNonSimpleDirectedPaths<>(g);
        List<GraphPath<Microservice, Connection>> allPaths = allPathsFinder.getAllPathsWithWeightLessThan(vA, vC, 5.);
        assertThat(allPaths.size()).isEqualTo(1);
        assertThat(allPaths)
            .extracting(GraphPath::getStartVertex, GraphPath::getEndVertex, GraphPath::getVertexList, GraphPath::getLength,
                GraphPath::getWeight)
            .containsOnly(
                tuple(vA, vC, List.of(vA, vB, vC), 2, 4.0)
            );
    }

    @Test
    public void testGetAllPathsWithWeightLessThan_simpleGraphStartAndEndVertexIsTheSame_onePathFound() {
        SimpleDirectedWeightedGraph<Microservice, Connection> g = new SimpleDirectedWeightedGraph<>(Connection.class);
        Microservice vA = new Microservice("A");
        Microservice vB = new Microservice("B");
        g.addVertex(vA);
        g.addVertex(vB);
        Connection vAvB = new Connection(1);
        g.addEdge(vA, vB, vAvB);
        g.setEdgeWeight(vA, vB, 1);
        Connection vBvA = new Connection(3);
        g.addEdge(vB, vA, vBvA);
        g.setEdgeWeight(vB, vA, 3);

        AllSimpleAndNonSimpleDirectedPaths<Microservice, Connection> allPathsFinder = new AllSimpleAndNonSimpleDirectedPaths<>(g);
        List<GraphPath<Microservice, Connection>> allPaths = allPathsFinder.getAllPathsWithWeightLessThan(vA, vA, 4.1);
        assertThat(allPaths.size()).isEqualTo(1);
        assertThat(allPaths)
            .extracting(GraphPath::getStartVertex, GraphPath::getEndVertex, GraphPath::getVertexList, GraphPath::getLength,
                GraphPath::getWeight)
            .containsOnly(
                tuple(vA, vA, List.of(vA, vB, vA), 2, 4.0)
            );
    }

    @Test
    public void testGetAllPathsWithWeightLessThan_simpleGraphNoPathBetweenVertices_noPaths() {
        SimpleDirectedWeightedGraph<Microservice, Connection> g = new SimpleDirectedWeightedGraph<>(Connection.class);
        Microservice vA = new Microservice("A");
        Microservice vB = new Microservice("B");
        Microservice vC = new Microservice("C");
        g.addVertex(vA);
        g.addVertex(vB);
        g.addVertex(vC);
        Connection vAvB = new Connection(1);
        g.addEdge(vA, vB, vAvB);
        g.setEdgeWeight(vA, vB, 1);

        AllSimpleAndNonSimpleDirectedPaths<Microservice, Connection> allPathsFinder = new AllSimpleAndNonSimpleDirectedPaths<>(g);
        List<GraphPath<Microservice, Connection>> allPaths = allPathsFinder.getAllPathsWithWeightLessThan(vA, vC, 10.);
        assertThat(allPaths).isEmpty();
    }

    @Test
    public void testGetAllPathsWithWeightLessThan_graphWithLoop_threePathsFound() {
        SimpleDirectedWeightedGraph<Microservice, Connection> g = new SimpleDirectedWeightedGraph<>(Connection.class);
        Microservice vA = new Microservice("A");
        Microservice vB = new Microservice("B");
        Microservice vC = new Microservice("C");
        Microservice vD = new Microservice("D");
        g.addVertex(vA);
        g.addVertex(vB);
        g.addVertex(vC);
        g.addVertex(vD);
        Connection vAvB = new Connection(1);
        g.addEdge(vA, vB, vAvB);
        g.setEdgeWeight(vA, vB, 1);
        Connection vBvC = new Connection(2);
        g.addEdge(vB, vC, vBvC);
        g.setEdgeWeight(vB, vC, 2);
        Connection vCvD = new Connection(3);
        g.addEdge(vC, vD, vCvD);
        g.setEdgeWeight(vC, vD, 3);
        Connection vDvC = new Connection(2);
        g.addEdge(vD, vC, vDvC);
        g.setEdgeWeight(vD, vC, 2);
        Connection vAvC = new Connection(3);
        g.addEdge(vA, vC, vAvC);
        g.setEdgeWeight(vA, vC, 2);

        AllSimpleAndNonSimpleDirectedPaths<Microservice, Connection> allPathsFinder = new AllSimpleAndNonSimpleDirectedPaths<>(g);
        List<GraphPath<Microservice, Connection>> allPaths = allPathsFinder.getAllPathsWithWeightLessThan(vA, vC, 8.);
        assertThat(allPaths.size()).isEqualTo(3);
        assertThat(allPaths)
            .extracting(GraphPath::getStartVertex, GraphPath::getEndVertex, GraphPath::getVertexList, GraphPath::getLength,
                GraphPath::getWeight)
            .containsOnly(
                tuple(vA, vC, List.of(vA, vC), 1, 2.0),
                tuple(vA, vC, List.of(vA, vB, vC), 2, 3.0),
                tuple(vA, vC, List.of(vA, vC, vD, vC), 3, 7.0)
            );
    }

}