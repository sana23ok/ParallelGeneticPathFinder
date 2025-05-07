package org.example.parallel.paralellOperations;

import org.example.graph.GraphPathChecker;
import org.example.sequential.ShortestPathGAIslandSequential;

import java.io.IOException;
import java.util.List;

import static org.example.Constants.NUM_NODES;
import static org.example.graph.GraphUtils.generateGraphInput;
import static org.example.graph.GraphUtils.loadGraph;

public class Main {
    public static void main(String[] args) {
        String filename = "graph.txt";
        try {
            // Generate graph input file
            generateGraphInput(filename);

            // Load graph into memory
            int[][] graph = new int[NUM_NODES][NUM_NODES];
            loadGraph(filename, graph);
            for (int i = 0; i < 4; i++) {
                System.out.println("Nodes: " + NUM_NODES);

                // Sequential
                long startSeqIsland = System.nanoTime();
                List<Integer> seqPath = ShortestPathGAIslandSequential.run(graph);
                long endSeqIsland = System.nanoTime();
                long durationSeqIsland = endSeqIsland - startSeqIsland;

                new GraphPathChecker().check(filename, seqPath);
                System.out.printf("Time for Sequential: %.3f ms%n", durationSeqIsland / 1_000_000.0);

                // Parallel run1
                long start1 = System.nanoTime();
                List<Integer> path1 = ShortestPathGAIslandParallel.run1(graph);
                long end1 = System.nanoTime();
                long duration1 = end1 - start1;

                new GraphPathChecker().check(filename, path1);
                System.out.printf("Time for run1: %.3f ms%n", duration1 / 1_000_000.0);
                System.out.printf("Speedup run1: %.2f%n", (double) durationSeqIsland / duration1);

                // Parallel run2
                long start2 = System.nanoTime();
                List<Integer> path2 = ShortestPathGAIslandParallel.run2(graph);
                long end2 = System.nanoTime();
                long duration2 = end2 - start2;

                new GraphPathChecker().check(filename, path2);
                System.out.printf("Time for run2: %.3f ms%n", duration2 / 1_000_000.0);
                System.out.printf("Speedup run2: %.2f%n", (double) durationSeqIsland / duration2);

                System.out.println("- - - - - - - - - - - - - - - - - - - - ");
            }


        } catch (IOException e) {
            System.err.println("Error occurred while handling the graph file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
        }
    }
}

