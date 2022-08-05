Command-line application that models microservices and connections between them as a weighted directed graph, where a vertex is a microservice, an edge is an existing link, and edge weight is a latency between two services connected by the edge. The application then computes some metrics like an average latency between two services, a number of traces originating from one service and ending in the other service, a length of the shortest trace between two services, and a number of different traces origination in one service and ending in the other with a certain latency limit.

### Build and run

To build the app:

`mvn clean package` 

To run the app:

`java -jar ./target/distributed-tracing-1.0-SNAPSHOT.jar <INPUT_FILE>`, where INPUT_FILE contains the graph. Test input file is provided in `src/resource/input.txt`. 

### Input

A directed graph where a node represents a microservice and an edge represents a connection between two microservices. The weight of the edge represents the average latency between those two services. A given connection will never appear more than once and for a given connection the starting and ending service will not be the same service.

### Test input

For the test input, the microservices are named using the first few letters of the alphabet from A to E. A trace between 2 microservices (A to B) with a latency of 5 is represented as AB5.

Graph: AB5, BC4, CD8, DC8, DE6, AD5, CE2, EB3, AE7

### Output

For test input 1 through 5, if no such trace exists, output ‘NO SUCH TRACE’. For example the first problem asks for the total average latency of a trace originating in service A, service A makes a call to service B followed immediately by service B making a call to service C.

1. The average latency of the trace A-B-C. 
2. The average latency of the trace A-D. 
3. The average latency of the trace A-D-C.
4. The average latency of the trace A-E-B-C-D.
5. The average latency of the trace A-E-D.
6. The number of traces originating in service C and ending in service C with a maximum of 3 hops. In the sample data below there are two such traces: C-D-C (2 stops) and C-E-B-C (3 stops).
7. The number of traces originating in A and ending in C with exactly 4 hops. In the sample data below there are three such traces: A to C (via B, C, D); A to C (via D, C, D); and A to C (via D, E, B). 
8. The length of the shortest trace (in terms of latency) between A and C. 
9. The length of the shortest trace (in terms of latency) between B and B. 
10. The number of different traces from C to C with an average latency of less than 30. In the same data, the traces are C-D-C, C-E-B-C, C-E-B-C-D-C, C-D-C-E-B-C, C-D-E-B-C, C-E-B-C-E-B-C, C-E-B-C-E-B-C-E-B-C.

### Expected output

1. 9
2. 5
3. 13
4. 22
5. NO SUCH TRACE
6. 2
7. 3
8. 9
9. 9
10. 7
