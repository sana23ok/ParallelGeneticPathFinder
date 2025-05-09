package org.example.recursive_version;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.example.Constants.*;

public class ShortestPathGAIslandParallel {
    private final int[][] graph;

    public ShortestPathGAIslandParallel(int[][] graph) {
        this.graph = graph;
    }

    public List<Integer> findShortestPathParallel() {
        int threadsNum = NUM_ISLANDS * 2 * 2;
        ExecutorService islandExecutor = Executors.newFixedThreadPool(threadsNum);

        // 🛠 Безпечне попереднє створення списку з null
        List<IslandParallel> islands = new ArrayList<>(Collections.nCopies(NUM_ISLANDS, null));

        // ✅ Потокобезпечне створення островів з використанням унікального індексу
        List<Future<?>> islandFutures = IntStream.range(0, NUM_ISLANDS)
                .mapToObj(i -> islandExecutor.submit(() -> {
                    IslandParallel island = new IslandParallel(graph, islandExecutor); //Expected 2 arguments but found 1
                    islands.set(i, island);  // кожен потік пише у свій індекс
                }))
                .collect(Collectors.toList());

        // Очікування завершення ініціалізації островів
        for (Future<?> future : islandFutures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 🔁 Еволюція поколінь
        for (int gen = 0; gen < GENERATIONS; gen++) {
            List<Future<?>> evolutionFutures = new ArrayList<>();
            for (IslandParallel island : islands) {
                evolutionFutures.add(islandExecutor.submit(island::evolve));
            }

            for (Future<?> future : evolutionFutures) {
                try {
                    future.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

        // 🔍 Пошук найкращого шляху серед усіх островів
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

    public static List<Integer> run(int[][] graph) {
        ShortestPathGAIslandParallel ga = new ShortestPathGAIslandParallel(graph);
        List<Integer> shortestPath = ga.findShortestPathParallel();

        System.out.println("Fitness (Parallel): " + ga.calculateFitness(shortestPath, graph));
        System.out.println("Shortest path (Parallel): " + shortestPath);
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
