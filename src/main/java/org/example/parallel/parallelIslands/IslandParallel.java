package org.example.parallel.parallelIslands;

import org.example.Constants;
import org.example.Island;
import java.util.*;
import java.util.concurrent.*;

public class IslandParallel extends Island {

    public IslandParallel(int[][] graph) {
        super(graph);
        // Використовуйте змінну для кількості потоків
        int numberOfThreads = 2;  // Тестуйте на різних значеннях
        this.pool = new ForkJoinPool(numberOfThreads);

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
        while (population.size() < Constants.POPULATION_SIZE) {
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

        // Паралельно еволюціонуємо популяцію
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < population.size(); i++) {
            final int index = i;
            tasks.add(() -> {
                List<Integer> parent1 = tournamentSelection();
                List<Integer> parent2 = tournamentSelection();
                List<Integer> child = crossover(parent1, parent2);
                mutate(child);
                if (isValidPath(child)) {
                    synchronized (population) {
                        nextGeneration.add(child);
                    }
                } else {
                    synchronized (population) {
                        nextGeneration.add(parent1);
                    }
                }
                return null;
            });
        }

        try {
            pool.invokeAll(tasks); // виконуємо всі завдання паралельно
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        population.clear();
        population.addAll(nextGeneration);
    }

    public List<List<Integer>> getBestIndividuals(int count) {
        return population.stream()
                .sorted(Comparator.comparingInt(this::calculateFitness))
                .limit(count)
                .toList();
    }

    public void addMigrants(List<List<Integer>> migrants) {
        for (List<Integer> migrant : migrants) {
            int index = random.nextInt(population.size());
            population.set(index, migrant);
        }
    }
}
