package org.example.parallel.v1;

import org.example.Island;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.example.Constants.*;

public class IslandParallel extends Island {

    public IslandParallel(int[][] graph) {
        super(graph);
    }

    @Override
    protected List<List<Integer>> createPopulationList() {
        return new CopyOnWriteArrayList<>();
    }

    @Override
    protected Map<List<Integer>, Integer> createFitnessCacheMap() {
        return new ConcurrentHashMap<>();
    }

    @Override
    protected void initializePopulation() {
        List<List<Integer>> localPopulation = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            while (true) {
                List<Integer> path = generateRandomPath();
                if (isValidPath(path)) {
                    localPopulation.add(path);
                    break;
                }
            }
        }
        population.addAll(localPopulation);
    }

    @Override
    public void evolve() {
        List<List<Integer>> nextGeneration = new ArrayList<>();
        evaluatePopulationSequential(); // Використовуємо послідовну оцінку тут

        for (int i = 0; i < population.size(); i++) {
            List<Integer> parent1 = tournamentSelectionSequential(); // Послідовний вибір
            List<Integer> parent2 = tournamentSelectionSequential(); // Послідовний вибір
            List<Integer> child = crossover(parent1, parent2);
            mutate(child);
            if (isValidPath(child)) {
                nextGeneration.add(child);
            } else {
                nextGeneration.add(parent1);
            }
        }

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

    protected void evaluatePopulationSequential() {
        for (List<Integer> individual : population) {
            calculateFitness(individual);
        }
    }

    protected List<Integer> tournamentSelectionSequential() {
        List<Integer> best = null;
        int bestFitness = Integer.MAX_VALUE;

        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            List<Integer> candidate = population.get(random.nextInt(population.size()));
            int fitness = calculateFitness(candidate);
            if (fitness < bestFitness) {
                best = candidate;
                bestFitness = fitness;
            }
        }
        return best;
    }

    @Override
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