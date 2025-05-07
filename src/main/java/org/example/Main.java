package org.example;

import org.example.old_versions._ShortestPathGAParallel;
import org.example.old_versions.ShortestPathGAParallel1;
import org.example.old_versions.islandParallel.ShortestPathGAIslandParallel;

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
            _ShortestPathGAParallel.run(graph);
            long endTimeGA = System.nanoTime();
            long durationGA = endTimeGA - startTimeGA;

            System.out.println("Time for Sequential: " + durationGA + " ns");

//            // Measure execution time for ShortestPathGAParallel (parallel)
//            long startTimeGAParallel = System.nanoTime();
//            ShortestPathGAParallel.run(graph);
//            long endTimeGAParallel = System.nanoTime();
//            long durationGAParallel = endTimeGAParallel - startTimeGAParallel;
//
//            // Output results
//            System.out.println("Time for Parallel: " + durationGAParallel + " ns");
//
//            // Calculate speedup
//            if (durationGA != 0) {
//                double speedup = (double) durationGA / durationGAParallel;
//                System.out.println("Speedup: " + speedup);
//            } else {
//                System.out.println("Time for Sequential ShortestPathGA is too small to calculate speedup.");
//            }
//            System.out.println("-------------------------------");
//
            // Measure execution time for ShortestPathGAParallel (parallel)
            long startTimeGAParallel1 = System.nanoTime();
            ShortestPathGAParallel1.run(graph);
            long endTimeGAParallel1 = System.nanoTime();
            long durationGAParallel1 = endTimeGAParallel1 - startTimeGAParallel1;

            // Calculate speedup
            if (durationGA != 0) {
                double speedup = (double) durationGA / durationGAParallel1;
                System.out.println("Speedup 1: " + speedup);
            } else {
                System.out.println("Time for Sequential ShortestPathGA is too small to calculate speedup.");
            }
            System.out.println("-------------------------------");

            // Measure execution time for ShortestPathGAParallel (parallel)
            long startIsland = System.nanoTime();
            ShortestPathGAIslandParallel.run(graph);
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

    private static void warmup() {
        long sum = 0;
        for (int i = 0; i < 10; i++) {
            for (long j = 0; j < 10000000000L; j++) {
                sum += j - (j / 2) * 2;
            }
            sum -= sum;
        }
    }
}
