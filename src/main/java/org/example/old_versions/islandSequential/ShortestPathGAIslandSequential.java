package org.example.old_versions.islandSequential;

import org.example.graph.GraphVisualizer;

import java.util.*;
import static org.example.Constants.*;

public class ShortestPathGAIslandSequential {

    private final int[][] graph;
    private final int startNode = 0;
    private final int endNode = NUM_NODES / 2 + 1;

    public ShortestPathGAIslandSequential(int[][] graph) {
        this.graph = graph;
    }

    public List<Integer> findShortestPath() {
        SeqIsland island = new SeqIsland(graph);

        for (int gen = 0; gen < GENERATIONS; gen++) {
            island.evolve();
        }

        return island.getBestPath();
    }

    public static List<Integer> run(int[][] graph) {
        ShortestPathGAIslandSequential ga = new ShortestPathGAIslandSequential(graph);
        List<Integer> shortestPath = ga.findShortestPath();

        System.out.println("Fitness: " + ga.calculateFitness(shortestPath));
        System.out.println("Shortest path: " + shortestPath);

        if (NUM_NODES <= 20) {
            GraphVisualizer visualizer = new GraphVisualizer(graph);
            visualizer.showGraph(shortestPath);
        }
        return shortestPath;
    }

    private int calculateFitness(List<Integer> path) {
        int fitness = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            fitness += graph[path.get(i)][path.get(i + 1)];
        }
        return fitness;
    }
}
