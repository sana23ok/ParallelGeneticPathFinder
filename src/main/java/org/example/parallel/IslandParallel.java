package org.example.parallel;

import org.example.Island;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static org.example.Constants.POPULATION_SIZE;
import static org.example.Constants.TOURNAMENT_SIZE;

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
        // Ініціалізація популяції з використанням паралельного потоку:
        // для кожного створюється валідний маршрут у паралельному режимі
        List<List<Integer>> parallelPopulation = Collections.synchronizedList(new ArrayList<>());

        IntStream.range(0, POPULATION_SIZE).parallel().forEach(i -> {
            while (true) {
                List<Integer> path = generateRandomPath();
                if (isValidPath(path)) {
                    parallelPopulation.add(path);
                    break;
                }
            }
        });

        population.addAll(parallelPopulation);
    }


    @Override
    public void evolve() {
        // Основна еволюційна фаза: кожен індивідуум створюється паралельно
        // за рахунок вибору батьків, кросоверу, мутації та перевірки валідності.
        List<List<Integer>> nextGeneration = Collections.synchronizedList(new ArrayList<>());
        evaluatePopulation(); // Оцінка популяції без паралельності (як раніше)

        // Паралельне виконання еволюційного процесу
        IntStream.range(0, population.size()).parallel().forEach(i -> {
            // Вибір батьків
            List<Integer> parent1 = tournamentSelection();
            List<Integer> parent2 = tournamentSelection();

            // Кросовер між батьками
            List<Integer> child = crossover(parent1, parent2);

            // Мутація
            mutate(child);

            // Перевірка валідності і додавання до наступного покоління
            if (isValidPath(child)) {
                nextGeneration.add(child);
            } else {
                nextGeneration.add(parent1); // fallback, якщо шлях не валідний
            }
        });

        // Оновлення популяції
        population.clear();
        population.addAll(nextGeneration);
    }


    public List<List<Integer>> getBestIndividuals(int count) {
        return population.parallelStream()
                .sorted(Comparator.comparingInt(this::calculateFitness))
                .limit(count)
                .collect(Collectors.toList());
    }


    public void addMigrants(List<List<Integer>> migrants) {
        for (List<Integer> migrant : migrants) {
            int index = random.nextInt(population.size());
            population.set(index, migrant);
        }
    }


    @Override
    protected void evaluatePopulation() {
        // Паралельна оцінка фітнесу кожного індивідуума в популяції
        population.parallelStream().forEach(this::calculateFitness);
    }


    @Override
    protected List<Integer> tournamentSelection() {
        return IntStream.range(0, TOURNAMENT_SIZE)
                .mapToObj(i -> population.get(random.nextInt(population.size())))
                .min(Comparator.comparingInt(this::calculateFitness))
                .orElse(population.get(0)); // fallback
    }
}
