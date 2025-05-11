package org.example.sequential;

import org.example.graph.GraphVisualizer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.example.Constants.*;

public class ShortestPathGAIslandSequential {
    private final int[][] graph;

    public ShortestPathGAIslandSequential(int[][] graph) {
        this.graph = graph;
    }

    public List<Integer> findShortestPath() {
        List<IslandSequential> islands = new ArrayList<>();

        // Ініціалізація островів
        for (int i = 0; i < NUM_ISLANDS; i++) {
            islands.add(new IslandSequential(graph));
        }

        // Еволюція поколінь
        for (int gen = 0; gen < GENERATIONS; gen++) {
            for (IslandSequential island : islands) {
                island.evolve();
            }

            if (gen > 0 && gen % MIGRATION_INTERVAL == 0) {
                migrate(islands);
            }
        }

        // Знаходимо найкращий шлях серед усіх островів
        return islands.stream()
                .map(IslandSequential::getBestPath)
                .min(Comparator.comparingInt(p -> calculateFitness(p, graph)))
                .orElse(null);
    }

    private void migrate(List<IslandSequential> islands) {
        for (int i = 0; i < islands.size(); i++) {
            IslandSequential source = islands.get(i);
            IslandSequential target = islands.get((i + 1) % islands.size());

            List<List<Integer>> migrants = source.getBestIndividuals(MIGRATION_COUNT);
            target.addMigrants(migrants);
        }
    }

    public static List<Integer> run(int[][] graph) {
        ShortestPathGAIslandSequential ga = new ShortestPathGAIslandSequential(graph);
        List<Integer> shortestPath = ga.findShortestPath();

        //System.out.println("Fitness: " + ga.calculateFitness(shortestPath, graph));
        //System.out.println("Shortest path: " + shortestPath);

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
