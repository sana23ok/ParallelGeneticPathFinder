package org.example.sequential;

import org.example.Island;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.example.Constants.POPULATION_SIZE;

public class IslandSequential extends Island {

    public IslandSequential(int[][] graph) {
        super(graph);
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
                nextGeneration.add(parent1);
            }
        }

        population.clear();
        population.addAll(nextGeneration);
    }
}