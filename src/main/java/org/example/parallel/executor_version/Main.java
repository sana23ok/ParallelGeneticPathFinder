package org.example.parallel.executor_version;

import org.example.sequential.ShortestPathGAIslandSequential;
import java.io.IOException;
import static org.example.Constants.NUM_NODES;
import static org.example.graph.GraphUtils.*;

public class Main {
    public static void main(String[] args) {
        String filename = "graph.txt";
        try {
            // Generate graph input file
            generateGraphInput(filename);

            // Load graph into memory
            int[][] graph = new int[NUM_NODES][NUM_NODES];
            loadGraph(filename, graph);

            // Measure execution time for ShortestPathGA (non-parallel)
            long startTimeGA = System.nanoTime();
            ShortestPathGAIslandSequential.run(graph);
            long endTimeGA = System.nanoTime();
            long durationGA = endTimeGA - startTimeGA;
            System.out.println("Time for Sequential: " + durationGA + " ns");

            // Measure execution time for ShortestPathGAParallel (parallel)
            long startIsland = System.nanoTime();
            ShortestPathGAIslandParallel.run(graph, 12);
            long endIsland = System.nanoTime();
            long durationIsland = endIsland- startIsland;

            // Calculate speedup
            if (durationGA != 0) {
                double speedup = (double) durationGA / durationIsland;
                System.out.println("Speedup Island: " + speedup);
            } else {
                System.out.println("Time for Sequential ShortestPathGA is too small to calculate speedup.");
            }

        } catch (IOException e) {
            System.err.println("Error occurred while handling the graph file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
        }
    }
}
