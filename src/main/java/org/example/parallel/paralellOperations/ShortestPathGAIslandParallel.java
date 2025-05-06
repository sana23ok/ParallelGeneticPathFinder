package org.example.parallel.paralellOperations;

import java.util.*;
import java.util.concurrent.*;

import static org.example.Constants.*;

public class ShortestPathGAIslandParallel {

    private final int[][] graph;

    public ShortestPathGAIslandParallel(int[][] graph) {
        this.graph = graph;
    }

    public List<Integer> findShortestPath() {
        List<IslandParallel> islands = new ArrayList<>();

        // Ініціалізація островів
        for (int i = 0; i < NUM_ISLANDS; i++) {
            islands.add(new IslandParallel(graph));
        }

        // Еволюція поколінь
        for (int gen = 0; gen < GENERATIONS; gen++) {
            List<Future<Void>> futures = new ArrayList<>();

            int NUM_THREADS = 2;
            ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS); // Пул потоків для паралелізму

            // Паралельна еволюція кожного острова
            for (IslandParallel island : islands) {
                futures.add(executorService.submit(() -> {
                    island.evolve();
                    return null;
                }));
            }

            // Чекаємо на завершення всіх потоків
            for (Future<Void> future : futures) {
                try {
                    future.get(); // Чекаємо на виконання кожного потоку
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            executorService.shutdown(); // Закриваємо пул потоків

            // Міграція, якщо потрібно
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

    private void migrate(List<IslandParallel> islands) {
        for (int i = 0; i < islands.size(); i++) {
            IslandParallel source = islands.get(i);
            IslandParallel target = islands.get((i + 1) % islands.size());

            List<List<Integer>> migrants = source.getBestIndividuals(MIGRATION_COUNT);
            target.addMigrants(migrants);
        }
    }

    public static List<Integer> run(int[][] graph) {
        ShortestPathGAIslandParallel ga = new ShortestPathGAIslandParallel(graph);
        List<Integer> shortestPath = ga.findShortestPath();

        System.out.println("Fitness: " + ga.calculateFitness(shortestPath, graph));
        System.out.println("Shortest path: " + shortestPath);
//        if (NUM_NODES <= 20) {
//            GraphVisualizer visualizer = new GraphVisualizer(graph);
//            visualizer.showGraph(shortestPath);
//        }
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
