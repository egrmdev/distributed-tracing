package com.example.tracing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import com.example.tracing.algorithm.Traversal;
import com.example.tracing.model.Connection;
import com.example.tracing.model.Microservice;
import java.util.List;
import java.util.Optional;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.GraphWalk;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TraversalTest {

    @Nested
    @DisplayName("Graph walking tests")
    class GraphWalkTest {

        @Test
        public void testGraphWalk_simpleGraphAllNodesAreConnected_correctWalk() {
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

            Optional<GraphWalk<Microservice, Connection>> actualWalk = Traversal.getGraphWalk(g, List.of(vA, vB, vC));
            assertThat(actualWalk).isNotEmpty();
            assertThat(actualWalk.get().getVertexList()).containsExactly(vA, vB, vC);
            assertThat(actualWalk.get().getEdgeList()).containsExactly(vAvB, vBvC);
            assertThat(actualWalk.get().getWeight()).isEqualTo(4);
            assertThat(actualWalk.get().getLength()).isEqualTo(2);
        }

        @Test
        public void testGraphWalk_simpleGraphNoConnection_empty() {
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
            Connection vBvD = new Connection(3);
            g.addEdge(vB, vD, vBvD);
            g.setEdgeWeight(vB, vD, 3);
            Connection vAvC = new Connection(5);
            g.addEdge(vA, vC, vAvC);
            g.setEdgeWeight(vA, vC, 5);

            Optional<GraphWalk<Microservice, Connection>> actualWalk = Traversal.getGraphWalk(g, List.of(vA, vC, vD));
            assertThat(actualWalk).isEmpty();
        }

        @Test
        public void testGraphWalk_graphWithLoopAllNodesConnected_correctWalk() {
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
            Connection vCvA = new Connection(5);
            g.addEdge(vC, vA, vCvA);
            g.setEdgeWeight(vC, vA, 5);

            Optional<GraphWalk<Microservice, Connection>> actualWalk = Traversal.getGraphWalk(g, List.of(vA, vB, vC, vA));
            assertThat(actualWalk).isNotEmpty();
            assertThat(actualWalk.get().getVertexList()).containsExactly(vA, vB, vC, vA);
            assertThat(actualWalk.get().getEdgeList()).containsExactly(vAvB, vBvC, vCvA);
            assertThat(actualWalk.get().getWeight()).isEqualTo(9);
            assertThat(actualWalk.get().getLength()).isEqualTo(3);
        }

        @Test
        public void testGraphWalk_simpleGraphSingleVertixWalk_correctWalk() {
            SimpleDirectedWeightedGraph<Microservice, Connection> g = new SimpleDirectedWeightedGraph<>(Connection.class);
            Microservice vA = new Microservice("A");
            Microservice vB = new Microservice("B");
            g.addVertex(vA);
            g.addVertex(vB);
            Connection vAvB = new Connection(1);
            g.addEdge(vA, vB, vAvB);
            g.setEdgeWeight(vA, vB, 1);

            Optional<GraphWalk<Microservice, Connection>> actualWalk = Traversal.getGraphWalk(g, List.of(vA));
            assertThat(actualWalk).isNotEmpty();
            assertThat(actualWalk.get().getVertexList()).containsExactly(vA);
            assertThat(actualWalk.get().getEdgeList()).isEmpty();
            assertThat(actualWalk.get().getWeight()).isEqualTo(0);
            assertThat(actualWalk.get().getLength()).isEqualTo(0);
        }

        @Test
        public void testGraphWalk_simpleGraphEmptyVerticesWalk_throwsIllegalArgumentException() {
            SimpleDirectedWeightedGraph<Microservice, Connection> g = new SimpleDirectedWeightedGraph<>(Connection.class);
            Microservice vA = new Microservice("A");
            Microservice vB = new Microservice("B");
            g.addVertex(vA);
            g.addVertex(vB);
            Connection vAvB = new Connection(1);
            g.addEdge(vA, vB, vAvB);
            g.setEdgeWeight(vA, vB, 1);

            assertThatThrownBy(() -> Traversal.getGraphWalk(g, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(Traversal.ERROR_VERTICES_CANNOT_BE_EMPTY);
        }
    }

    @Nested
    @DisplayName("Shortest path tests")
    class ShortestPathTest {

        @Test
        public void testShortestPath_simpleGraphOnePath_shortestPathIsFound() {
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

            Optional<GraphPath<Microservice, Connection>> shortestPath = Traversal.getShortestPath(g, vA, vC);
            assertThat(shortestPath).isNotEmpty();
            assertThat(shortestPath.get().getStartVertex()).isEqualTo(vA);
            assertThat(shortestPath.get().getEndVertex()).isEqualTo(vC);
            assertThat(shortestPath.get().getVertexList()).containsExactly(vA, vB, vC);
            assertThat(shortestPath.get().getEdgeList()).containsExactly(vAvB, vBvC);
            assertThat(shortestPath.get().getLength()).isEqualTo(2);
            assertThat(shortestPath.get().getWeight()).isEqualTo(4);
        }

        @Test
        public void testShortestPath_simpleGraphTwoPathsWithDifferentWeightAndLength_shortestPathIsFound() {
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

            Optional<GraphPath<Microservice, Connection>> shortestPath = Traversal.getShortestPath(g, vA, vC);
            assertThat(shortestPath).isNotEmpty();
            assertThat(shortestPath.get().getStartVertex()).isEqualTo(vA);
            assertThat(shortestPath.get().getEndVertex()).isEqualTo(vC);
            assertThat(shortestPath.get().getVertexList()).containsExactly(vA, vB, vC);
            assertThat(shortestPath.get().getEdgeList()).containsExactly(vAvB, vBvC);
            assertThat(shortestPath.get().getLength()).isEqualTo(2);
            assertThat(shortestPath.get().getWeight()).isEqualTo(4);
        }

        @Test
        public void testShortestPath_simpleGraphTwoPathsWithTheSameWeightDifferentLength_pathWithSmallerLengthReturned() {
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
            Connection vAvC = new Connection(4);
            g.addEdge(vA, vC, vAvC);
            g.setEdgeWeight(vA, vC, 4);

            Optional<GraphPath<Microservice, Connection>> shortestPath = Traversal.getShortestPath(g, vA, vC);
            assertThat(shortestPath).isNotEmpty();
            assertThat(shortestPath.get().getStartVertex()).isEqualTo(vA);
            assertThat(shortestPath.get().getEndVertex()).isEqualTo(vC);
            assertThat(shortestPath.get().getVertexList()).containsExactly(vA, vC);
            assertThat(shortestPath.get().getEdgeList()).containsExactly(vAvC);
            assertThat(shortestPath.get().getLength()).isEqualTo(1);
            assertThat(shortestPath.get().getWeight()).isEqualTo(4);
        }

        @Test
        public void testShortestPath_graphWithLoopStartAndEndVerticesAreTheSame_shortestPathIsFound() {
            SimpleDirectedWeightedGraph<Microservice, Connection> g = new SimpleDirectedWeightedGraph<>(Connection.class);
            Microservice vA = new Microservice("A");
            Microservice vB = new Microservice("B");
            g.addVertex(vA);
            g.addVertex(vB);
            Connection vAvB = new Connection(1);
            g.addEdge(vA, vB, vAvB);
            g.setEdgeWeight(vA, vB, 1);

            Connection vBvA = new Connection(2);
            g.addEdge(vB, vA, vBvA);
            g.setEdgeWeight(vB, vA, 2);

            Optional<GraphPath<Microservice, Connection>> shortestPath = Traversal.getShortestPath(g, vA, vA);
            assertThat(shortestPath).isNotEmpty();
            assertThat(shortestPath.get().getStartVertex()).isEqualTo(vA);
            assertThat(shortestPath.get().getEndVertex()).isEqualTo(vA);
            assertThat(shortestPath.get().getVertexList()).containsExactly(vA, vB, vA);
            assertThat(shortestPath.get().getEdgeList()).containsExactly(vAvB, vBvA);
            assertThat(shortestPath.get().getLength()).isEqualTo(2);
            assertThat(shortestPath.get().getWeight()).isEqualTo(3);
        }

        @Test
        public void testShortestPath_graphWithTwoLoopStartAndEndVerticesAreTheSame_shortestPathIsFound() {
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
            Connection vBvC = new Connection(2);
            g.addEdge(vB, vC, vBvC);
            g.setEdgeWeight(vB, vC, 2);
            Connection vBvA = new Connection(6);
            g.addEdge(vB, vA, vBvA);
            g.setEdgeWeight(vB, vA, 6);
            Connection vCvA = new Connection(3);
            g.addEdge(vC, vA, vCvA);
            g.setEdgeWeight(vC, vA, 3);

            Optional<GraphPath<Microservice, Connection>> shortestPath = Traversal.getShortestPath(g, vA, vA);
            assertThat(shortestPath).isNotEmpty();
            assertThat(shortestPath.get().getStartVertex()).isEqualTo(vA);
            assertThat(shortestPath.get().getEndVertex()).isEqualTo(vA);
            assertThat(shortestPath.get().getVertexList()).containsExactly(vA, vB, vC, vA);
            assertThat(shortestPath.get().getEdgeList()).containsExactly(vAvB, vBvC, vCvA);
            assertThat(shortestPath.get().getLength()).isEqualTo(3);
            assertThat(shortestPath.get().getWeight()).isEqualTo(6);
        }

        @Test
        public void testShortestPath_graphWithNoPath_emptyOptional() {
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

            Optional<GraphPath<Microservice, Connection>> shortestPath = Traversal.getShortestPath(g, vA, vC);
            assertThat(shortestPath).isEmpty();
        }
    }

    @Nested
    @DisplayName("All paths tests")
    class AllPathsTest {

        @Test
        public void testGetAllPathWithMaxLength_simpleGraph_twoPathsFound() {
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

            List<GraphPath<Microservice, Connection>> allPaths = Traversal.getAllPathsWithPathLengthUpToMax(g, vA, vC, 2);
            assertThat(allPaths.size()).isEqualTo(2);
            assertThat(allPaths)
                .extracting(GraphPath::getStartVertex, GraphPath::getEndVertex, GraphPath::getVertexList, GraphPath::getLength,
                    GraphPath::getWeight)
                .containsOnly(
                    tuple(vA, vC, List.of(vA, vC), 1, 5.0),
                    tuple(vA, vC, List.of(vA, vB, vC), 2, 4.0)
                );
        }

        @Test
        public void testGetAllPathWithMaxLength_simpleGraphNoPathBetweenVertices_emptyList() {
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

            List<GraphPath<Microservice, Connection>> allPaths = Traversal.getAllPathsWithPathLengthUpToMax(g, vA, vC, 3);
            assertThat(allPaths).isEmpty();
        }

        @Test
        public void testGetAllPathWithMaxLength_graphWithLoop_fourPathsFound() {
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
            g.setEdgeWeight(vA, vC, 3);


            List<GraphPath<Microservice, Connection>> allPaths = Traversal.getAllPathsWithPathLengthUpToMax(g, vA, vC, 4);
            assertThat(allPaths.size()).isEqualTo(4);
            assertThat(allPaths)
                .extracting(GraphPath::getStartVertex, GraphPath::getEndVertex, GraphPath::getVertexList, GraphPath::getLength, GraphPath::getWeight)
                .containsOnly(
                    tuple(vA, vC, List.of(vA, vC), 1, 3.0),
                    tuple(vA, vC, List.of(vA, vB, vC), 2, 3.0),
                    tuple(vA, vC, List.of(vA, vB, vC, vD, vC), 4, 8.0),
                    tuple(vA, vC, List.of(vA, vC, vD, vC), 3, 8.0)
                );
        }

        @Test
        public void testGetAllPathWithExactLength_graphWithLoop_onePathsFound() {
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
            g.setEdgeWeight(vA, vC, 3);


            List<GraphPath<Microservice, Connection>> allPaths = Traversal.getAllPathsWithExactPathLength(g, vA, vC, 4);
            assertThat(allPaths.size()).isEqualTo(1);
            assertThat(allPaths)
                .extracting(GraphPath::getStartVertex, GraphPath::getEndVertex, GraphPath::getVertexList, GraphPath::getLength, GraphPath::getWeight)
                .containsOnly(
                    tuple(vA, vC, List.of(vA, vB, vC, vD, vC), 4, 8.0)
                );
        }

        @Test
        public void testGetAllPathWithMaxWeight_graphWithLoop_fivePathsFound() {
            SimpleDirectedWeightedGraph<Microservice, Connection> g = new SimpleDirectedWeightedGraph<>(Connection.class);
            Microservice vA = new Microservice("A");
            Microservice vB = new Microservice("B");
            Microservice vC = new Microservice("C");
            Microservice vD = new Microservice("D");
            Microservice vE = new Microservice("E");
            g.addVertex(vA);
            g.addVertex(vB);
            g.addVertex(vC);
            g.addVertex(vD);
            g.addVertex(vE);
            Connection vAvB = new Connection(1);
            g.addEdge(vA, vB, vAvB);
            g.setEdgeWeight(vA, vB, 1);
            Connection vBvC = new Connection(2);
            g.addEdge(vB, vC, vBvC);
            g.setEdgeWeight(vB, vC, 2);
            Connection vCvD = new Connection(3);
            g.addEdge(vC, vD, vCvD);
            g.setEdgeWeight(vC, vD, 3);
            Connection vDvE = new Connection(1);
            g.addEdge(vD, vE, vDvE);
            g.setEdgeWeight(vD, vE, 1);
            Connection vEvD = new Connection(1);
            g.addEdge(vE, vD, vEvD);
            g.setEdgeWeight(vE, vD, 1);
            Connection vDvC = new Connection(2);
            g.addEdge(vD, vC, vDvC);
            g.setEdgeWeight(vD, vC, 2);
            Connection vAvC = new Connection(2);
            g.addEdge(vA, vC, vAvC);
            g.setEdgeWeight(vA, vC, 2);


            List<GraphPath<Microservice, Connection>> allPathsSlow = Traversal.getAllPathsWithWeightLessThanSlow(g, vA, vC, 10, 1);
            assertThat(allPathsSlow.size()).isEqualTo(5);
            assertThat(allPathsSlow)
                .extracting(GraphPath::getStartVertex, GraphPath::getEndVertex, GraphPath::getVertexList, GraphPath::getLength, GraphPath::getWeight)
                .containsOnly(
                    tuple(vA, vC, List.of(vA, vC), 1, 2.0),
                    tuple(vA, vC, List.of(vA, vB, vC), 2, 3.0),
                    tuple(vA, vC, List.of(vA, vB, vC, vD, vC), 4, 8.0),
                    tuple(vA, vC, List.of(vA, vC, vD, vC), 3, 7.0),
                    tuple(vA, vC, List.of(vA, vC, vD, vE, vD, vC), 5, 9.0)
                );

            List<GraphPath<Microservice, Connection>> allPaths = Traversal.getAllPathsWithWeightLessThan(g, vA, vC, 10);
            assertThat(allPaths).hasSameElementsAs(allPathsSlow);
        }
    }
}