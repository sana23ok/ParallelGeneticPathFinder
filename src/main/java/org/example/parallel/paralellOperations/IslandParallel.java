package org.example.parallel.paralellOperations;

import org.example.Island;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.example.Constants.POPULATION_SIZE;

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

//    @Override
//    protected void initializePopulation() {
//        Queue<List<Integer>> tempPopulation = new ConcurrentLinkedQueue<>();
//
//        IntStream.range(0, POPULATION_SIZE * 3) // з запасом, бо не всі будуть валідні
//                .parallel()
//                .mapToObj(i -> {
//                    List<Integer> path = generateRandomPath();
//                    return isValidPath(path) ? path : null;
//                })
//                .filter(Objects::nonNull)
//                .limit(POPULATION_SIZE) // беремо рівно POPULATION_SIZE валідних
//                .forEach(tempPopulation::add);
//
//        population = new ArrayList<>(tempPopulation);
//    }

    @Override
    protected void initializePopulation() {
        //System.out.println("initializePopulation() running in " + Thread.currentThread().getName());
        //використовує спільний ForkJoinPool
        IntStream.range(0, POPULATION_SIZE).parallel().forEach(i -> {
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
        evaluatePopulation();

        List<List<Integer>> nextGeneration = population
                .parallelStream()
                .map(individual -> {
                    List<Integer> parent1 = tournamentSelection();
                    List<Integer> parent2 = tournamentSelection();
                    List<Integer> child = crossover(parent1, parent2);
                    mutate(child);
                    return isValidPath(child) ? child : parent1;
                })
                .collect(Collectors.toList());

        population.clear();
        population.addAll(nextGeneration);
    }

    public List<List<Integer>> getBestIndividuals(int count) {
        return population.stream()
                .sorted(Comparator.comparingInt(this::calculateFitness))
                .limit(count)
                .toList();
    }

    @Override
    public void addMigrants(List<List<Integer>> migrants) {
        for (List<Integer> migrant : migrants) {
            int index = random.nextInt(population.size());
            population.set(index, migrant);
        }
    }
}
