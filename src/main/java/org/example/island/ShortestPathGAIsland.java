package org.example.island;

import org.example.GraphVisualizer;

import java.util.*;
import java.util.concurrent.*;
import static org.example.Constants.*;

public class ShortestPathGAIsland {

    private final int[][] graph;
    private final int startNode = 0;
    private final int endNode = NUM_NODES / 2 + 1;
    private final Random random = new Random();

    private final int numIslands = Runtime.getRuntime().availableProcessors();
    private final int migrationInterval = 10;
    private final int migrantsCount = 2;

    public ShortestPathGAIsland(int[][] graph) {
        this.graph = graph;
    }

    public List<Integer> findShortestPath() {
        ExecutorService executor = Executors.newFixedThreadPool(numIslands);
        List<Island> islands = new ArrayList<>();

        for (int i = 0; i < numIslands; i++) {
            islands.add(new Island(graph));
        }

        for (int gen = 0; gen < GENERATIONS; gen++) {
            List<Callable<Void>> tasks = new ArrayList<>();
            for (Island island : islands) {
                tasks.add(() -> {
                    island.evolve();
                    return null;
                });
            }
            try {
                executor.invokeAll(tasks);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (gen > 0 && gen % migrationInterval == 0) {
                migrate(islands);
            }
        }

        executor.shutdown();

        // Знаходимо найкращий шлях серед усіх островів
        return islands.stream()
                .map(Island::getBestPath)
                .min(Comparator.comparingInt(p -> calculateFitness(p, graph)))
                .orElse(null);
    }

    private void migrate(List<Island> islands) {
        for (int i = 0; i < islands.size(); i++) {
            Island source = islands.get(i);
            Island target = islands.get((i + 1) % islands.size());

            List<List<Integer>> migrants = source.getBestIndividuals(migrantsCount);
            target.addMigrants(migrants);
        }
    }

    public static void run(int[][] graph) {
        ShortestPathGAIsland ga = new ShortestPathGAIsland(graph);
        List<Integer> shortestPath = ga.findShortestPath();

        System.out.println("Fitness: " + ga.calculateFitness(shortestPath, graph));

        if (NUM_NODES <= 20) {
            GraphVisualizer visualizer = new GraphVisualizer(graph);
            visualizer.showGraph(shortestPath);
        }
    }

    private int calculateFitness(List<Integer> path, int[][] graph) {
        int fitness = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            fitness += graph[path.get(i)][path.get(i + 1)];
        }
        return fitness;
    }
}
