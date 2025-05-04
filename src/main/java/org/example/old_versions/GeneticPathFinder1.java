package org.example.old_versions;

import org.example.graph.GraphVisualizer;

import java.io.*;
import java.util.*;

public class GeneticPathFinder1 {

    static final int NUM_VERTICES = 5;
    static final int POPULATION_SIZE = 100;
    static final int GENERATIONS = 1000;
    static final double MUTATION_RATE = 0.05;

    static int[][] graph = new int[NUM_VERTICES][NUM_VERTICES];
    static Random rand = new Random();

    public static void main(String[] args) throws IOException {
        generateGraphInput("graph.txt");
        loadGraph("graph.txt");

        int start = 0, end = NUM_VERTICES - 1;
        List<List<Integer>> population = initializePopulation(start, end);
        List<Integer> bestPath = null;
        int bestFitness = Integer.MAX_VALUE;

        for (int gen = 0; gen < GENERATIONS; gen++) {
            population.sort(Comparator.comparingInt(GeneticPathFinder1::fitness));

            if (fitness(population.get(0)) < bestFitness) {
                bestPath = population.get(0);
                bestFitness = fitness(bestPath);
            }

            List<List<Integer>> newPopulation = new ArrayList<>(population.subList(0, 10)); // elitism

            while (newPopulation.size() < POPULATION_SIZE) {
                List<Integer> parent1 = select(population);
                List<Integer> parent2 = select(population);
                List<Integer> child = crossover(parent1, parent2, start, end);

                if (rand.nextDouble() < MUTATION_RATE) {
                    mutate(child, start, end);
                }

                newPopulation.add(child);
            }

            population = newPopulation;
        }

        System.out.println("Best path: " + bestPath + " | Cost: " + bestFitness);
        System.out.println("Route details:");

        for (int i = 0; i < bestPath.size() - 1; i++) {
            int from = bestPath.get(i);
            int to = bestPath.get(i + 1);
            int weight = graph[from][to];
            System.out.println(from + " -> " + to + " (weight: " + weight + ")");
        }

        GraphVisualizer visualizer = new GraphVisualizer(graph);
        visualizer.showGraph(bestPath);
    }

    // --------- STEP 1: Generate Graph and Save to File -----------
    static void generateGraphInput(String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        for (int i = 0; i < NUM_VERTICES; i++) {
            for (int j = 0; j < NUM_VERTICES; j++) {
                if (i != j && rand.nextDouble() < 0.7) { // 70% chance edge exists
                    int weight = rand.nextInt(5) + 1;
                    writer.write(i + " " + j + " " + weight + "\n");
                }
            }
        }
        writer.close();
    }

    // --------- STEP 2: Load Graph from File ----------------------
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

    // --------- STEP 3: Initial Population ------------------------
    static List<List<Integer>> initializePopulation(int start, int end) {
        List<List<Integer>> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            List<Integer> path = new ArrayList<>();
            path.add(start);

            List<Integer> middle = new ArrayList<>();
            for (int j = 0; j < NUM_VERTICES; j++) {
                if (j != start && j != end) middle.add(j);
            }

            Collections.shuffle(middle);
            path.addAll(middle);
            path.add(end);

            population.add(path);
        }
        return population;
    }

    // --------- STEP 4: Fitness Function --------------------------
    static int fitness(List<Integer> path) {
        int cost = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            int u = path.get(i);
            int v = path.get(i + 1);
            if (graph[u][v] == 0) return Integer.MAX_VALUE; // No path
            cost += graph[u][v];
        }
        return cost;
    }

    // --------- STEP 5: Selection (Tournament) --------------------
    static List<Integer> select(List<List<Integer>> population) {
        List<Integer> a = population.get(rand.nextInt(POPULATION_SIZE));
        List<Integer> b = population.get(rand.nextInt(POPULATION_SIZE));
        return fitness(a) < fitness(b) ? a : b;
    }

    // --------- STEP 6: Crossover ---------------------------------
    static List<Integer> crossover(List<Integer> p1, List<Integer> p2, int start, int end) {
        Set<Integer> seen = new HashSet<>();
        List<Integer> child = new ArrayList<>();
        child.add(start);

        // Use the first parent for the first part
        for (int i = 1; i < p1.size() - 1; i++) {
            int node = (i % 2 == 0) ? p1.get(i) : p2.get(i);
            if (node != start && node != end && seen.add(node)) {
                child.add(node);
            }
        }

        // Fill the rest of the nodes from the second parent
        for (int i = 0; i < NUM_VERTICES; i++) {
            if (i != start && i != end && !seen.contains(i)) {
                child.add(i);
            }
        }

        child.add(end);
        return child;
    }

    // --------- STEP 7: Mutation ----------------------------------
    static void mutate(List<Integer> path, int start, int end) {
        int i = rand.nextInt(path.size() - 2) + 1;
        int j = rand.nextInt(path.size() - 2) + 1;
        Collections.swap(path, i, j);
    }
}
