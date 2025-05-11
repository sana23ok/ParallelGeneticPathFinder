package org.example;

import org.example.sequential.ShortestPathGAIslandSequential;
import java.io.IOException;
import java.util.List;
import static org.example.graph.GraphUtils.*;

public class PerformanceTestSizes {
    private static final int[] THREAD_COUNTS = {12}; // Кількість потоків
    private static final int[] GRAPH_SIZES = {500, 2000, 4000, 8000, 10000}; // Кількість вузлів у графі
    private static final int NUM_ITERATIONS = 10; // Кількість ітерацій для усереднення

    public static void main(String[] args) {
        String filename = "graph.txt";

        System.out.println("=========================================");
        System.out.println("Size | Sequential Time | ForkJoinPool Time | ExecutorService Time | ForkJoinPool Speedup | ExecutorService Speedup ");

        try {
            for (int graphSize : GRAPH_SIZES) {
                Constants.NUM_NODES = graphSize;
                generateGraphInput(filename);

                int[][] graph = new int[graphSize][graphSize];
                loadGraph(filename, graph);

                long totalSeqTime = 0;
                long totalForkJoinTime = 0;
                long totalExecutorTime = 0;

                for (int i = 0; i < NUM_ITERATIONS; i++) {
                    // Sequential Execution
                    long startSeq = System.currentTimeMillis();
                    List<Integer> seqPath = ShortestPathGAIslandSequential.run(graph);
                    long endSeq = System.currentTimeMillis();
                    totalSeqTime += (endSeq - startSeq);

                    // Parallel ForkJoinPool Version
                    long startForkJoin = System.currentTimeMillis();
                    List<Integer> forkJoinPath = org.example.parallel.fork_version.ShortestPathGAIslandParallel.run(graph, THREAD_COUNTS[0]);
                    long endForkJoin = System.currentTimeMillis();
                    totalForkJoinTime += (endForkJoin - startForkJoin);

                    // Parallel ExecutorService Version
                    long startExecutor = System.currentTimeMillis();
                    List<Integer> executorPath = org.example.parallel.executor_version.ShortestPathGAIslandParallel.run(graph, THREAD_COUNTS[0]);
                    long endExecutor = System.currentTimeMillis();
                    totalExecutorTime += (endExecutor - startExecutor);
                }

                // Обчислення середнього часу виконання
                long avgSeqTime = totalSeqTime / NUM_ITERATIONS;
                long avgForkJoinTime = totalForkJoinTime / NUM_ITERATIONS;
                long avgExecutorTime = totalExecutorTime / NUM_ITERATIONS;

                // Обчислення прискорення
                double speedupForkJoin = (double) avgSeqTime / avgForkJoinTime;
                double speedupExecutor = (double) avgSeqTime / avgExecutorTime;

                // Вивід результатів
                System.out.printf("%12d | %18d | %16d | %19d | %9.2f | %9.2f%n",
                        graphSize, avgSeqTime, avgForkJoinTime, avgExecutorTime, speedupForkJoin, speedupExecutor);
            }
        } catch (IOException e) {
            System.err.println("Помилка під час обробки файлу графа: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Несподівана помилка: " + e.getMessage());
        }
    }
}
