package org.example;

import org.example.graph.GraphVisualizer;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;
import static org.example.Constants.*;

public class ShortestPathGAParallel1 {

    private final int[][] graph;
    private final int startNode = 0;
    private final int endNode = NUM_NODES / 2 + 1;
    private final Random random = new Random();
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final Map<List<Integer>, Integer> fitnessCache = new ConcurrentHashMap<>();

    public ShortestPathGAParallel1(int[][] graph) {
        this.graph = graph;
    }

    public List<Integer> findShortestPath() {
        List<List<Integer>> population = initializePopulation();
        for (int i = 0; i < GENERATIONS; i++) {
            Map<List<Integer>, Integer> evaluated = evaluatePopulation(population);
            population = createNextGeneration(population);
            List<Integer> best = evaluated.entrySet().parallelStream()
                    .min(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
            int fitness = evaluated.get(best);
            //System.out.println("Generation " + (i + 1) + ", Best Fitness: " + fitness);
        }
        executor.shutdown();
        return getBestPath(population);
    }

    private List<List<Integer>> initializePopulation() {
        return IntStream.range(0, POPULATION_SIZE).parallel()
                .mapToObj(i -> {
                    List<Integer> path;
                    do {
                        path = generateRandomPath();
                    } while (!isValidPath(path));
                    return path;
                }).collect(Collectors.toList());
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

    private Map<List<Integer>, Integer> evaluatePopulation(List<List<Integer>> population) {
        return population.parallelStream()
                .collect(Collectors.toConcurrentMap(
                        path -> path,
                        this::calculateFitness,
                        (v1, v2) -> v1  // залишаємо перше зіткнення
                ));
    }

    private List<List<Integer>> createNextGeneration(List<List<Integer>> population) {
        return IntStream.range(0, POPULATION_SIZE).parallel()
                .mapToObj(i -> {
                    List<Integer> parent1 = tournamentSelection(population);
                    List<Integer> parent2 = tournamentSelection(population);
                    List<Integer> child = crossover(parent1, parent2);
                    mutate(child);
                    return isValidPath(child) ? child : population.get(random.nextInt(POPULATION_SIZE));
                })
                .collect(Collectors.toList());
    }

    private List<Integer> tournamentSelection(List<List<Integer>> population) {
        return IntStream.range(0, TOURNAMENT_SIZE)
                .mapToObj(i -> population.get(random.nextInt(POPULATION_SIZE)))
                .min(Comparator.comparingInt(this::calculateFitness))
                .orElse(null);
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

    private List<Integer> getBestPath(List<List<Integer>> population) {
        return population.parallelStream()
                .min(Comparator.comparingInt(this::calculateFitness))
                .orElse(null);
    }

    public static void run(int[][] graph) {
        ShortestPathGAParallel1 ga = new ShortestPathGAParallel1(graph);
        List<Integer> shortestPath = ga.findShortestPath();

        System.out.println("Fitness: " + ga.calculateFitness(shortestPath));
        System.out.println("Shortest path: " + shortestPath);

        if (NUM_NODES <= 20) {
            GraphVisualizer visualizer = new GraphVisualizer(graph);
            visualizer.showGraph(shortestPath);
        }
    }
}

