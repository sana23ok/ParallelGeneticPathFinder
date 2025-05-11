package org.example.parallel.executor_version;

import org.example.Constants;
import org.example.Island;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

public class IslandParallel extends Island {
    private final ExecutorService executor;

    public IslandParallel(int[][] graph, ExecutorService executor) {
        super(graph);
        this.executor = executor;
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

    protected void initializePopulation() {
        List<List<Integer>> populationSafe = Collections.synchronizedList(new ArrayList<>());

        List<Callable<Void>> tasks = IntStream.range(0, Constants.POPULATION_SIZE)
                .mapToObj(i -> (Callable<Void>) () -> {
                    List<Integer> path = generateRandomPath();
                    if (isValidPath(path)) {
                        populationSafe.add(path);
                    }
                    return null;
                }).collect(Collectors.toList());

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        population = populationSafe;

    }

    @Override
    public void evolve() {
        List<List<Integer>> nextGeneration = Collections.synchronizedList(new ArrayList<>(population.size()));
        evaluatePopulationParallel();

        List<Callable<Void>> evolutionTasks = IntStream.range(0, population.size())
                .mapToObj(i -> (Callable<Void>) () -> {
                    List<Integer> parent1 = tournamentSelection();
                    List<Integer> parent2 = tournamentSelection();
                    List<Integer> child = crossover(parent1, parent2);
                    mutate(child);
                    List<Integer> individualToAdd = isValidPath(child) ? child : new ArrayList<>(parent1);
                    nextGeneration.add(individualToAdd);
                    return null;
                }).collect(Collectors.toList());

        try {
            executor.invokeAll(evolutionTasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        population.clear();
        population.addAll(nextGeneration);
    }

    private void evaluatePopulationParallel() {
        List<Callable<Void>> fitnessTasks = population.stream()
                .map(individual -> (Callable<Void>) () -> {
                    calculateFitness(individual);
                    return null;
                }).collect(Collectors.toList());

        try {
            executor.invokeAll(fitnessTasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public List<List<Integer>> getBestIndividuals(int count) {
        return population.stream()
                .sorted(Comparator.comparingInt(this::calculateFitness))
                .limit(count)
                .map(ArrayList::new)
                .collect(Collectors.toList());
    }

    @Override
    public void addMigrants(List<List<Integer>> migrants) {
        migrants.forEach(migrant -> {
            int index = random.nextInt(population.size());
            population.set(index, new ArrayList<>(migrant));
        });
    }

//    @Override
//    protected int calculateFitness(List<Integer> path) {
//        if (fitnessCache.containsKey(path)) {
//            return fitnessCache.get(path);
//        }
//
//        int fitness = 0;
//        for (int i = 0; i < path.size() - 1; i++) {
//            fitness += graph[path.get(i)][path.get(i + 1)];
//        }
//
//        fitnessCache.put(path, fitness);
//        return fitness;
//    }

}
