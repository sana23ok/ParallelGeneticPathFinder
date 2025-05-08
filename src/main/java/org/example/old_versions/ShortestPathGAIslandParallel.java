//package org.example.old_versions;
//
//import org.example.graph.GraphVisualizer;
//
//import java.util.*;
//import java.util.concurrent.*;
//
//import static org.example.Constants.*;
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
//        for (int i = 0; i < NUM_ISLANDS; i++) {
//            islands.add(new IslandParallel(graph));
//        }
//
//        // Паралельна еволюція островів
//        List<Callable<Void>> tasks = new ArrayList<>();
//        for (IslandParallel island : islands) {
//            tasks.add(() -> {
//                for (int gen = 0; gen < GENERATIONS; gen++) {
//                    island.evolve();
//                    if (gen > 0 && gen % MIGRATION_INTERVAL == 0) {
//                        migrate(islands);
//                    }
//                }
//                return null;
//            });
//        }
//
//        // Виконуємо еволюцію для кожного острова паралельно
//        ExecutorService executorService = Executors.newFixedThreadPool(NUM_ISLANDS);
//        try {
//            executorService.invokeAll(tasks); // запуск усіх еволюцій в паралельних потоках
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        executorService.shutdown();
//
//        // Знаходимо найкращий шлях серед усіх островів
//        return islands.stream()
//                .map(IslandParallel::getBestPath)
//                .min(Comparator.comparingInt(p -> calculateFitness(p, graph)))
//                .orElse(null);
//    }
//
//    private void migrate(List<IslandParallel> islands) {
//        for (int i = 0; i < islands.size(); i++) {
//            IslandParallel source = islands.get(i);
//            IslandParallel target = islands.get((i + 1) % islands.size());
//
//            List<List<Integer>> migrants = source.getBestIndividuals(MIGRATION_COUNT);
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
//        // If the number of nodes is small, we can visualize the graph
//        if (NUM_NODES <= 20) {
//            GraphVisualizer visualizer = new GraphVisualizer(graph);
//            visualizer.showGraph(shortestPath);
//        }
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
//
