package com.example.tracing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.tracing.model.Connection;
import com.example.tracing.model.Microservice;
import com.example.tracing.util.FileReaderUtil;
import com.example.tracing.util.GraphBuilder;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Scanner;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.junit.jupiter.api.Test;

class GraphBuilderTest {

    @Test
    public void testBuildGraph_inputHasSelfLoop_throwsIllegalArgumentException() {
        Scanner s = new Scanner("AA5");
        assertThatThrownBy(() -> GraphBuilder.buildGraphFromInput(s))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(GraphBuilder.ERROR_SELF_LOOPS_NOT_ALLOWED);
    }

    @Test
    public void testBuildGraph_inputHasZeroWeight_throwsIllegalArgumentException() {
        Scanner s = new Scanner("AB0");
        assertThatThrownBy(() -> GraphBuilder.buildGraphFromInput(s))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(Connection.ERROR_LATENCY_MUST_BE_POSITIVE);
    }

    @Test
    public void testBuildGraph_inputDoesNotMatchTheFormat_throwsIllegalArgumentException() {
        Scanner s = new Scanner("ABC1");
        assertThatThrownBy(() -> GraphBuilder.buildGraphFromInput(s))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(GraphBuilder.ERROR_TRACE_DOES_NOT_MATCH_FORMAT);
    }

    @Test
    public void testBuildGraph_inputHasDuplicateEdges_throwsIllegalArgumentException() {
        Scanner s = new Scanner("AB1,AB5");
        s.useDelimiter(FileReaderUtil.INPUT_DELIMITER);
        assertThatThrownBy(() -> GraphBuilder.buildGraphFromInput(s))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageStartingWith("Graph already contains edge")
            .hasMessageEndingWith("Duplicate edges are not allowed");
    }

    @Test
    public void testBuildGraph_graphWithThreeVerticesAndTwoEdges_ok() {
        Scanner s = new Scanner("AB1,BC5");
        s.useDelimiter(FileReaderUtil.INPUT_DELIMITER);
        SimpleImmutableEntry<SimpleDirectedWeightedGraph<Microservice, Connection>, Integer> actualGraph = GraphBuilder
            .buildGraphFromInput(s);
        assertThat(actualGraph.getKey().vertexSet()).extracting(Microservice::getName)
            .containsExactlyElementsOf(List.of("A", "B", "C"));
        assertThat(actualGraph.getKey().edgeSet()).hasSize(2);
        assertThat(actualGraph.getKey().getEdge(new Microservice("A"), new Microservice("B")))
            .isNotNull()
            .extracting(Connection::getLatencyInMs)
            .isEqualTo(1);
        assertThat(actualGraph.getKey().getEdge(new Microservice("B"), new Microservice("C")))
            .isNotNull()
            .extracting(Connection::getLatencyInMs)
            .isEqualTo(5);
        assertThat(actualGraph.getValue()).isEqualTo(1);
    }

    @Test
    public void testBuildGraph_graphWithTwoVerticesTwoEdgesAndCycle_ok() {
        Scanner s = new Scanner("AB1,BA5");
        s.useDelimiter(FileReaderUtil.INPUT_DELIMITER);
        SimpleImmutableEntry<SimpleDirectedWeightedGraph<Microservice, Connection>, Integer> actualGraph = GraphBuilder
            .buildGraphFromInput(s);
        assertThat(actualGraph.getKey().vertexSet()).extracting(Microservice::getName)
            .containsExactlyElementsOf(List.of("A", "B"));
        assertThat(actualGraph.getKey().edgeSet()).hasSize(2);
        assertThat(actualGraph.getKey().getEdge(new Microservice("A"), new Microservice("B")))
            .isNotNull()
            .extracting(Connection::getLatencyInMs)
            .isEqualTo(1);
        assertThat(actualGraph.getKey().getEdge(new Microservice("B"), new Microservice("A")))
            .isNotNull()
            .extracting(Connection::getLatencyInMs)
            .isEqualTo(5);
        assertThat(actualGraph.getValue()).isEqualTo(1);
    }

}