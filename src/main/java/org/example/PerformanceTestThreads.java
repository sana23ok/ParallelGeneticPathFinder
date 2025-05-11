package org.example;

import org.example.graph.GraphPathChecker;
import org.example.parallel.fork_version.ShortestPathGAIslandParallel;
import org.example.sequential.ShortestPathGAIslandSequential;
import java.io.IOException;
import java.util.List;
import static org.example.graph.GraphUtils.*;

public class PerformanceTestThreads {
    // Масиви для перевірки різних параметрів
    private static final int[] THREAD_COUNTS = {2, 4, 8, 12, 16}; // Кількість потоків
    private static final int[] GRAPH_SIZES = {500, 2000, 4000, 8000, 10000}; // Кількість вузлів у графі

    public static void main(String[] args) {
        String filename = "graph.txt";
        try {
            for (int graphSize : GRAPH_SIZES) {
                Constants.NUM_NODES = graphSize;
                System.out.println("=========================================");
                System.out.println("Testing with graph size: " + graphSize);
                generateGraphInput(filename);

                int[][] graph = new int[graphSize][graphSize];
                loadGraph(filename, graph);

                for (int threadCount : THREAD_COUNTS) {
                    System.out.println("-----------------------------------------");
                    System.out.println("Testing with " + threadCount + " threads");

                    // Sequential
                    long startSeqIsland = System.currentTimeMillis();
                    List<Integer> seqPath = ShortestPathGAIslandSequential.run(graph);
                    long endSeqIsland = System.currentTimeMillis();
                    long durationSeqIsland = endSeqIsland - startSeqIsland;

                    new GraphPathChecker().check(filename, seqPath);
                    System.out.printf("Time for Sequential: %d ms%n", durationSeqIsland);
                    System.out.println("- - - - - - - - - - - - - - - - - - - - ");

                    // Parallel ForkJoinPool Version
                    long startParallelForkJoin = System.currentTimeMillis();
                    List<Integer> parallelPathForkJoin = ShortestPathGAIslandParallel.run(graph, threadCount);
                    long endParallelForkJoin = System.currentTimeMillis();
                    long durationParallelForkJoin = endParallelForkJoin - startParallelForkJoin;

                    new GraphPathChecker().check(filename, parallelPathForkJoin);
                    System.out.printf("Time for Parallel ForkJoinPool Version (%d threads): %d ms%n",
                            threadCount, durationParallelForkJoin);

                    // Speedup calculation
                    if (durationParallelForkJoin != 0) {
                        double speedup = (double) durationSeqIsland / durationParallelForkJoin;
                        System.out.printf("Speedup: %.2f%n", speedup);
                    }
                    System.out.println("- - - - - - - - - - - - - - - - - - - - ");

                    // Parallel ExecutorService Version
                    long startParallelExecutor = System.currentTimeMillis();
                    List<Integer> parallelPathExecutor = org.example.parallel.executor_version.ShortestPathGAIslandParallel.run(graph, threadCount);
                    long endParallelExecutor = System.currentTimeMillis();
                    long durationParallelExecutor = endParallelExecutor - startParallelExecutor;

                    new GraphPathChecker().check(filename, parallelPathExecutor);
                    System.out.printf("Time for Parallel ExecutorService Version (%d threads): %d ms%n",
                            threadCount, durationParallelExecutor);

                    // Speedup calculation
                    if (durationParallelExecutor != 0) {
                        double speedup = (double) durationSeqIsland / durationParallelExecutor;
                        System.out.printf("Speedup: %.2f%n", speedup);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error occurred while handling the graph file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
        }
    }
}
