package org.example.parallel;

import org.example.graph.GraphVisualizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static org.example.Constants.*;

public class ShortestPathGAIslandParallel {

    private final int[][] graph;


    public ShortestPathGAIslandParallel(int[][] graph) {
        this.graph = graph;
    }


    public List<Integer> findShortestPath() {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_ISLANDS);// Пул потоків для островів
        List<IslandParallel> islands = new ArrayList<>();
        //System.out.println("Num of islands: " + numIslands);

        for (int i = 0; i < NUM_ISLANDS; i++) {
            islands.add(new IslandParallel(graph));
        }

        for (int gen = 0; gen < GENERATIONS; gen++) {
            List<Callable<Void>> tasks = new ArrayList<>();
            for (IslandParallel island : islands) {
                // Кожен острів виконує еволюцію в окремому потоці
                tasks.add(() -> {
                    island.evolve();
                    return null;
                });
            }
            try {
                executor.invokeAll(tasks); // Запускає всі задачі паралельно
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (gen > 0 && gen % MIGRATION_INTERVAL == 0) {
                migrate(islands);  // Міграція після кожного N-го покоління
            }
        }

        executor.shutdown();

        // Паралельний пошук найкращого шляху серед усіх островів:
        // кожен острів повертає свій найкращий шлях у паралельному потоці,
        // після чого обирається глобально найкращий шлях за мінімальним фітнесом.

        // Знаходимо найкращий шлях серед усіх островів
        return islands.parallelStream()
                .map(IslandParallel::getBestPath)
                .min(Comparator.comparingInt(p -> calculateFitness(p, graph)))
                .orElse(null);

    }


    private void migrate(List<IslandParallel> islands) {
        //підготувати список пар source -> migrants паралельно, а внесення змін — серіалізувати:
        List<List<List<Integer>>> migrantsList = IntStream.range(0, islands.size())
                .parallel()
                .mapToObj(i -> islands.get(i).getBestIndividuals(MIGRATION_COUNT))
                .collect(Collectors.toList());

        // Потім послідовне оновлення (зміна стану!)
        for (int i = 0; i < islands.size(); i++) {
            IslandParallel target = islands.get((i + 1) % islands.size());
            target.addMigrants(migrantsList.get(i));
        }

    }


    public static List<Integer> run(int[][] graph) {
        ShortestPathGAIslandParallel ga = new ShortestPathGAIslandParallel(graph);
        List<Integer> shortestPath = ga.findShortestPath();

        System.out.println("Fitness: " + ga.calculateFitness(shortestPath, graph));
        //System.out.println("Shortest path: " + shortestPath);

        if (NUM_NODES <= 20) {
            GraphVisualizer visualizer = new GraphVisualizer(graph);
            visualizer.showGraph(shortestPath);
        }
        return shortestPath;
    }


    private int calculateFitness(List<Integer> path, int[][] graph) {
        int fitness = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            fitness += graph[path.get(i)][path.get(i + 1)];
        }
        return fitness;
    }
}
