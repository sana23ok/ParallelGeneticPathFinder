//package org.example.parallel.new_version;
//
//import org.example.Island;
//import org.example.Constants;
//import java.util.*;
//import java.util.concurrent.ForkJoinPool;
//import java.util.concurrent.RecursiveTask;
//
//public class IslandParallel extends Island {
//
//    private final ForkJoinPool pool;
//
//    public IslandParallel(int[][] graph) {
//        super(graph);
//        this.pool = new ForkJoinPool(); // Створюємо пул потоків
//    }
//
//    @Override
//    protected List<List<Integer>> createPopulationList() {
//        return new ArrayList<>();
//    }
//
//    @Override
//    protected Map<List<Integer>, Integer> createFitnessCacheMap() {
//        return new HashMap<>();
//    }
//
//    @Override
//    protected void initializePopulation() {
//        while (population.size() < Constants.POPULATION_SIZE) {
//            List<Integer> path = generateRandomPath();
//            if (isValidPath(path)) {
//                population.add(path);
//            }
//        }
//    }
//
//    @Override
//    public void evolve() {
//        List<RecursiveTask<Void>> tasks = new ArrayList<>();
//
//        // Розпаралелюємо еволюцію для кожної особини популяції
//        for (int i = 0; i < population.size(); i++) {
//            tasks.add(new EvolutionTask(i)); // Додаємо задачу еволюції
//        }
//
//        // Виконання всіх задач паралельно
//        pool.invokeAll(tasks);
//
//        // Створюємо нове покоління після еволюції
//        List<List<Integer>> nextGeneration = new ArrayList<>();
//        for (int i = 0; i < population.size(); i++) {
//            List<Integer> parent1 = tournamentSelection();
//            List<Integer> parent2 = tournamentSelection();
//            List<Integer> child = crossover(parent1, parent2);
//            mutate(child);
//            if (isValidPath(child)) {
//                nextGeneration.add(child);
//            } else {
//                nextGeneration.add(parent1);
//            }
//        }
//
//        population.clear();
//        population.addAll(nextGeneration);
//    }
//
//    // Завдання для паралельної еволюції
//    private class EvolutionTask extends RecursiveTask<Void> {
//        private final int index;
//
//        public EvolutionTask(int index) {
//            this.index = index;
//        }
//
//        @Override
//        protected Void compute() {
//            List<Integer> individual = population.get(index);
//            int fitness = calculateFitness(individual);
//            // Тут може бути додаткове обчислення чи зміна популяції
//            return null;
//        }
//    }
//
//    public void addMigrants(List<List<Integer>> migrants) {
//        for (List<Integer> migrant : migrants) {
//            int index = random.nextInt(population.size());
//            population.set(index, migrant);
//        }
//    }
//}
