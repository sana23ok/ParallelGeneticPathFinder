package org.example.executor_version;

import org.example.Island;
import java.util.*;
import static org.example.Constants.POPULATION_SIZE;

public class IslandSequential extends Island {

    public IslandSequential(int[][] graph) {
        super(graph);
        initializePopulation();
    }

    @Override
    protected List<List<Integer>> createPopulationList() {
        return new ArrayList<>();
    }

    @Override
    protected Map<List<Integer>, Integer> createFitnessCacheMap() {
        return new HashMap<>();
    }

    @Override
    protected void initializePopulation() {
        while (population.size() < POPULATION_SIZE) {
            List<Integer> path = generateRandomPath();
            if (isValidPath(path)) {
                population.add(path);
            }
        }
    }

    @Override
    public void evolve() {
        List<List<Integer>> nextGeneration = new ArrayList<>();
        evaluatePopulation();

        for (int i = 0; i < population.size(); i++) {
            List<Integer> parent1 = tournamentSelection();
            List<Integer> parent2 = tournamentSelection();
            List<Integer> child = crossover(parent1, parent2);
            mutate(child);
            if (isValidPath(child)) {
                nextGeneration.add(child);
            } else {
                nextGeneration.add(new ArrayList<>(parent1)); // Додаємо копію батьківського шляху
            }
        }

        population.clear();
        population.addAll(nextGeneration);
    }

    public List<List<Integer>> getBestIndividuals(int count) {
        List<List<Integer>> sortedPopulation = new ArrayList<>(population);
        sortedPopulation.sort(Comparator.comparingInt(this::calculateFitness));
        List<List<Integer>> bestIndividuals = new ArrayList<>();
        for (int i = 0; i < Math.min(count, sortedPopulation.size()); i++) {
            bestIndividuals.add(new ArrayList<>(sortedPopulation.get(i))); // Додаємо копію шляху
        }
        return bestIndividuals;
    }

    public void addMigrants(List<List<Integer>> migrants) {
        for (List<Integer> migrant : migrants) {
            int index = random.nextInt(population.size());
            population.set(index, new ArrayList<>(migrant)); // Додаємо копію шляху
        }
    }
}
