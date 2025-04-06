package org.example;

import java.io.IOException;
import static org.example.Constants.NUM_NODES;
import static org.example.GraphUtils.*;

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
            ShortestPathGA.run(graph);
            long endTimeGA = System.nanoTime();
            long durationGA = endTimeGA - startTimeGA;

            // Measure execution time for ShortestPathGAParallel (parallel)
            long startTimeGAParallel = System.nanoTime();
            ShortestPathGAParallel.run(graph);
            long endTimeGAParallel = System.nanoTime();
            long durationGAParallel = endTimeGAParallel - startTimeGAParallel;

            // Output results
            System.out.println("Time for Sequential: " + durationGA + " ns");
            System.out.println("Time for Parallel: " + durationGAParallel + " ns");

            // Calculate speedup
            if (durationGA != 0) {
                double speedup = (double) durationGA / durationGAParallel;
                System.out.println("Speedup: " + speedup);
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
