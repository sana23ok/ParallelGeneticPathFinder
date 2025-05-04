package org.example.parallel;

import org.example.Island;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.example.Constants.POPULATION_SIZE;
import static org.example.Constants.TOURNAMENT_SIZE;

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
        int targetSize = POPULATION_SIZE / Runtime.getRuntime().availableProcessors();

        IntStream.range(0, targetSize).forEach(i -> {
            while (true) {
                List<Integer> path = generateRandomPath();
                if (isValidPath(path)) {
                    population.add(path);
                    break;
                }
            }
        });
    }

    @Override
    public void evolve() {
        List<List<Integer>> nextGeneration = Collections.synchronizedList(new ArrayList<>());
        evaluatePopulation(); // послідовно

        // Еволюція також без паралельності
        IntStream.range(0, population.size()).forEach(i -> {
            List<Integer> parent1 = tournamentSelection();
            List<Integer> parent2 = tournamentSelection();
            List<Integer> child = crossover(parent1, parent2);
            mutate(child);
            if (isValidPath(child)) {
                nextGeneration.add(child);
            } else {
                nextGeneration.add(parent1); // fallback
            }
        });

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

    @Override
    protected void evaluatePopulation() {
        // Виконання оцінки без паралелізації
        population.forEach(this::calculateFitness);
    }

    @Override
    protected List<Integer> tournamentSelection() {
        return IntStream.range(0, TOURNAMENT_SIZE)
                .mapToObj(i -> population.get(random.nextInt(population.size())))
                .min(Comparator.comparingInt(this::calculateFitness))
                .orElse(population.get(0)); // fallback
    }
}
