package org.example;

import org.example.graph.GraphPathChecker;
import org.example.old_versions.parallel_old.paralellOperations.ShortestPathGAIslandParallel;
import org.example.sequential.ShortestPathGAIslandSequential;
import java.io.IOException;
import java.util.List;
import static org.example.Constants.NUM_NODES;
import static org.example.graph.GraphUtils.*;

public class Test {
    public static void main(String[] args) {
        String filename = "graph.txt";
        try {
            // Generate graph input file
            generateGraphInput(filename);

            // Load graph into memory
            int[][] graph = new int[NUM_NODES][NUM_NODES];
            loadGraph(filename, graph);
            for (int i = 0; i < 3; i++) {
                System.out.println("Nodes: " + NUM_NODES);

                // Sequential
                long startSeqIsland = System.nanoTime();
                List<Integer> seqPath = ShortestPathGAIslandSequential.run(graph);
                long endSeqIsland = System.nanoTime();
                long durationSeqIsland = endSeqIsland - startSeqIsland;

                new GraphPathChecker().check(filename, seqPath);
                System.out.printf("Time for Sequential: %.3f ms%n", durationSeqIsland / 1_000_000.0);
                System.out.println("- - - - - - - - - - - - - - - - - - - - ");

                // Parallel run0
                long startIslandPr = System.nanoTime();
                List<Integer> paralelPathPr = org.example.old_versions.parallel_old.parallelIslands.ShortestPathGAIslandParallel.run(graph);
                long endIslandPr = System.nanoTime();
                long durationIslandPr = endIslandPr - startIslandPr;

                GraphPathChecker checkerParPr = new GraphPathChecker();
                checkerParPr.check(filename, paralelPathPr);

                System.out.printf("Time for Parallel Island: %.3f s%n", durationIslandPr / 1_000_000_000.0);
                System.out.printf("Time for Parallel Island: %.2f%n", (double) durationSeqIsland / durationIslandPr);
                System.out.println("- - - - - - - - - - - - - - - - - - - - ");

                // Parallel run1
                long start1 = System.nanoTime();
                List<Integer> path1 = ShortestPathGAIslandParallel.run1(graph);
                long end1 = System.nanoTime();
                long duration1 = end1 - start1;

                new GraphPathChecker().check(filename, path1);
                System.out.printf("Time for Parallel Operations: %.3f ms%n", duration1 / 1_000_000.0);
                System.out.printf("Speedup run Parallel Operations: %.2f%n", (double) durationSeqIsland / duration1);
                System.out.println("- - - - - - - - - - - - - - - - - - - - ");

                // Parallel run2
                long start2 = System.nanoTime();
                List<Integer> path2 = ShortestPathGAIslandParallel.run2(graph);
                long end2 = System.nanoTime();
                long duration2 = end2 - start2;

                new GraphPathChecker().check(filename, path2);
                System.out.printf("Time for Common Pool: %.3f ms%n", duration2 / 1_000_000.0);
                System.out.printf("Speedup for Common Pool: %.2f%n", (double) durationSeqIsland / duration2);
                System.out.println("- - - - - - - - - - - - - - - - - - - - ");

                // Measure execution time for ShortestPathGAIslandParallel
                long startIsland = System.nanoTime();
                List<Integer> paralelPath = org.example.parallel.fork_version.ShortestPathGAIslandParallel.run(graph, 12);
                long endIsland = System.nanoTime();
                long durationIsland = endIsland - startIsland;

                GraphPathChecker checkerPar = new GraphPathChecker();
                checkerPar.check(filename, paralelPath);

                System.out.printf("Time for Parallel Final Version: %.3f s%n", durationIsland / 1_000_000_000.0);

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
                List<Integer> paralelPathRecursive = org.example.parallel.executor_version.ShortestPathGAIslandParallel.run(graph, 12);
                long endIslandRecursive = System.nanoTime();
                long durationIslandRecursive = endIslandRecursive - startIslandRecursive;

                GraphPathChecker checkerParRecursive = new GraphPathChecker();
                checkerParRecursive.check(filename, paralelPathRecursive);

                System.out.printf("Time for Parallel Recursive Version: %.3f s%n", durationIslandRecursive / 1_000_000_000.0);

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


