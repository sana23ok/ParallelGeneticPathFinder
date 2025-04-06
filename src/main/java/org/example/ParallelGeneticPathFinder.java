package org.example;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class ParallelGeneticPathFinder {

    static final int NUM_VERTICES = 2000;
    static final int POPULATION_SIZE = 100;
    static final int GENERATIONS = 100;
    static final double MUTATION_RATE = 0.05;
    static final int TOURNAMENT_SIZE = 5;
    static final double ELITISM_PERCENTAGE = 0.1;
    static final int MAX_STAGNATION = 100;
    static final double CHANCE_EDGE_EXIXTS = 0.2;

    static int[][] graph = new int[NUM_VERTICES][NUM_VERTICES];
    static Random rand = new Random();
    static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public static void main(String[] args) throws IOException {
        //generateGraphInput("graph.txt");
        loadGraph("graph.txt");

        int start = 0, end = NUM_VERTICES - 1;

        // Розпаралелена ініціалізація популяції
        List<List<Integer>> population = initializePopulationParallel(start, end);

        // Розпаралелений пошук найкращої особини
        population.sort(Comparator.comparingInt(ParallelGeneticPathFinder::fitness));

        int bestCost = Integer.MAX_VALUE;
        int stagnationCounter = 0;
        List<Integer> bestPath = population.get(0);

        for (int gen = 0; gen < GENERATIONS; gen++) {
            List<List<Integer>> newPopulation = new ArrayList<>();
            int elitismSize = (int) (ELITISM_PERCENTAGE * POPULATION_SIZE);
            newPopulation.addAll(population.subList(0, elitismSize));

            while (newPopulation.size() < POPULATION_SIZE) {
                List<Integer> parent1 = tournamentSelectionParallel(population);
                List<Integer> parent2 = tournamentSelectionParallel(population);
                List<Integer> child = crossover(parent1, parent2, start, end);
                if (rand.nextDouble() < MUTATION_RATE) {
                    mutate(child, start, end);
                }
                newPopulation.add(child);
            }

            population = newPopulation;
            population.sort(Comparator.comparingInt(ParallelGeneticPathFinder::fitness));

            int currentBestCost = fitness(population.get(0));
            if (currentBestCost < bestCost) {
                bestCost = currentBestCost;
                bestPath = population.get(0);
                stagnationCounter = 0;
                System.out.println("Generation " + gen + " best cost " + bestCost);
            } else {
                stagnationCounter++;
            }

//            if (stagnationCounter >= MAX_STAGNATION) {
//                System.out.println("Terminating due to stagnation at generation " + gen);
//                break;
//            }
        }

        System.out.println("Best path: " + bestPath + " | Cost: " + bestCost);

        // Закриваємо ExecutorService після виконання
        executorService.shutdown();
    }

    // --------- STEP 1: Generate Graph and Save to File -----------
    static void generateGraphInput(String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        for (int i = 0; i < NUM_VERTICES; i++) {
            for (int j = 0; j < NUM_VERTICES; j++) {
                if (i != j && rand.nextDouble() < CHANCE_EDGE_EXIXTS) { // 70% chance edge exists
                    int weight = rand.nextInt(5) + 1;
                    writer.write(i + " " + j + " " + weight + "\n");
                }
            }
        }
        writer.close();
    }

    static void loadGraph(String filename) throws IOException {
        for (int[] row : graph)
            Arrays.fill(row, 0);
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

    static List<List<Integer>> initializePopulationParallel(int start, int end) {
        List<Callable<List<Integer>>> tasks = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            tasks.add(() -> {
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
                return path;
            });
        }

        try {
            List<Future<List<Integer>>> futures = executorService.invokeAll(tasks);
            List<List<Integer>> population = new ArrayList<>();
            for (Future<List<Integer>> future : futures) {
                population.add(future.get());
            }
            return population;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    static List<Integer> tournamentSelectionParallel(List<List<Integer>> population) {
        List<List<Integer>> tournament = new ArrayList<>();
        List<Callable<List<Integer>>> tasks = new ArrayList<>();
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            tasks.add(() -> population.get(rand.nextInt(POPULATION_SIZE)));
        }
        try {
            List<Future<List<Integer>>> futures = executorService.invokeAll(tasks);
            for (Future<List<Integer>> future : futures) {
                tournament.add(future.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return Collections.min(tournament, Comparator.comparingInt(ParallelGeneticPathFinder::fitness));
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

    static List<Integer> crossover(List<Integer> p1, List<Integer> p2, int start, int end) {
        List<Integer> child = new ArrayList<>();
        int crossoverPoint = rand.nextInt(Math.min(p1.size(), p2.size()) - 1) + 1;
        child.addAll(p1.subList(0, crossoverPoint));
        for (int node : p2) {
            if (!child.contains(node) && node != start) {
                child.add(node);
            }
            if (node == end) break;
        }
        if (!child.contains(end)) child.add(end);
        return child;
    }

    static void mutate(List<Integer> path, int start, int end) {
        if (path.size() > 2) {
            int i = rand.nextInt(path.size() - 2) + 1;
            int j = rand.nextInt(path.size() - 2) + 1;
            Collections.swap(path, i, j);
        }
    }
}
