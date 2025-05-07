package org.example.parallel.paralellOperations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.example.Constants.*;

public class ShortestPathGAIslandParallel {
    private final int[][] graph;

    public ShortestPathGAIslandParallel(int[][] graph) {
        this.graph = graph;
    }

    // послідовне виконання островів, але розпралелені операції
    public List<Integer> findShortestPath1() {
        List<IslandParallel> islands = new ArrayList<>();

        // Ініціалізація островів
        for (int i = 0; i < NUM_ISLANDS; i++) {
            IslandParallel island = new IslandParallel(graph);
            island.initializePopulation(); // <-- Ініціалізує популяцію одразу після створення
            islands.add(island);
        }

        // Еволюція поколінь
        for (int gen = 0; gen < GENERATIONS; gen++) {
            for (IslandParallel island : islands) {
                island.evolve();  // виконується послідовно, але всередині — паралельно
            }

            if (gen > 0 && gen % MIGRATION_INTERVAL == 0) {
                migrate(islands);
            }
        }

        // Знаходимо найкращий шлях серед усіх островів
        return islands.stream()
                .map(IslandParallel::getBestPath)
                .min(Comparator.comparingInt(p -> calculateFitness(p, graph)))
                .orElse(null);
    }

    // пралельне виконання островів і всі розпралелені операції, але в ForkJoinPool.commonPool()
    public List<Integer> findShortestPath2() {
        List<IslandParallel> islands = new ArrayList<>();

        // Ініціалізація островів
        for (int i = 0; i < NUM_ISLANDS; i++) {
            IslandParallel island = new IslandParallel(graph);
            island.initializePopulation(); // <-- Ініціалізує популяцію одразу після створення
            islands.add(island);
        }

        ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

        try {
            for (int gen = 0; gen < GENERATIONS; gen++) {
                forkJoinPool.submit(() ->
                        islands.parallelStream().forEach(IslandParallel::evolve)
                ).get(); // Очікуємо завершення всіх еволюцій

                if (gen > 0 && gen % MIGRATION_INTERVAL == 0) {
                    migrate(islands);
                }
            }

            // Пошук найкращого шляху
            return islands.stream()
                    .map(IslandParallel::getBestPath)
                    .min(Comparator.comparingInt(p -> calculateFitness(p, graph)))
                    .orElse(null);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            return null;
        } finally {
            forkJoinPool.shutdown();
        }
    }

    private void migrate(List<IslandParallel> islands) {
        for (int i = 0; i < islands.size(); i++) {
            IslandParallel source = islands.get(i);
            IslandParallel target = islands.get((i + 1) % islands.size());

            List<List<Integer>> migrants = source.getBestIndividuals(MIGRATION_COUNT);
            target.addMigrants(migrants);
        }
    }

    public static List<Integer> run1(int[][] graph) {
        ShortestPathGAIslandParallel ga = new ShortestPathGAIslandParallel(graph);
        List<Integer> shortestPath = ga.findShortestPath1();

        System.out.println("Fitness: " + ga.calculateFitness(shortestPath, graph));
        System.out.println("Shortest path: " + shortestPath);
        return shortestPath;
    }

    public static List<Integer> run2(int[][] graph) {
        ShortestPathGAIslandParallel ga = new ShortestPathGAIslandParallel(graph);
        List<Integer> shortestPath = ga.findShortestPath2();

        System.out.println("Fitness: " + ga.calculateFitness(shortestPath, graph));
        System.out.println("Shortest path: " + shortestPath);
        return shortestPath;
    }

    private int calculateFitness(List<Integer> path, int[][] graph) {
        int fitness = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            fitness += graph[path.get(i)][path.get(i + 1)];
        }
        return fitness;
    }
}
