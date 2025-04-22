package org.example.island;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.example.Constants.*;

class Island {
    private final int[][] graph;
    private final int startNode = 0;
    private final int endNode = NUM_NODES / 2 + 1;
    private final Random random = new Random();
    private final List<List<Integer>> population;
    private final ConcurrentMap<List<Integer>, Integer> fitnessCache = new ConcurrentHashMap<>();

    public Island(int[][] graph) {
        this.graph = graph;
        this.population = new CopyOnWriteArrayList<>();
        initializePopulationParallel();
    }

    // Паралельна ініціалізація популяції
    private void initializePopulationParallel() {
        int targetSize = POPULATION_SIZE / Runtime.getRuntime().availableProcessors();

        ForkJoinPool.commonPool().submit(() -> IntStream.range(0, targetSize).parallel().forEach(i -> {
            while (true) {
                List<Integer> path = generateRandomPath();
                if (isValidPath(path)) {
                    population.add(path);
                    break;
                }
            }
        })).join();
    }

    public void evolve() {
        List<List<Integer>> nextGeneration = Collections.synchronizedList(new ArrayList<>());
        evaluatePopulation(); // Оновлює кеш

        ForkJoinPool.commonPool().submit(() ->
                IntStream.range(0, population.size()).parallel().forEach(i -> {
                    List<Integer> parent1 = tournamentSelection();
                    List<Integer> parent2 = tournamentSelection();
                    List<Integer> child = crossover(parent1, parent2);
                    mutate(child);
                    if (isValidPath(child)) {
                        nextGeneration.add(child);
                    } else {
                        nextGeneration.add(parent1);
                    }
                })
        ).join();

        population.clear();
        population.addAll(nextGeneration);
    }

    public List<List<Integer>> getBestIndividuals(int count) {
        return population.stream()
                .sorted(Comparator.comparingInt(this::calculateFitness))
                .limit(count)
                .collect(Collectors.toList());
    }

    public void addMigrants(List<List<Integer>> migrants) {
        for (List<Integer> migrant : migrants) {
            int index = random.nextInt(population.size());
            population.set(index, migrant);
        }
    }

    public List<Integer> getBestPath() {
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

        if (path.get(path.size() - 1) != endNode) path.add(endNode);
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

    private void evaluatePopulation() {
        population.parallelStream().forEach(this::calculateFitness);
    }

    private List<Integer> tournamentSelection() {
        return IntStream.range(0, TOURNAMENT_SIZE)
                .mapToObj(i -> population.get(random.nextInt(population.size())))
                .min(Comparator.comparingInt(this::calculateFitness))
                .orElse(population.get(0)); // fallback
    }

    private List<Integer> crossover(List<Integer> parent1, List<Integer> parent2) {
        Set<Integer> visited = new HashSet<>();
        List<Integer> child = new ArrayList<>();
        int crossoverPoint = random.nextInt(Math.min(parent1.size(), parent2.size()));

        for (int i = 0; i < crossoverPoint; i++) {
            child.add(parent1.get(i));
            visited.add(parent1.get(i));
        }

        for (int i = crossoverPoint; i < parent2.size(); i++) {
            if (visited.add(parent2.get(i))) {
                child.add(parent2.get(i));
            }
        }
        return child;
    }

    private void mutate(List<Integer> path) {
        if (random.nextDouble() < MUTATION_RATE && path.size() > 2) {
            int index1 = 1 + random.nextInt(path.size() - 2);
            int index2 = 1 + random.nextInt(path.size() - 2);
            Collections.swap(path, index1, index2);
        }
    }

    private int calculateFitness(List<Integer> path) {
        return fitnessCache.computeIfAbsent(path, p -> {
            int fitness = 0;
            for (int i = 0; i < p.size() - 1; i++) {
                fitness += graph[p.get(i)][p.get(i + 1)];
            }
            return fitness;
        });
    }
}

