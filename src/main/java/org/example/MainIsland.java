package org.example;

import org.example.graph.GraphPathChecker;
import org.example.sequential.ShortestPathGAIslandSequential;
import org.example.parallel.ShortestPathGAIslandParallel;

import java.io.IOException;
import java.util.List;

import static org.example.Constants.NUM_NODES;
import static org.example.graph.GraphUtils.*;

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
                List<Integer> seqPath = ShortestPathGAIslandSequential.run(graph);
                long endSeqIsland = System.nanoTime();
                long durationSeqIsland = endSeqIsland - startSeqIsland;

                GraphPathChecker checkerSeq = new GraphPathChecker();
                checkerSeq.check(filename, seqPath);

                System.out.println("Time for Sequential: " + durationSeqIsland + " ns");

                // Measure execution time for ShortestPathGAParallel (parallel)
                long startIsland = System.nanoTime();
                List<Integer> paralelPath =  ShortestPathGAIslandParallel.run(graph);
                long endIsland = System.nanoTime();
                long durationIsland = endIsland - startIsland;

                GraphPathChecker checkerPar = new GraphPathChecker();
                checkerPar.check(filename, paralelPath);

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

