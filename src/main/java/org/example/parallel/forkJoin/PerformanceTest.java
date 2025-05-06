package org.example.parallel.forkJoin;

import org.example.Constants;
import org.example.graph.GraphPathChecker;
import org.example.sequential.ShortestPathGAIslandSequential;

import java.io.*;
import java.util.*;

import static org.example.graph.GraphUtils.generateGraphInput;
import static org.example.graph.GraphUtils.loadGraph;

public class PerformanceTest {

    public static void main(String[] args) {
        System.out.println("threads,nodes,time_ms,fitness,speedup");

        int[] nodeSizes = {4000}; // змінюй під потребу
        int[] threadCounts = {4, 8, 16};

        for (int numNodes : nodeSizes) {
            for (int threads : threadCounts) {
                try {
                    long startTimeGA = System.nanoTime();
                    Constants.NUM_NODES = numNodes;
                    int j=0;
                    for(int i = 0; i < numNodes; i++){
                        j++;
                    }
                    // Генерація графа
                    String filename = "graph_" + numNodes + ".txt";
                    generateGraphInput(filename);
                    int[][] graph = new int[numNodes][numNodes];
                    loadGraph(filename, graph);

                    // Запуск послідовного алгоритму

                    List<Integer> seqPath = ShortestPathGAIslandSequential.run(graph);
                    long endTimeGA = System.nanoTime();
                    long durationGA = endTimeGA - startTimeGA;

                    // Перевірка шляху
                    GraphPathChecker checkerSeq = new GraphPathChecker();
                    checkerSeq.check(filename, seqPath);

                    System.out.println("Nodes: " + numNodes);
                    System.out.println("Time for Sequential: " + (durationGA / 1_000_000.0) + " ms");

                    // Запуск паралельного алгоритму
                    long startTimeIsland = System.nanoTime();
                    ShortestPathGAIslandParallel ga = new ShortestPathGAIslandParallel(graph, threads);
                    List<Integer> parallelPath = ga.findShortestPath();
                    long endTimeIsland = System.nanoTime();
                    long durationIsland = endTimeIsland - startTimeIsland;

                    // Отримання fitness для паралельного шляху
                    int parallelFitness = ga.calculateFitness(parallelPath, graph);

                    System.out.printf("Time for Parallel: %.3f s%n", durationIsland / 1_000_000_000.0);

                    // Обчислення прискорення
                    if (durationIsland != 0) {
                        double speedup = (double) durationGA / durationIsland;
                        System.out.printf("Speedup: %.2f%n", speedup);
                    } else {
                        System.out.println("Time for Parallel is too small to calculate speedup.");
                    }

                    // Виведення результатів
                    System.out.printf("%d,%d,%d,%d%n", threads, numNodes, (durationIsland / 1_000_000), parallelFitness);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
