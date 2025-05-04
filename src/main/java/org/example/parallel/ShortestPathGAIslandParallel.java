package org.example.parallel;

import org.example.graph.GraphVisualizer;
import java.util.*;
import java.util.concurrent.*;

import static org.example.Constants.*;

public class ShortestPathGAIslandParallel {
    private final int[][] graph;

    public ShortestPathGAIslandParallel(int[][] graph) {
        this.graph = graph;
    }

    public List<Integer> findShortestPath() throws InterruptedException, ExecutionException {
        List<IslandParallel> islands = new ArrayList<>();
        for (int i = 0; i < NUM_ISLANDS; i++) {
            islands.add(new IslandParallel(graph));
        }

        ExecutorService executor = Executors.newFixedThreadPool(NUM_ISLANDS);

        for (int gen = 0; gen < GENERATIONS; gen++) {
            List<Future<?>> futures = new ArrayList<>();
            for (IslandParallel island : islands) {
                futures.add(executor.submit(island::evolve));
            }

            // Wait for all islands to finish this generation
            for (Future<?> f : futures) f.get();

            // Migration step
            if (gen > 0 && gen % MIGRATION_INTERVAL == 0) {
                migrate(islands);
            }
        }

        executor.shutdown();

        // Return best overall path
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

    public static List<Integer> run(int[][] graph) throws InterruptedException, ExecutionException {
        ShortestPathGAIslandParallel ga = new ShortestPathGAIslandParallel(graph);
        List<Integer> shortestPath = ga.findShortestPath();

        System.out.println("Fitness: " + ga.calculateFitness(shortestPath, graph));
        if (NUM_NODES <= 20) {
            new GraphVisualizer(graph).showGraph(shortestPath);
        }
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
