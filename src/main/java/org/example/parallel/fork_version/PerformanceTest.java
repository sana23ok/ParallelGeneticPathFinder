package org.example.parallel.fork_version;

import org.example.Constants;
import org.example.graph.GraphPathChecker;
import org.example.sequential.ShortestPathGAIslandSequential;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.example.graph.GraphUtils.generateGraphInput;
import static org.example.graph.GraphUtils.loadGraph;

public class PerformanceTest {
    public static void main(String[] args) throws IOException {
        int[] threadCounts = {2, 4, 8, 16};

        for (int numNodes = 500; numNodes <= 2000; numNodes += 1500) {
            Constants.NUM_NODES = numNodes;
            String filename = "graph_" + numNodes + "_" + (int) (Constants.CHANCE_EDGE_EXISTS * 100) + "_" + Constants.POPULATION_SIZE + ".txt";
            generateGraphInput(filename);

            int[][] graph = new int[Constants.NUM_NODES][Constants.NUM_NODES];
            loadGraph(filename, graph);

            long startTimeSeq = System.nanoTime();
            List<Integer> seqPath = ShortestPathGAIslandSequential.run(graph);
            long endTimeSeq = System.nanoTime();
            long durationSeqNs = endTimeSeq - startTimeSeq;
            double durationSeqMs = durationSeqNs / 1_000_000.0;

            GraphPathChecker checkerSeq = new GraphPathChecker();
            checkerSeq.check(filename, seqPath);

            System.out.println("\n========== Nodes: " + numNodes + " ==========");
            System.out.printf("Time for Sequential: %.3f ms%n", durationSeqMs);

            for (int threads : threadCounts) {
                long startParallel = System.nanoTime();
                List<Integer> parallelPath = ShortestPathGAIslandParallel.run(graph, threads);
                long endParallel = System.nanoTime();
                long durationParallelNs = endParallel - startParallel;
                double durationParallelMs = durationParallelNs / 1_000_000.0;

                GraphPathChecker checkerPar = new GraphPathChecker();
                checkerPar.check(filename, parallelPath);

                System.out.printf("Threads: %2d | Time: %.3f ms | Speedup: %.2f%n",
                        threads,
                        durationParallelMs,
                        durationSeqMs / durationParallelMs
                );
            }

            deleteFile(filename);
        }

        for (int numNodes = 2000; numNodes <= 10000; numNodes += 2000) {
            Constants.NUM_NODES = numNodes;
            String filename = "graph_" + numNodes + "_" + (int) (Constants.CHANCE_EDGE_EXISTS * 100) + "_" + Constants.POPULATION_SIZE + ".txt";
            generateGraphInput(filename);

            int[][] graph = new int[Constants.NUM_NODES][Constants.NUM_NODES];
            loadGraph(filename, graph);

            long startTimeSeq = System.nanoTime();
            List<Integer> seqPath = ShortestPathGAIslandSequential.run(graph);
            long endTimeSeq = System.nanoTime();
            long durationSeqNs = endTimeSeq - startTimeSeq;
            double durationSeqMs = durationSeqNs / 1_000_000.0;

            GraphPathChecker checkerSeq = new GraphPathChecker();
            checkerSeq.check(filename, seqPath);

            System.out.println("\n========== Nodes: " + numNodes + " ==========");
            System.out.printf("Time for Sequential: %.3f ms%n", durationSeqMs);

            for (int threads : threadCounts) {
                long startParallel = System.nanoTime();
                List<Integer> parallelPath = ShortestPathGAIslandParallel.run(graph, threads);
                long endParallel = System.nanoTime();
                long durationParallelNs = endParallel - startParallel;
                double durationParallelMs = durationParallelNs / 1_000_000.0;

                GraphPathChecker checkerPar = new GraphPathChecker();
                checkerPar.check(filename, parallelPath);

                System.out.printf("Threads: %2d | Time: %.3f ms | Speedup: %.2f%n",
                        threads,
                        durationParallelMs,
                        durationSeqMs / durationParallelMs
                );
            }

            deleteFile(filename);
        }
    }

    private static void deleteFile(String filename) {
        File file = new File(filename);
        if (!file.delete()) {
            System.out.println("Не вдалося видалити файл " + filename);
        }
    }
}