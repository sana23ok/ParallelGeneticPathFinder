package org.example;

import org.example.graphs.GraphPathChecker;
import org.example.islandParallel.ShortestPathGAIslandParallel;
import org.example.islandSequential.ShortestPathGAIslandSequential;

import java.io.IOException;
import static org.example.Constants.NUM_NODES;
import static org.example.GraphUtils.*;

public class MainIsland {
    public static void main(String[] args) {
        String filename = "graph.txt";
        try {
            // Generate graph input file
            generateGraphInput(filename);

            // Load graph into memory
            int[][] graph = new int[NUM_NODES][NUM_NODES];
            loadGraph(filename, graph);
            //for (int i = 0; i < 4; i++) {

                // Measure execution time for ShortestPathGAParallel (parallel)
                long startSeqIsland = System.nanoTime();
                ShortestPathGAIslandSequential.run(graph);
                long endSeqIsland = System.nanoTime();
                long durationSeqIsland = endSeqIsland - startSeqIsland;

                System.out.println("Time for Sequential: " + durationSeqIsland + " ns");

                // Measure execution time for ShortestPathGAParallel (parallel)
                long startIsland = System.nanoTime();
                ShortestPathGAIslandParallel.run(graph);
                long endIsland = System.nanoTime();
                long durationIsland = endIsland - startIsland;
                //GraphPathChecker checker = new GraphPathChecker();

                // Calculate speedup
                if (durationSeqIsland != 0) {
                    double speedup = (double) durationSeqIsland / durationIsland;
                    System.out.println("Speedup Island: " + speedup);
                } else {
                    System.out.println("Time for Sequential ShortestPathGA is too small to calculate speedup.");
                }

                System.out.println("- - - - - - - - - - - - - - - - - - - - ");
            //}

        } catch (IOException e) {
            System.err.println("Error occurred while handling the graph file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
        }
    }
}

