package org.example.parallel.forkJoin;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.example.Constants.*;

public class ShortestPathGAIslandParallel {
    private final int[][] graph;
    private final int threadCount;

    public ShortestPathGAIslandParallel(int[][] graph, int threadCount) {
        this.graph = graph;
        this.threadCount = threadCount;
    }

    public List<Integer> findShortestPath() {
        List<IslandParallel> islands = new ArrayList<>();
        for (int i = 0; i < NUM_ISLANDS; i++) {
            islands.add(new IslandParallel(graph));
        }

        for (int gen = 0; gen < GENERATIONS; gen++) {
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            for (IslandParallel island : islands) {
                executor.submit(island::evolve);
            }

            executor.shutdown();
            try {
                executor.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (gen > 0 && gen % MIGRATION_INTERVAL == 0) {
                migrate(islands);
            }
        }

        return islands.stream()
                .map(IslandParallel::getBestPath)
                .min(Comparator.comparingInt(p -> calculateFitness(p, graph)))
                .orElse(null);
    }

    private void migrate(List<IslandParallel> islands) {
        IntStream.range(0, islands.size()).parallel().forEach(i -> {
            IslandParallel source = islands.get(i);
            IslandParallel target = islands.get((i + 1) % islands.size());

            List<List<Integer>> migrants = source.getBestIndividuals(MIGRATION_COUNT);
            target.addMigrants(migrants);
        });
    }

    public static List<Integer> run(int[][] graph) {
        int threads = Runtime.getRuntime().availableProcessors();
        ShortestPathGAIslandParallel ga = new ShortestPathGAIslandParallel(graph, threads);
        List<Integer> shortestPath = ga.findShortestPath();

        System.out.println("Fitness: " + ga.calculateFitness(shortestPath, graph));
        System.out.println("Shortest path: " + shortestPath);
        return shortestPath;
    }

    public int calculateFitness(List<Integer> path, int[][] graph) {
        int fitness = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            fitness += graph[path.get(i)][path.get(i + 1)];
        }
        return fitness;
    }
}
