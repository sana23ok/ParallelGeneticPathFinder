package org.example;

import java.io.IOException;
import java.util.List;
import static org.example.graph.GraphUtils.*;
import org.example.graph.GraphPathChecker;
import org.example.parallel.finalVersion.ShortestPathGAIslandParallel;
import org.example.sequential.ShortestPathGAIslandSequential;
import java.io.File;

public class MainTest {
    public static void main(String[] args) throws IOException {
        for (int numNodes = 500; numNodes <= 2500; numNodes += 500) {
            Constants.NUM_NODES = numNodes;
            String filename = "graph_" + numNodes + "_" + (int) (Constants.CHANCE_EDGE_EXISTS * 100) + "_" + Constants.POPULATION_SIZE + ".txt";
            generateGraphInput(filename);

            int[][] graph = new int[Constants.NUM_NODES][Constants.NUM_NODES];
            loadGraph(filename, graph);

            long startTimeGA = System.nanoTime();
            List<Integer> seqPath = ShortestPathGAIslandSequential.run(graph);
            long endTimeGA = System.nanoTime();
            long durationGA = endTimeGA - startTimeGA;

            GraphPathChecker checkerSeq = new GraphPathChecker();
            checkerSeq.check(filename, seqPath);

            System.out.println("Nodes: " + numNodes);
            System.out.println("Time for Sequential: " + (durationGA / 1_000_000.0) + " ms");

            long startIsland = System.nanoTime();
            List<Integer> paralelPath = ShortestPathGAIslandParallel.run(graph);
            long endIsland = System.nanoTime();
            long durationIsland = endIsland - startIsland;

            GraphPathChecker checkerPar = new GraphPathChecker();
            checkerPar.check(filename, paralelPath);

            System.out.printf("Time for Parallel: %.3f s%n", durationIsland / 1_000_000_000.0);

            if (durationIsland != 0) {
                double speedup = (double) durationGA / durationIsland;
                System.out.printf("Speedup: %.2f%n", speedup);
            } else {
                System.out.println("Time for Parallel is too small to calculate speedup.");
            }

            // Видалення файлу після завершення
            deleteFile(filename);
        }

        for (int numNodes = 3000; numNodes <= 10000; numNodes += 1000) {
            Constants.NUM_NODES = numNodes;
            String filename = "graph_" + numNodes + "_" + (int) (Constants.CHANCE_EDGE_EXISTS * 100) + "_" + Constants.POPULATION_SIZE + ".txt";
            generateGraphInput(filename);

            int[][] graph = new int[Constants.NUM_NODES][Constants.NUM_NODES];
            loadGraph(filename, graph);

            long startTimeGA = System.nanoTime();
            List<Integer> seqPath = ShortestPathGAIslandSequential.run(graph);
            long endTimeGA = System.nanoTime();
            long durationGA = endTimeGA - startTimeGA;

            GraphPathChecker checkerSeq = new GraphPathChecker();
            checkerSeq.check(filename, seqPath);

            System.out.println("Nodes: " + numNodes);
            System.out.println("Time for Sequential: " + (durationGA / 1_000_000.0) + " ms");

            long startIsland = System.nanoTime();
            List<Integer> paralelPath = ShortestPathGAIslandParallel.run(graph);
            long endIsland = System.nanoTime();
            long durationIsland = endIsland - startIsland;

            GraphPathChecker checkerPar = new GraphPathChecker();
            checkerPar.check(filename, paralelPath);

            System.out.printf("Time for Parallel: %.3f s%n", durationIsland / 1_000_000_000.0);

            if (durationIsland != 0) {
                double speedup = (double) durationGA / durationIsland;
                System.out.printf("Speedup: %.2f%n", speedup);
            } else {
                System.out.println("Time for Parallel is too small to calculate speedup.");
            }

            // Видалення файлу після завершення
            deleteFile(filename);
        }
    }

    // Метод для видалення файлу
    private static void deleteFile(String filename) {
        File file = new File(filename);
        if (file.exists() && file.delete()) {
//            System.out.println("Файл " + filename + " видалено.");
        } else {
            System.out.println("Не вдалося видалити файл " + filename);
        }
    }
}


