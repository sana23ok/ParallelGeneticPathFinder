package org.example;

import org.example.graph.GraphPathChecker;
import org.example.sequential.ShortestPathGAIslandSequential;

import java.io.IOException;
import java.util.List;

import static org.example.Constants.NUM_NODES;
import static org.example.graph.GraphUtils.generateGraphInput;
import static org.example.graph.GraphUtils.loadGraph;

public class PerformanceTest {
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
                System.out.println("- - - - - - - - - - - - - - - - - - - - ");

                // Measure execution time for ShortestPathGAIslandParallel
                long startIsland = System.nanoTime();
                List<Integer> paralelPath = org.example.parallel.fork_version.ShortestPathGAIslandParallel.run(graph, 12);
                long endIsland = System.nanoTime();
                long durationIsland = endIsland - startIsland;

                GraphPathChecker checkerPar = new GraphPathChecker();
                checkerPar.check(filename, paralelPath);

                System.out.printf("Time for Parallel ForkJoinPool Version: %.3f s%n", durationIsland / 1_000_000_000.0);

                // Calculate speedup
                if (durationIsland != 0) {
                    double speedup = (double) durationSeqIsland / durationIsland;
                    System.out.printf("Speedup: %.2f%n", speedup);
                } else {
                    System.out.println("Time for Parallel is too small to calculate speedup.");
                }

                System.out.println("- - - - - - - - - - - - - - - - - - - - ");
                // Measure execution time for ShortestPathGAIslandParallel
                long startIslandRecursive = System.nanoTime();
                List<Integer> paralelPathRecursive = org.example.executor_version.ShortestPathGAIslandParallel.run(graph);
                long endIslandRecursive = System.nanoTime();
                long durationIslandRecursive = endIslandRecursive - startIslandRecursive;

                GraphPathChecker checkerParRecursive = new GraphPathChecker();
                checkerParRecursive.check(filename, paralelPathRecursive);

                System.out.printf("Time for Parallel ExecutorService Version: %.3f s%n", durationIslandRecursive / 1_000_000_000.0);

                // Calculate speedup
                if (durationIslandRecursive != 0) {
                    double speedup = (double) durationSeqIsland / durationIslandRecursive;
                    System.out.printf("Speedup: %.2f%n", speedup);
                } else {
                    System.out.println("Time for Parallel is too small to calculate speedup.");
                }

                System.out.println("-----------------------------------------");
                System.out.println("-----------------------------------------");
            }

        } catch (IOException e) {
            System.err.println("Error occurred while handling the graph file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
        }
    }
}


