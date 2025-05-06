//package org.example.parallel.new_version;
//
//import org.example.Constants;
//import org.example.graph.GraphVisualizer;
//import java.util.*;
//import java.util.concurrent.ForkJoinPool;
//import java.util.concurrent.RecursiveTask;
//
//public class ShortestPathGAIslandParallel {
//    private final int[][] graph;
//
//    public ShortestPathGAIslandParallel(int[][] graph) {
//        this.graph = graph;
//    }
//
//    public List<Integer> findShortestPath() {
//        List<IslandParallel> islands = new ArrayList<>();
//
//        // Ініціалізація островів
//        for (int i = 0; i < Constants.NUM_ISLANDS; i++) {
//            islands.add(new IslandParallel(graph));
//        }
//
//        // Еволюція поколінь
//        for (int gen = 0; gen < Constants.GENERATIONS; gen++) {
//            List<RecursiveTask<Void>> tasks = new ArrayList<>();
//            for (IslandParallel island : islands) {
//                tasks.add(new EvolutionTask(island)); // Завдання для еволюції острова
//            }
//            ForkJoinPool.commonPool().invokeAll(tasks); // Паралельне виконання
//
//            if (gen > 0 && gen % Constants.MIGRATION_INTERVAL == 0) {
//                migrate(islands);
//            }
//        }
//
//        // Знаходимо найкращий шлях серед усіх островів
//        return islands.stream()
//                .map(IslandParallel::getBestPath)
//                .min(Comparator.comparingInt(p -> calculateFitness(p, graph)))
//                .orElse(null);
//    }
//
//    // Завдання для еволюції кожного острова
//    private class EvolutionTask extends RecursiveTask<Void> {
//        private final IslandParallel island;
//
//        public EvolutionTask(IslandParallel island) {
//            this.island = island;
//        }
//
//        @Override
//        protected Void compute() {
//            island.evolve(); // Виконуємо еволюцію для острова
//            return null;
//        }
//    }
//
//    private void migrate(List<IslandParallel> islands) {
//        for (int i = 0; i < islands.size(); i++) {
//            IslandParallel source = islands.get(i);
//            IslandParallel target = islands.get((i + 1) % islands.size());
//
//            List<List<Integer>> migrants = source.getBestIndividuals(Constants.MIGRATION_COUNT);
//            target.addMigrants(migrants);
//        }
//    }
//
//    public static List<Integer> run(int[][] graph) {
//        ShortestPathGAIslandParallel ga = new ShortestPathGAIslandParallel(graph);
//        List<Integer> shortestPath = ga.findShortestPath();
//
//        System.out.println("Fitness: " + ga.calculateFitness(shortestPath, graph));
//        System.out.println("Shortest path: " + shortestPath);
//        return shortestPath;
//    }
//
//    private int calculateFitness(List<Integer> path, int[][] graph) {
//        int fitness = 0;
//        for (int i = 0; i < path.size() - 1; i++) {
//            fitness += graph[path.get(i)][path.get(i + 1)];
//        }
//        return fitness;
//    }
//}
