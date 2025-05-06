package org.example;

import java.util.*;
import java.util.concurrent.ForkJoinPool;

public abstract class Island {

    protected final int[][] graph;
    protected final int startNode = 0;
    protected final int endNode = Constants.NUM_NODES / 2 + 1;
    protected final Random random = new Random();
    protected List<List<Integer>> population;
    protected Map<List<Integer>, Integer> fitnessCache;
    protected ForkJoinPool pool;


    public Island(int[][] graph) {
        this.graph = graph;
        this.population = createPopulationList();
        this.fitnessCache = createFitnessCacheMap();
        initializePopulation();
    }


    protected abstract List<List<Integer>> createPopulationList();


    protected abstract Map<List<Integer>, Integer> createFitnessCacheMap();


    protected abstract void initializePopulation();


    public abstract void evolve();


    public List<Integer> getBestPath() {
        return population.stream()
                .min(Comparator.comparingInt(this::calculateFitness))
                .orElse(null);
    }


    protected List<Integer> generateRandomPath() {
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


    protected List<Integer> getNeighbors(int node) {
        List<Integer> neighbors = new ArrayList<>();
        for (int i = 0; i < graph.length; i++) {
            if (graph[node][i] > 0) neighbors.add(i);
        }
        return neighbors;
    }


    protected boolean isValidPath(List<Integer> path) {
        if (path.get(0) != startNode || path.get(path.size() - 1) != endNode) return false;
        for (int i = 0; i < path.size() - 1; i++) {
            if (graph[path.get(i)][path.get(i + 1)] == 0) return false;
        }
        return true;
    }


    protected void evaluatePopulation() {
        population.forEach(this::calculateFitness);
    }


    protected List<Integer> tournamentSelection() {
        List<Integer> best = null;
        int bestFitness = Integer.MAX_VALUE;

        for (int i = 0; i < Constants.TOURNAMENT_SIZE; i++) {
            List<Integer> candidate = population.get(random.nextInt(population.size()));
            int fitness = calculateFitness(candidate);
            if (fitness < bestFitness) {
                best = candidate;
                bestFitness = fitness;
            }
        }
        return best;
    }


    protected List<Integer> crossover(List<Integer> parent1, List<Integer> parent2) {
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


    protected void mutate(List<Integer> path) {
        if (random.nextDouble() < Constants.MUTATION_RATE && path.size() > 2) {
            int index1 = 1 + random.nextInt(path.size() - 2);
            int index2 = 1 + random.nextInt(path.size() - 2);
            Collections.swap(path, index1, index2);
        }
    }


    protected int calculateFitness(List<Integer> path) {
        return fitnessCache.computeIfAbsent(path, p -> {
            int fitness = 0;
            for (int i = 0; i < p.size() - 1; i++) {
                fitness += graph[p.get(i)][p.get(i + 1)];
            }
            return fitness;
        });
    }
}
