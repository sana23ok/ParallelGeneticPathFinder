package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static org.example.Constants.*;

public class ShortestPathGA {

    private final int[][] graph; // Матриця суміжності графа
    private final int startNode = 0; // Початкова вершина
    private final int endNode = NUM_NODES/2 + 1; // Кінцева вершина
    private final Random random;

    public ShortestPathGA(int[][] graph) {
        this.graph = graph;
        this.random = new Random();
    }

    public List<Integer> findShortestPath() {
        List<List<Integer>> population = initializePopulation();
        for (int i = 0; i < GENERATIONS; i++) {
            population = createNextGeneration(population);
            // Додано виведення ваги найкращого шляху на кожному поколінні
            //System.out.println("Generation " + (i + 1) + ", Best Fitness: " + calculateFitness(getBestPath(population)));
        }
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
        List<List<Integer>> newPopulation = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            List<Integer> parent1 = tournamentSelection(population);
            List<Integer> parent2 = tournamentSelection(population);
            List<Integer> child = crossover(parent1, parent2);
            mutate(child);
            if (isValidPath(child)) {
                newPopulation.add(child);
            } else {
                newPopulation.add(population.get(random.nextInt(POPULATION_SIZE)));
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

    private int calculateFitness(List<Integer> path) {
        int fitness = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            fitness += graph[path.get(i)][path.get(i + 1)];
        }
        return fitness;
    }

    private List<Integer> getBestPath(List<List<Integer>> population) {
        List<Integer> bestPath = null;
        int bestFitness = Integer.MAX_VALUE;
        for (List<Integer> path : population) {
            int fitness = calculateFitness(path);
            if (fitness < bestFitness) {
                bestFitness = fitness;
                bestPath = path;
            }
        }
        return bestPath;
    }


    public static void run(int[][] graph) {
//        String filename = "graph.txt";
//            generateGraphInput(filename);
//            int[][] graph = new int[NUM_NODES][NUM_NODES];
//            loadGraph(filename, graph);
        ShortestPathGA ga = new ShortestPathGA(graph);
        List<Integer> shortestPath = ga.findShortestPath();

        System.out.println("Shortest path: " + shortestPath);
        System.out.println("Fitness: " + ga.calculateFitness(shortestPath));

        if(NUM_NODES <= 20){
            GraphVisualizer visualizer = new GraphVisualizer(graph);
            visualizer.showGraph(shortestPath);
        }

    }
}