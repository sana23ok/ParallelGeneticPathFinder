package org.example.old_versions;

import java.io.*;
import java.util.*;
//        long startTimeSeq = System.nanoTime();
//        // Виконання для звичайної версії
//        GeneticPathFinder.main(args);
//        long endTimeSeq = System.nanoTime();
//        long durationSeq = (endTimeSeq - startTimeSeq) / 1_000_000;
//        System.out.println("Execution Time (Sequential): " + durationSeq + " ms");
//
//        long startTimeParallel = System.nanoTime();
//        // Виконання для розпаралеленої версії
//        ParallelGeneticPathFinder.main(args);
//        long endTimeParallel = System.nanoTime();
//        long durationParallel = (endTimeParallel - startTimeParallel) / 1_000_000;
//        System.out.println("Execution Time (Parallel): " + durationParallel + " ms");
//
//        // Обчислення прискорення
//        double speedup = (double) durationSeq / durationParallel;
//        System.out.println("Speedup: " + speedup);
public class GeneticPathFinder {

    static final int NUM_VERTICES = 2000;
    static final int POPULATION_SIZE = 100;
    static final int GENERATIONS = 100;
    static final double INITIAL_MUTATION_RATE = 0.1;
    static final int TOURNAMENT_SIZE = 5;
    static final double ELITISM_PERCENTAGE = 0.1;
    static final int MAX_STAGNATION = 100;
    static final double CHANCE_EDGE_EXISTS = 0.2;

    static int[][] graph = new int[NUM_VERTICES][NUM_VERTICES];
    static Random rand = new Random();

    public static void main(String[] args) throws IOException {
        //generateGraphInput("graph.txt");
        loadGraph("graph.txt");

        int start = 0, end = NUM_VERTICES - 1;
        List<List<Integer>> population = initializePopulation(start, end);
        population.sort(Comparator.comparingInt(GeneticPathFinder::fitness));

        int bestCost = Integer.MAX_VALUE;
        int stagnationCounter = 0;
        List<Integer> bestPath = population.get(0);
        double mutationRate = INITIAL_MUTATION_RATE;

        for (int gen = 0; gen < GENERATIONS; gen++) {
            List<List<Integer>> newPopulation = new ArrayList<>();
            int elitismSize = (int) (ELITISM_PERCENTAGE * POPULATION_SIZE);
            newPopulation.addAll(population.subList(0, elitismSize));

            while (newPopulation.size() < POPULATION_SIZE) {
                List<Integer> parent1 = tournamentSelection(population);
                List<Integer> parent2 = tournamentSelection(population);
                List<Integer> child = crossover(parent1, parent2, start, end);
                if (rand.nextDouble() < mutationRate) {
                    mutate(child, start, end);
                }
                newPopulation.add(child);
            }

            population = newPopulation;
            population.sort(Comparator.comparingInt(GeneticPathFinder::fitness));

            int currentBestCost = fitness(population.get(0));
            System.out.println("Generation " + gen + " best cost: " + currentBestCost);

            if (currentBestCost < bestCost) {
                bestCost = currentBestCost;
                bestPath = population.get(0);
                stagnationCounter = 0;
                mutationRate = INITIAL_MUTATION_RATE; // скидаємо мутацію
            } else {
                stagnationCounter++;
                mutationRate = Math.min(0.5, mutationRate * 1.05); // поступово підвищуємо
            }

            if (stagnationCounter >= MAX_STAGNATION) {
                System.out.println("Terminating due to stagnation at generation " + gen);
                break;
            }
        }

        System.out.println("Best path: " + bestPath + " | Cost: " + bestCost);
    }

    static void generateGraphInput(String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        for (int i = 0; i < NUM_VERTICES; i++) {
            for (int j = 0; j < NUM_VERTICES; j++) {
                if (i != j && rand.nextDouble() < CHANCE_EDGE_EXISTS) {
                    int weight = rand.nextInt(5) + 1;
                    writer.write(i + " " + j + " " + weight + "\n");
                }
            }
        }
        writer.close();
    }

    static void loadGraph(String filename) throws IOException {
        for (int[] row : graph) Arrays.fill(row, 0);
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(" ");
            int u = Integer.parseInt(parts[0]);
            int v = Integer.parseInt(parts[1]);
            int w = Integer.parseInt(parts[2]);
            graph[u][v] = w;
        }
        reader.close();
    }

    static List<List<Integer>> initializePopulation(int start, int end) {
        List<List<Integer>> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            List<Integer> path = new ArrayList<>();
            path.add(start);
            Set<Integer> visited = new HashSet<>();
            visited.add(start);
            while (path.size() < NUM_VERTICES) {
                int last = path.get(path.size() - 1);
                List<Integer> neighbors = new ArrayList<>();
                for (int j = 0; j < NUM_VERTICES; j++) {
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
            population.add(path);
        }
        return population;
    }

    static int fitness(List<Integer> path) {
        int cost = 0;
        Set<Integer> visited = new HashSet<>();
        for (int i = 0; i < path.size() - 1; i++) {
            int u = path.get(i);
            int v = path.get(i + 1);
            if (graph[u][v] == 0 || visited.contains(v)) return Integer.MAX_VALUE;
            cost += graph[u][v];
            visited.add(u);
        }
        return cost;
    }

    static List<Integer> tournamentSelection(List<List<Integer>> population) {
        List<List<Integer>> tournament = new ArrayList<>();
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            tournament.add(population.get(rand.nextInt(POPULATION_SIZE)));
        }
        return Collections.min(tournament, Comparator.comparingInt(GeneticPathFinder::fitness));
    }

    static List<Integer> crossover(List<Integer> p1, List<Integer> p2, int start, int end) {
        Set<Integer> used = new HashSet<>();
        List<Integer> child = new ArrayList<>();
        child.add(start);
        used.add(start);

        int i = 1;
        while (i < p1.size() && p1.get(i) != end) {
            int candidate = p1.get(i);
            if (!used.contains(candidate)) {
                child.add(candidate);
                used.add(candidate);
            }
            i++;
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

    static void mutate(List<Integer> path, int start, int end) {
        if (path.size() > 3) {
            int i = rand.nextInt(path.size() - 2) + 1;
            int j = rand.nextInt(path.size() - 2) + 1;
            if (i > j) {
                int temp = i;
                i = j;
                j = temp;
            }
            Collections.reverse(path.subList(i, j));
        }
    }
}
