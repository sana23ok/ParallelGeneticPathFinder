package org.example.parallel.fork_version;

import org.example.Constants;
import org.example.graph.GraphVisualizer;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static org.example.Constants.*;


class ParallelExecutor {
    public static void runInCustomPool(ForkJoinPool pool, Runnable task) {
        try {
            pool.submit(task).get(); // wait for completion
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Parallel execution failed", e);
        }
    }
}


public class ShortestPathGAIslandParallel {

    private final int[][] graph;
    private final ForkJoinPool forkJoinPool;
    private List<IslandParallel> islands;

    // Конструктор, який приймає існуючий ForkJoinPool
    public ShortestPathGAIslandParallel(int[][] graph, ForkJoinPool forkJoinPool) {
        this.graph = graph;
        this.forkJoinPool = forkJoinPool;
        this.islands = IntStream.range(0, Constants.NUM_ISLANDS)
                .mapToObj(i -> new IslandParallel(graph, this.forkJoinPool))
                .collect(java.util.stream.Collectors.toList());

        // Ініціалізація популяції одразу після створення островів
        ParallelExecutor.runInCustomPool(forkJoinPool, () ->
                islands.parallelStream().forEach(IslandParallel::initializePopulation)
        );
    }

    public List<Integer> findShortestPath() {
        for (int gen = 0; gen < GENERATIONS; gen++) {

            ParallelExecutor.runInCustomPool(forkJoinPool, () ->
                    islands.parallelStream().forEach(IslandParallel::evolve)
            );

            if (gen > 0 && gen % MIGRATION_INTERVAL == 0) {
                migrateIslands();
            }
        }

        forkJoinPool.shutdown();

        return islands.stream()
                .map(IslandParallel::getBestPath)
                .min(Comparator.comparingInt(p -> calculateFitness(p, graph)))
                .orElse(null);
    }

    private void migrateIslands() {
        List<List<List<Integer>>> migrantsList = IntStream.range(0, islands.size())
                .mapToObj(i -> islands.get(i).getBestIndividuals(MIGRATION_COUNT))
                .collect(Collectors.toList());

        for (int i = 0; i < islands.size(); i++) {
            IslandParallel target = islands.get((i + 1) % islands.size());
            target.addMigrants(migrantsList.get(i));
        }
    }

    private int calculateFitness(List<Integer> path, int[][] graph) {
        int fitness = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            fitness += graph[path.get(i)][path.get(i + 1)];
        }
        return fitness;
    }

    public static List<Integer> run(int[][] graph, int numThreads) {
        ForkJoinPool customPool = new ForkJoinPool(numThreads);
        ShortestPathGAIslandParallel ga = new ShortestPathGAIslandParallel(graph, customPool);
        List<Integer> shortestPath = ga.findShortestPath();
        customPool.shutdown();

        //System.out.println("Fitness (threads: " + numThreads + "): " + ga.calculateFitness(shortestPath, graph));
        //System.out.println("Shortest path (threads: " + numThreads + "): " + shortestPath);

        if (NUM_NODES <= 20) {
            GraphVisualizer visualizer = new GraphVisualizer(graph);
            visualizer.showGraph(shortestPath);
        }
        return shortestPath;
    }

}