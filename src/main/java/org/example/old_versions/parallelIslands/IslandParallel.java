package org.example.old_versions.parallel_old.parallelIslands;

import org.example.Island;
import java.util.*;
import java.util.concurrent.*;
import static org.example.Constants.*;

public class IslandParallel extends Island {
    private ForkJoinPool pool;

    public IslandParallel(int[][] graph) {
        super(graph);
        this.pool = ForkJoinPool.commonPool();
        initializePopulation();
    }

    @Override
    protected List<List<Integer>> createPopulationList() {
        return Collections.synchronizedList(new ArrayList<>());
    }

    @Override
    protected Map<List<Integer>, Integer> createFitnessCacheMap() {
        return new ConcurrentHashMap<>();
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
        evaluatePopulation();

        List<Callable<List<Integer>>> tasks = new ArrayList<>();
        for (int i = 0; i < population.size(); i++) {
            tasks.add(() -> {
                List<Integer> parent1 = tournamentSelection();
                List<Integer> parent2 = tournamentSelection();
                List<Integer> child = crossover(parent1, parent2);
                mutate(child);
                return isValidPath(child) ? child : parent1;
            });
        }

        try {
            List<Future<List<Integer>>> futures = ((ExecutorService) pool).invokeAll(tasks);
            List<List<Integer>> nextGeneration = new ArrayList<>();
            for (Future<List<Integer>> future : futures) {
                nextGeneration.add(future.get());
            }
            population.clear();
            population.addAll(nextGeneration);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void evaluatePopulation() {
        population.parallelStream().forEach(this::calculateFitness);
    }

    public List<List<Integer>> getBestIndividuals(int count) {
        return population.stream()
                .sorted(Comparator.comparingInt(this::calculateFitness))
                .limit(count)
                .toList();
    }

    @Override
    public void addMigrants(List<List<Integer>> migrants) {
        migrants.parallelStream().forEach(migrant -> {
            int index = random.nextInt(population.size());
            population.set(index, migrant);
        });
    }
}
