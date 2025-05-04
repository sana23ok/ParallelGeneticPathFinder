package org.example.islandParallel;

import org.example.graph.GraphVisualizer;

import java.util.*;
import java.util.concurrent.*;
import static org.example.Constants.*;

public class ShortestPathGAIslandParallel {

    private final int[][] graph;
    private final int startNode = 0;
    private final int endNode = NUM_NODES / 2 + 1;
    private final Random random = new Random();

    private final int numIslands = Runtime.getRuntime().availableProcessors();
    private final int migrationInterval = 20;
    private final int migrantsCount = 3;

    public ShortestPathGAIslandParallel(int[][] graph) {
        this.graph = graph;
    }

    public List<Integer> findShortestPath() {
        ExecutorService executor = Executors.newFixedThreadPool(numIslands);
        List<Island> islands = new ArrayList<>();
        //System.out.println("Num of islands: " + numIslands);

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

    public static List<Integer> run(int[][] graph) {
        ShortestPathGAIslandParallel ga = new ShortestPathGAIslandParallel(graph);
        List<Integer> shortestPath = ga.findShortestPath();

        System.out.println("Fitness: " + ga.calculateFitness(shortestPath, graph));
        System.out.println("Shortest path: " + shortestPath);

        if (NUM_NODES <= 20) {
            GraphVisualizer visualizer = new GraphVisualizer(graph);
            visualizer.showGraph(shortestPath);
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
