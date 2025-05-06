package org.example.parallel.paralellOperations;

import org.example.Constants;
import org.example.Island;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class IslandParallel extends Island {

    public IslandParallel(int[][] graph) {
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
        int NUM_THREADS = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS); // Пул потоків для паралелізму

        List<Future<Void>> futures = new ArrayList<>();
        for (int i = 0; i < population.size(); i++) {
            final int index = i;
            futures.add(executorService.submit(() -> {
                List<Integer> parent1 = tournamentSelection();
                List<Integer> parent2 = tournamentSelection();
                List<Integer> child = crossover(parent1, parent2);
                mutate(child);
                if (isValidPath(child)) {
                    synchronized (nextGeneration) {
                        nextGeneration.add(child);
                    }
                } else {
                    synchronized (nextGeneration) {
                        nextGeneration.add(parent1);
                    }
                }
                return null;
            }));
        }

        // Очікуємо на завершення всіх потоків
        for (Future<Void> future : futures) {
            try {
                future.get(); // Чекаємо на виконання кожного потоку
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executorService.shutdown(); // Закриваємо пул потоків
        population.clear();
        population.addAll(nextGeneration);
    }

    @Override
    public List<Integer> getBestPath() {
        return population.stream()
                .min(Comparator.comparingInt(this::calculateFitness))
                .orElse(null);
    }

    public void addMigrants(List<List<Integer>> migrants) {
        for (List<Integer> migrant : migrants) {
            int index = random.nextInt(population.size());
            population.set(index, migrant);
        }
    }

    // Додаємо метод для отримання найкращих індивідуумів
    public List<List<Integer>> getBestIndividuals(int count) {
        // Сортуємо популяцію за найкращими результатами
        return population.stream()
                .sorted(Comparator.comparingInt(this::calculateFitness))
                .limit(count)
                .collect(Collectors.toList());
    }
}
