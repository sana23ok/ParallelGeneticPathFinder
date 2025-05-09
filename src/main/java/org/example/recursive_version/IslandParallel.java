package org.example.recursive_version;

import org.example.Constants;
import org.example.Island;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

public class IslandParallel extends Island {

    private final ForkJoinPool pool;

    public IslandParallel(int[][] graph) {
        super(graph);
        this.pool = new ForkJoinPool();
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
        List<RecursiveAction> tasks = IntStream.range(0, Constants.POPULATION_SIZE)
                .mapToObj(i -> new RecursiveAction() {
                    @Override
                    protected void compute() {
                        List<Integer> path = generateRandomPath();
                        if (isValidPath(path)) {
                            synchronized (population) {
                                population.add(path);
                            }
                        }
                    }
                })
                .collect(Collectors.toList());

        tasks.forEach(pool::invoke);
    }

    @Override
    public void evolve() {
        List<List<Integer>> nextGeneration = new ArrayList<>(population.size());
        evaluatePopulationParallel();

        List<RecursiveAction> evolutionTasks = IntStream.range(0, population.size())
                .mapToObj(i -> new RecursiveAction() {
                    @Override
                    protected void compute() {
                        List<Integer> parent1 = tournamentSelection();
                        List<Integer> parent2 = tournamentSelection();
                        List<Integer> child = crossover(parent1, parent2);
                        mutate(child);
                        List<Integer> individualToAdd = isValidPath(child) ? child : new ArrayList<>(parent1);
                        synchronized (nextGeneration) {
                            nextGeneration.add(individualToAdd);
                        }
                    }
                })
                .collect(Collectors.toList());

        evolutionTasks.forEach(pool::invoke);

        population.clear();
        population.addAll(nextGeneration);
    }

    private void evaluatePopulationParallel() {
        List<RecursiveAction> tasks = population.stream()
                .map(individual -> new RecursiveAction() {
                    @Override
                    protected void compute() {
                        calculateFitness(individual);
                    }
                })
                .collect(Collectors.toList());
        tasks.forEach(pool::invoke);
    }

//    private class FitnessCalculationTask extends RecursiveAction {
//        private final List<Integer> path;
//
//        public FitnessCalculationTask(List<Integer> path) {
//            this.path = Collections.unmodifiableList(path);
//        }
//
//        @Override
//        protected void compute() {
//            calculateFitness(path);
//        }
//    }

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
}