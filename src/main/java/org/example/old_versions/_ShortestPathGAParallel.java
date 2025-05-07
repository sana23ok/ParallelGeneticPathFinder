package org.example.old_versions;
import org.example.graph.GraphVisualizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.example.Constants.*;

public class _ShortestPathGAParallel {

    private final int[][] graph;
    private final int startNode = 0;
    private final int endNode = NUM_NODES / 2 + 1;
    private final Random random;
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()); // Пул потоків

    public _ShortestPathGAParallel(int[][] graph) {
        this.graph = graph;
        this.random = new Random();
    }

    public List<Integer> findShortestPath() {
        List<List<Integer>> population = initializePopulation();
        for (int i = 0; i < GENERATIONS; i++) {
            population = createNextGeneration(population);
            // System.out.println("Generation " + (i + 1) + ", Best Fitness: " + calculateFitness(getBestPath(population)));
        }
        // Не завершуємо executor тут, оскільки getBestPath його використовує
        return getBestPath(population);
    }

    private List<List<Integer>> initializePopulation() {
        List<List<Integer>> population = new ArrayList<>();
        while (population.size() < POPULATION_SIZE) {
            List<Integer> path = generateRandomPath();
            if (isValidPath(path)) {
                population.add(path);
            }
        }
        return population;
    }

    private List<Integer> generateRandomPath() {
        List<Integer> path = new ArrayList<>();
        path.add(startNode);
        int currentNode = startNode;
        while (currentNode != endNode && path.size() < graph.length) {
            List<Integer> neighbors = getNeighbors(currentNode);
            if (neighbors.isEmpty()) {
                break;
            }
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
            if (graph[node][i] > 0) {
                neighbors.add(i);
            }
        }
        return neighbors;
    }

    private boolean isValidPath(List<Integer> path) {
        if (path.get(0) != startNode || path.get(path.size() - 1) != endNode) {
            return false;
        }
        for (int i = 0; i < path.size() - 1; i++) {
            if (graph[path.get(i)][path.get(i + 1)] == 0) {
                return false;
            }
        }
        return true;
    }

    private List<List<Integer>> createNextGeneration(List<List<Integer>> population) {
        List<List<Integer>> newPopulation = new ArrayList<>(POPULATION_SIZE);
        List<Future<List<Integer>>> futures = new ArrayList<>();

        for (int i = 0; i < POPULATION_SIZE; i++) {
            futures.add(executor.submit(() -> {
                List<Integer> parent1 = tournamentSelection(population);
                List<Integer> parent2 = tournamentSelection(population);
                List<Integer> child = crossover(parent1, parent2);
                mutate(child);
                return isValidPath(child) ? child : population.get(random.nextInt(POPULATION_SIZE));
            }));
        }

        for (Future<List<Integer>> future : futures) {
            try {
                newPopulation.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                // Обробка помилок
            }
        }
        return newPopulation;
    }

    private List<Integer> tournamentSelection(List<List<Integer>> population) {
        List<Integer> bestPath = null;
        int bestFitness = Integer.MAX_VALUE;
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            List<Integer> path = population.get(random.nextInt(POPULATION_SIZE));
            int fitness = calculateFitness(path);
            if (fitness < bestFitness) {
                bestFitness = fitness;
                bestPath = path;
            }
        }
        return bestPath;
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
        if (random.nextDouble() < MUTATION_RATE) {
            int index1 = random.nextInt(path.size() - 1) + 1;
            int index2 = random.nextInt(path.size() - 1) + 1;
            int temp = path.get(index1);
            path.set(index1, path.get(index2));
            path.set(index2, temp);
        }
    }

    int calculateFitness(List<Integer> path) {
        int fitness = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            fitness += graph[path.get(i)][path.get(i + 1)];
        }
        return fitness;
    }

    private List<Integer> getBestPath(List<List<Integer>> population) {
        int bestFitness = Integer.MAX_VALUE;
        List<Integer> bestPath = null;
        List<Future<Integer>> fitnessFutures = new ArrayList<>();
        List<Future<List<Integer>>> pathFutures = new ArrayList<>();

        for (List<Integer> path : population) {
            Callable<Integer> fitnessTask = () -> calculateFitness(path);
            fitnessFutures.add(executor.submit(fitnessTask));
            pathFutures.add(executor.submit(() -> path)); // Просто передаємо шлях для подальшого порівняння
        }

        for (int i = 0; i < population.size(); i++) {
            try {
                int fitness = fitnessFutures.get(i).get();
                List<Integer> path = pathFutures.get(i).get();
                if (fitness < bestFitness) {
                    bestFitness = fitness;
                    bestPath = path;
                }
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                // Обробка помилок
            }
        }
        executor.shutdown(); // Завершуємо executor після використання в getBestPath
        try {
            executor.awaitTermination(Long.MAX_VALUE, java.util.concurrent.TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return bestPath;
    }

    public static void run(int[][] graph) {
        _ShortestPathGAParallel ga = new _ShortestPathGAParallel(graph);
        List<Integer> shortestPath = ga.findShortestPath();

        System.out.println("Fitness: " + ga.calculateFitness(shortestPath));
        System.out.println("Shortest path: " + shortestPath);

        if (NUM_NODES <= 20) {
            GraphVisualizer visualizer = new GraphVisualizer(graph);
            visualizer.showGraph(shortestPath);
        }
    }
}