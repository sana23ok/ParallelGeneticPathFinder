package org.example.parallel.recursiveTask;

import org.example.Island;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.example.Constants.*;

public class IslandParallel extends Island {

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
        List<RecursiveAction> tasks = new ArrayList<>();

        for (int i = 0; i < POPULATION_SIZE; i++) {
            tasks.add(new RecursiveAction() {
                @Override
                protected void compute() {
                    while (true) {
                        List<Integer> path = generateRandomPath();
                        if (isValidPath(path)) {
                            population.add(path);
                            break;
                        }
                    }
                }
            });
        }

        ForkJoinTask.invokeAll(tasks);  // ⬅ Виклик ForkJoin для паралельного виконання
    }

    @Override
    public void evolve() {
        List<List<Integer>> nextGeneration = new CopyOnWriteArrayList<>();
        evaluatePopulation();

        List<RecursiveAction> tasks = new ArrayList<>();

        // Створюємо завдання для кожного індивідуума в популяції
        for (int i = 0; i < population.size(); i++) {
            int index = i;
            tasks.add(new RecursiveAction() {
                @Override
                protected void compute() {
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
            });
        }

        ForkJoinTask.invokeAll(tasks);  // Викликаємо ForkJoinTask для паралельного виконання

        synchronized (population) {
            population.clear();
            population.addAll(nextGeneration);
        }
    }

    public List<List<Integer>> getBestIndividuals(int count) {
        List<RecursiveAction> tasks = new ArrayList<>();

        // Створюємо завдання для кожного індивідуума, щоб знайти найкращих
        for (List<Integer> individual : population) {
            tasks.add(new RecursiveAction() {
                @Override
                protected void compute() {
                    calculateFitness(individual);
                }
            });
        }

        ForkJoinTask.invokeAll(tasks);  // Викликаємо ForkJoin для паралельного виконання

        // Сортуємо і повертаємо кращих індивідуумів
        return population.stream()
                .sorted(Comparator.comparingInt(this::calculateFitness))
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    protected void evaluatePopulation() {
        List<RecursiveAction> tasks = new ArrayList<>();

        // Створюємо рекурсивні завдання для оцінки кожного шляху
        for (List<Integer> path : population) {
            tasks.add(new RecursiveAction() {
                @Override
                protected void compute() {
                    calculateFitness(path);
                }
            });
        }

        ForkJoinTask.invokeAll(tasks);  // Запуск паралельних обчислень
    }

    @Override
    protected List<Integer> tournamentSelection() {
        return IntStream.range(0, TOURNAMENT_SIZE)
                .mapToObj(i -> population.get(random.nextInt(population.size())))
                .min(Comparator.comparingInt(this::calculateFitness))
                .orElse(population.get(0));
    }

    public void addMigrants(List<List<Integer>> migrants) {
        for (List<Integer> migrant : migrants) {
            int index = random.nextInt(population.size());
            population.set(index, migrant);
        }
    }
}
