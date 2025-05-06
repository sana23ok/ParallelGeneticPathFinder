package org.example.parallel.parallelStream;

import org.example.graph.GraphVisualizer;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.example.Constants.*;

class IslandEvolutionTask extends RecursiveAction {
    private final IslandParallel island;

    public IslandEvolutionTask(IslandParallel island) {
        this.island = island;
    }

    @Override
    protected void compute() {
        island.evolve();
    }
}

class MigrationTask extends RecursiveAction {
    private final List<IslandParallel> islands;

    public MigrationTask(List<IslandParallel> islands) {
        this.islands = islands;
    }

    @Override
    protected void compute() {
        List<List<List<Integer>>> migrantsList = IntStream.range(0, islands.size())
                .mapToObj(i -> islands.get(i).getBestIndividuals(MIGRATION_COUNT))
                .collect(Collectors.toList());

        for (int i = 0; i < islands.size(); i++) {
            IslandParallel target = islands.get((i + 1) % islands.size());
            target.addMigrants(migrantsList.get(i));
        }
    }
}

public class ShortestPathGAIslandParallel {

    private final int[][] graph;
    private final ForkJoinPool forkJoinPool;
    private final List<IslandParallel> islands;

    public ShortestPathGAIslandParallel(int[][] graph) {
        this.graph = graph;
        this.forkJoinPool = new ForkJoinPool(NUM_ISLANDS);
        this.islands = IntStream.range(0, NUM_ISLANDS)
                .mapToObj(i -> new IslandParallel(graph))
                .collect(Collectors.toList());
    }

    public List<Integer> findShortestPath() {
        for (int gen = 0; gen < GENERATIONS; gen++) {
            List<IslandEvolutionTask> evolutionTasks = islands.stream()
                    .map(IslandEvolutionTask::new)
                    .collect(Collectors.toList());

            evolutionTasks.forEach(forkJoinPool::invoke);

            if (gen > 0 && gen % MIGRATION_INTERVAL == 0) {
                forkJoinPool.invoke(new MigrationTask(islands));
            }
        }

        forkJoinPool.shutdown();

        return islands.stream()
                .map(IslandParallel::getBestPath)
                .min(Comparator.comparingInt(p -> calculateFitness(p, graph)))
                .orElse(null);
    }

    private int calculateFitness(List<Integer> path, int[][] graph) {
        int fitness = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            fitness += graph[path.get(i)][path.get(i + 1)];
        }
        return fitness;
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
}
