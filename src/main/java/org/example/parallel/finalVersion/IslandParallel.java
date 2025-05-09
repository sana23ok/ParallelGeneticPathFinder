package org.example.parallel.finalVersion;

import org.example.Island;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.example.Constants.*;

public class IslandParallel extends Island {

    private final ForkJoinPool pool;

    public IslandParallel(int[][] graph, ForkJoinPool pool) {
        super(graph);
        this.pool = pool;
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
        //System.out.println("initializePopulation() running in " + Thread.currentThread().getName());
        try {
            pool.submit(() ->
                    IntStream.range(0, POPULATION_SIZE).parallel().forEach(i -> {
                        while (true) {
                            List<Integer> path = generateRandomPath();
                            if (isValidPath(path)) {
                                population.add(path);
                                break;
                            }
                        }
                    })
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("initializePopulation failed", e);
        }
    }

    @Override
    public void evolve() {
        //System.out.println("evolve() child creation in " + Thread.currentThread().getName());
        //System.out.println("Island " + this + " running in " + Thread.currentThread().getName());
        List<List<Integer>> nextGeneration = new CopyOnWriteArrayList<>();

        evaluatePopulation();

        try {
            pool.submit(() ->
                    IntStream.range(0, population.size()).parallel().forEach(i -> {
                        List<Integer> parent1 = tournamentSelection();
                        List<Integer> parent2 = tournamentSelection();
                        List<Integer> child = crossover(parent1, parent2);
                        mutate(child);
                        if (isValidPath(child)) {
                            nextGeneration.add(child);
                        } else {
                            nextGeneration.add(parent1);
                        }
                    })
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("evolve failed", e);
        }

        synchronized (population) {
            population.clear();
            population.addAll(nextGeneration);
        }
    }

    @Override
    protected void evaluatePopulation() {
        //System.out.println("evaluatePopulation() running in " + Thread.currentThread().getName());
        try {
            pool.submit(() -> population.parallelStream().forEach(this::calculateFitness)).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("evaluatePopulation failed", e);
        }
    }

    @Override
    protected List<Integer> tournamentSelection() {
        // tournamentSelection itself is single-threaded, so no change
        return IntStream.range(0, TOURNAMENT_SIZE)
                .mapToObj(i -> population.get(random.nextInt(population.size())))
                .min(Comparator.comparingInt(this::calculateFitness))
                .orElse(population.get(0));
    }

    public List<List<Integer>> getBestIndividuals(int count) {
        try {
            return pool.submit(() ->
                    population.parallelStream()
                            .sorted(Comparator.comparingInt(this::calculateFitness))//sorted блокуюча?
                            .limit(count)
                            .collect(Collectors.toList())
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("getBestIndividuals failed", e);
        }
    }

    public void addMigrants(List<List<Integer>> migrants) {
        for (List<Integer> migrant : migrants) {
            int index = random.nextInt(population.size());
            population.set(index, migrant);
        }
    }
}
