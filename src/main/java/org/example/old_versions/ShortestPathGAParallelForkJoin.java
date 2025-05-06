package org.example.old_versions;

import org.example.graph.GraphVisualizer;

import java.util.*;
import java.util.concurrent.*;

import static org.example.Constants.*;

public class ShortestPathGAParallelForkJoin {
    private final int[][] graph;
    private final int startNode = 0;
    private final int endNode = NUM_NODES / 2 + 1;
    private final Random random = new Random();
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    public ShortestPathGAParallelForkJoin(int[][] graph) {
        this.graph = graph;
    }

    public List<Integer> findShortestPath() {
        List<List<Integer>> population = forkJoinPool.invoke(new InitializePopulationTask());

        for (int i = 0; i < GENERATIONS; i++) {
            population = forkJoinPool.invoke(new CreateNextGenerationTask(population));
        }

        return getBestPath(population);
    }

    private class InitializePopulationTask extends RecursiveTask<List<List<Integer>>> {
        @Override
        protected List<List<Integer>> compute() {
            List<GeneratePathTask> tasks = new ArrayList<>();
            for (int i = 0; i < POPULATION_SIZE; i++) {
                tasks.add(new GeneratePathTask());
            }
            invokeAll(tasks);
            List<List<Integer>> result = new ArrayList<>();
            for (GeneratePathTask task : tasks) {
                result.add(task.join());
            }
            return result;
        }
    }

    private class GeneratePathTask extends RecursiveTask<List<Integer>> {
        @Override
        protected List<Integer> compute() {
            List<Integer> path;
            do {
                path = generateRandomPath();
            } while (!isValidPath(path));
            return path;
        }
    }

    private class CreateNextGenerationTask extends RecursiveTask<List<List<Integer>>> {
        private final List<List<Integer>> population;

        public CreateNextGenerationTask(List<List<Integer>> population) {
            this.population = population;
        }

        @Override
        protected List<List<Integer>> compute() {
            List<ReproduceTask> tasks = new ArrayList<>();
            for (int i = 0; i < POPULATION_SIZE; i++) {
                tasks.add(new ReproduceTask(population));
            }
            invokeAll(tasks);
            List<List<Integer>> nextGen = new ArrayList<>();
            for (ReproduceTask task : tasks) {
                nextGen.add(task.join());
            }
            return nextGen;
        }
    }

    private class ReproduceTask extends RecursiveTask<List<Integer>> {
        private final List<List<Integer>> population;

        public ReproduceTask(List<List<Integer>> population) {
            this.population = population;
        }

        @Override
        protected List<Integer> compute() {
            List<Integer> parent1 = forkJoinPool.invoke(new TournamentSelectionTask(population));
            List<Integer> parent2 = forkJoinPool.invoke(new TournamentSelectionTask(population));
            List<Integer> child = crossover(parent1, parent2);
            mutate(child);
            return isValidPath(child) ? child : population.get(random.nextInt(POPULATION_SIZE));
        }
    }

    private class TournamentSelectionTask extends RecursiveTask<List<Integer>> {
        private final List<List<Integer>> population;

        public TournamentSelectionTask(List<List<Integer>> population) {
            this.population = population;
        }

        @Override
        protected List<Integer> compute() {
            List<List<Integer>> candidates = new ArrayList<>();
            for (int i = 0; i < TOURNAMENT_SIZE; i++) {
                candidates.add(population.get(random.nextInt(POPULATION_SIZE)));
            }
            return candidates.stream()
                    .min(Comparator.comparingInt(ShortestPathGAParallelForkJoin.this::calculateFitness))
                    .orElse(population.get(0));
        }
    }

    private List<Integer> getBestPath(List<List<Integer>> population) {
        return population.stream()
                .min(Comparator.comparingInt(this::calculateFitness))
                .orElse(null);
    }

    private List<Integer> generateRandomPath() {
        List<Integer> path = new ArrayList<>();
        path.add(startNode);
        int currentNode = startNode;
        while (currentNode != endNode && path.size() < graph.length) {
            List<Integer> neighbors = getNeighbors(currentNode);
            if (neighbors.isEmpty()) break;
            int nextNode = neighbors.get(random.nextInt(neighbors.size()));
            path.add(nextNode);
            currentNode = nextNode;
        }
        if (path.get(path.size() - 1) != endNode) {
            path.add(endNode);
        }
        return path;
    }

    private List<Integer> getNeighbors(int node) {
        List<Integer> neighbors = new ArrayList<>();
        for (int i = 0; i < graph.length; i++) {
            if (graph[node][i] > 0) neighbors.add(i);
        }
        return neighbors;
    }

    private boolean isValidPath(List<Integer> path) {
        if (path.get(0) != startNode || path.get(path.size() - 1) != endNode) return false;
        for (int i = 0; i < path.size() - 1; i++) {
            if (graph[path.get(i)][path.get(i + 1)] == 0) return false;
        }
        return true;
    }

    private List<Integer> crossover(List<Integer> parent1, List<Integer> parent2) {
        List<Integer> child = new ArrayList<>();
        int crossoverPoint = random.nextInt(Math.min(parent1.size(), parent2.size()));
        for (int i = 0; i < crossoverPoint; i++) {
            child.add(parent1.get(i));
        }
        for (int i = crossoverPoint; i < parent2.size(); i++) {
            if (!child.contains(parent2.get(i))) {
                child.add(parent2.get(i));
            }
        }
        return child;
    }

    private void mutate(List<Integer> path) {
        if (random.nextDouble() < MUTATION_RATE && path.size() > 2) {
            int index1 = random.nextInt(path.size() - 2) + 1;
            int index2 = random.nextInt(path.size() - 2) + 1;
            Collections.swap(path, index1, index2);
        }
    }

    int calculateFitness(List<Integer> path) {
        int fitness = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            fitness += graph[path.get(i)][path.get(i + 1)];
        }
        return fitness;
    }

    public static void run(int[][] graph) {
        ShortestPathGAParallelForkJoin ga = new ShortestPathGAParallelForkJoin(graph);
        List<Integer> shortestPath = ga.findShortestPath();
        System.out.println("Fitness: " + ga.calculateFitness(shortestPath));
        System.out.println("Shortest path: " + shortestPath);

        if (NUM_NODES <= 20) {
            GraphVisualizer visualizer = new GraphVisualizer(graph);
            visualizer.showGraph(shortestPath);
        }
    }
}
