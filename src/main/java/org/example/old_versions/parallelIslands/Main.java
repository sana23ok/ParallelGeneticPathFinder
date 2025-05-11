package org.example.old_versions.parallelIslands;

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
                System.out.println("Nodes: "+NUM_NODES);

                // Measure execution time for ShortestPathGAIslandSequential
                long startSeqIsland = System.nanoTime();
                List<Integer> seqPath = ShortestPathGAIslandSequential.run(graph);
                long endSeqIsland = System.nanoTime();
                long durationSeqIsland = endSeqIsland - startSeqIsland;

                GraphPathChecker checkerSeq = new GraphPathChecker();
                checkerSeq.check(filename, seqPath);

                System.out.printf("Time for Sequential: %.3f s%n", durationSeqIsland / 1_000_000_000.0);

                // Measure execution time for ShortestPathGAIslandParallel
                long startIsland = System.nanoTime();
                List<Integer> paralelPath = ShortestPathGAIslandParallel.run(graph);
                long endIsland = System.nanoTime();
                long durationIsland = endIsland - startIsland;

                GraphPathChecker checkerPar = new GraphPathChecker();
                checkerPar.check(filename, paralelPath);

                System.out.printf("Time for Parallel: %.3f s%n", durationIsland / 1_000_000_000.0);

                // Calculate speedup
                if (durationIsland != 0) {
                    double speedup = (double) durationSeqIsland / durationIsland;
                    System.out.printf("Speedup: %.2f%n", speedup);
                } else {
                    System.out.println("Time for Parallel is too small to calculate speedup.");
                }

                System.out.println("- - - - - - - - - - - - - - - - - - - - ");
            }

        } catch (IOException e) {
            System.err.println("Error occurred while handling the graph file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
        }
    }
}

