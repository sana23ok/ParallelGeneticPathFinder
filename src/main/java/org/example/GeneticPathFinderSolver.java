package org.example;

import java.io.*;
import java.util.*;

public class GeneticPathFinderSolver {
    private final String graphFile;
    private final int numVertices;
    private final int populationSize;
    private final int generations;
    private final double initialMutationRate;
    private final int tournamentSize;
    private final double elitismPercentage;
    private final int stagnationLimit;

    private final int[][] graph;
    private final List<Integer> bestFitnessHistory = new ArrayList<>();
    private static final Random rand = new Random();

    public GeneticPathFinderSolver(String graphFile, int numVertices, int populationSize, int generations,
                                   double initialMutationRate, int tournamentSize, double elitismPercentage,
                                   int stagnationLimit) {
        this.graphFile = graphFile;
        this.numVertices = numVertices;
        this.populationSize = populationSize;
        this.generations = generations;
        this.initialMutationRate = initialMutationRate;
        this.tournamentSize = tournamentSize;
        this.elitismPercentage = elitismPercentage;
        this.stagnationLimit = stagnationLimit;
        this.graph = new int[numVertices][numVertices];
    }

    public void run(int start, int end) {
        //this.generateGraphInput(graphFile);
        loadGraph();
        List<List<Integer>> population = initializePopulation(start, end);
        population.sort(Comparator.comparingInt(this::fitness));

        List<Integer> bestPath = population.get(0);
        int bestCost = fitness(bestPath);
        int stagnationCounter = 0;
        double mutationRate = initialMutationRate;

        for (int gen = 0; gen < generations; gen++) {
            List<List<Integer>> newGen = new ArrayList<>();
            int eliteCount = (int) (elitismPercentage * populationSize);
            newGen.addAll(population.subList(0, eliteCount));

            while (newGen.size() < populationSize) {
                List<Integer> parent1 = tournamentSelection(population);
                List<Integer> parent2 = tournamentSelection(population);
                List<Integer> child = crossover(parent1, parent2, start, end);
                if (rand.nextDouble() < mutationRate) {
                    mutate(child);
                }
                newGen.add(child);
            }

            population = newGen;
            population.sort(Comparator.comparingInt(this::fitness));

            int currentBest = fitness(population.get(0));
            bestFitnessHistory.add(currentBest);

            if (currentBest < bestCost) {
                bestCost = currentBest;
                bestPath = population.get(0);
                stagnationCounter = 0;
                mutationRate = initialMutationRate;
            } else {
                stagnationCounter++;
                mutationRate = Math.min(0.5, mutationRate * 1.05);
            }

            System.out.printf("Generation %d: Best Cost = %d\n", gen, currentBest);

            if (stagnationCounter >= stagnationLimit) {
                System.out.println("Stopped early due to stagnation.");
                break;
            }
        }

        System.out.println("Best path found: " + bestPath);
        System.out.println("Cost: " + bestCost);
        System.out.println("Fitness evolution: " + bestFitnessHistory);
    }

    private void generateGraphInput(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (int i = 0; i < numVertices; i++) {
                for (int j = 0; j < numVertices; j++) {
                    if (i != j && rand.nextDouble() < 0.2) {
                        int weight = rand.nextInt(5) + 1;
                        writer.write(i + " " + j + " " + weight + "\n");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error generating graph: " + e.getMessage());
        }
    }

    private void loadGraph() {
        try (BufferedReader br = new BufferedReader(new FileReader(graphFile))) {
            for (int[] row : graph) Arrays.fill(row, 0);
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                int u = Integer.parseInt(parts[0]);
                int v = Integer.parseInt(parts[1]);
                int w = Integer.parseInt(parts[2]);
                graph[u][v] = w;
            }
        } catch (IOException e) {
            System.err.println("Error loading graph: " + e.getMessage());
        }
    }

    private List<List<Integer>> initializePopulation(int start, int end) {
        List<List<Integer>> pop = new ArrayList<>();
        while (pop.size() < populationSize) {
            List<Integer> path = generateRandomPath(start, end);
            if (!pop.contains(path)) pop.add(path);
        }
        return pop;
    }

    private List<Integer> generateRandomPath(int start, int end) {
        List<Integer> path = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        path.add(start);
        visited.add(start);

        while (path.size() < numVertices) {
            int last = path.get(path.size() - 1);
            List<Integer> neighbors = new ArrayList<>();
            for (int j = 0; j < numVertices; j++) {
                if (graph[last][j] > 0 && !visited.contains(j)) {
                    neighbors.add(j);
                }
            }
            if (neighbors.isEmpty()) break;
            int next = neighbors.get(rand.nextInt(neighbors.size()));
            path.add(next);
            visited.add(next);
            if (next == end) break;
        }
        if (!path.contains(end)) path.add(end);
        return path;
    }

    private int fitness(List<Integer> path) {
        int cost = 0;
        Set<Integer> visited = new HashSet<>();
        for (int i = 0; i < path.size() - 1; i++) {
            int u = path.get(i), v = path.get(i + 1);
            if (graph[u][v] == 0 || visited.contains(v)) return Integer.MAX_VALUE;
            cost += graph[u][v];
            visited.add(u);
        }
        return cost;
    }

    private List<Integer> tournamentSelection(List<List<Integer>> population) {
        List<List<Integer>> candidates = new ArrayList<>();
        for (int i = 0; i < tournamentSize; i++) {
            candidates.add(population.get(rand.nextInt(populationSize)));
        }
        return Collections.min(candidates, Comparator.comparingInt(this::fitness));
    }

    private List<Integer> crossover(List<Integer> p1, List<Integer> p2, int start, int end) {
        Set<Integer> used = new HashSet<>();
        List<Integer> child = new ArrayList<>();
        child.add(start);
        used.add(start);

        for (int i = 1; i < p1.size() && p1.get(i) != end; i++) {
            int node = p1.get(i);
            if (!used.contains(node)) {
                child.add(node);
                used.add(node);
            }
        }

        for (int node : p2) {
            if (!used.contains(node) && node != start) {
                child.add(node);
                used.add(node);
            }
            if (node == end) break;
        }

        if (!child.contains(end)) child.add(end);
        return child;
    }

    private void mutate(List<Integer> path) {
        if (path.size() > 3) {
            int i = rand.nextInt(path.size() - 2) + 1;
            int j = rand.nextInt(path.size() - 2) + 1;
            if (i > j) {
                int tmp = i;
                i = j;
                j = tmp;
            }
            Collections.reverse(path.subList(i, j));
        }
    }


    public static void main(String[] args) {
        // Параметри, які можна конфігурувати
        String graphFile = "graph.txt";
        int numVertices = 200;
        int populationSize = 100;
        int generations = 100;
        double initialMutationRate = 0.1;
        int tournamentSize = 5;
        double elitismPercentage = 0.1;
        int stagnationLimit = 100;

        int startVertex = 0;
        int endVertex = numVertices - 1;

        GeneticPathFinderSolver solver = new GeneticPathFinderSolver(
                graphFile,
                numVertices,
                populationSize,
                generations,
                initialMutationRate,
                tournamentSize,
                elitismPercentage,
                stagnationLimit
        );

        solver.run(startVertex, endVertex);
    }

}

