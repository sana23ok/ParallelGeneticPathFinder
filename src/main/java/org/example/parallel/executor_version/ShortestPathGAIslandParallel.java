package org.example.parallel.executor_version;

import java.util.*;
import java.util.concurrent.*;

import static org.example.Constants.*;

public class ShortestPathGAIslandParallel {
    private final int[][] graph;

    public ShortestPathGAIslandParallel(int[][] graph) {
        this.graph = graph;
    }

    public List<Integer> findShortestPathParallel(int threadsNum) {
        ExecutorService islandExecutor = Executors.newFixedThreadPool(threadsNum);

        //List<IslandParallel> islands = new ArrayList<>(Collections.nCopies(NUM_ISLANDS, null));

        // Створення островів (все ще паралельно)

        List<IslandParallel> islands = new ArrayList<>();

        for (int i = 0; i < NUM_ISLANDS; i++) {
            IslandParallel island = new IslandParallel(graph, islandExecutor);
            islands.add(island);  // додаємо об'єкт, не null
        }

        // Еволюція — послідовно по кожному острову
        for (int gen = 0; gen < GENERATIONS; gen++) {
            for (IslandParallel island : islands) {
                // Еволюція кожного острова — послідовно
                island.evolve();  // усередині – паралельна робота з executor
            }

            if (gen > 0 && gen % MIGRATION_INTERVAL == 0) {
                migrate(islands);
            }
        }

        islandExecutor.shutdown();
        try {
            islandExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return islands.stream()
                .map(IslandParallel::getBestPath)
                .filter(Objects::nonNull)
                .min(Comparator.comparingInt(p -> calculateFitness(p, graph)))
                .orElse(null);
    }



    private void migrate(List<IslandParallel> islands) {
        for (int i = 0; i < islands.size(); i++) {
            IslandParallel source = islands.get(i);
            IslandParallel target = islands.get((i + 1) % islands.size());

            List<List<Integer>> migrants = source.getBestIndividuals(MIGRATION_COUNT);
            target.addMigrants(migrants);
        }
    }

    public static List<Integer> run(int[][] graph, int threadsNum) {
        ShortestPathGAIslandParallel ga = new ShortestPathGAIslandParallel(graph);
        List<Integer> shortestPath = ga.findShortestPathParallel(threadsNum);

        //System.out.println("Fitness (Parallel): " + ga.calculateFitness(shortestPath, graph));
        //System.out.println("Shortest path (Parallel): " + shortestPath);
        return shortestPath;
    }

    private int calculateFitness(List<Integer> path, int[][] graph) {
        if (path == null) return Integer.MAX_VALUE;

        int fitness = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            fitness += graph[path.get(i)][path.get(i + 1)];
        }
        return fitness;
    }
}
