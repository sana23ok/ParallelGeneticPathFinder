package org.example.parallel;

import org.example.graph.GraphVisualizer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.example.Constants.GENERATIONS;
import static org.example.Constants.NUM_NODES;

public class ShortestPathGAIslandParallel {

    private final int[][] graph;
    private final int numIslands = Runtime.getRuntime().availableProcessors();
    private final int migrationInterval = 20;
    private final int migrantsCount = 3;

    public ShortestPathGAIslandParallel(int[][] graph) {
        this.graph = graph;
    }

    public List<Integer> findShortestPath() {
        ExecutorService executor = Executors.newFixedThreadPool(numIslands);
        List<IslandParallel> islands = new ArrayList<>();
        //System.out.println("Num of islands: " + numIslands);

        for (int i = 0; i < numIslands; i++) {
            islands.add(new IslandParallel(graph));
        }

        for (int gen = 0; gen < GENERATIONS; gen++) {
            List<Callable<Void>> tasks = new ArrayList<>();
            for (IslandParallel island : islands) {
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
                .map(IslandParallel::getBestPath)
                .min(Comparator.comparingInt(p -> calculateFitness(p, graph)))
                .orElse(null);
    }

    private void migrate(List<IslandParallel> islands) {
        for (int i = 0; i < islands.size(); i++) {
            IslandParallel source = islands.get(i);
            IslandParallel target = islands.get((i + 1) % islands.size());

            List<List<Integer>> migrants = source.getBestIndividuals(migrantsCount);
            target.addMigrants(migrants);
        }
    }

    public static List<Integer> run(int[][] graph) {
        ShortestPathGAIslandParallel ga = new ShortestPathGAIslandParallel(graph);
        List<Integer> shortestPath = ga.findShortestPath();

        System.out.println("Fitness: " + ga.calculateFitness(shortestPath, graph));
        //System.out.println("Shortest path: " + shortestPath);

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
