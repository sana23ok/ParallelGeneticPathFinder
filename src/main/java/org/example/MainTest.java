package org.example;

import java.io.IOException;

import static org.example.GraphUtils.*;
import org.example.islandSequential.ShortestPathGAIslandSequential;


public class MainTest {
    public static void main(String[] args) throws IOException {
        // Тестування для різних параметрів
        for (int numNodes = 500; numNodes <= 2500; numNodes += 500) { // Розмір графа
            // Оновлення констант з параметрів
            Constants.NUM_NODES = numNodes;
            // Генерація вхідного файлу для графа
            String filename = "graph_" + numNodes + "_" + (int) (Constants.CHANCE_EDGE_EXISTS* 100) + "_" + Constants.POPULATION_SIZE + ".txt";
            generateGraphInput(filename);

            // Завантаження графа в пам'ять
            int[][] graph = new int[Constants.NUM_NODES][Constants.NUM_NODES];
            loadGraph(filename, graph);

            // Вимірювання часу виконання для ShortestPathGAIslandSequential
            long startTimeGA = System.nanoTime();
            ShortestPathGAIslandSequential.run(graph);
            long endTimeGA = System.nanoTime();
            long durationGA = endTimeGA - startTimeGA;

            // Переведення часу в мілісекунди
            double durationInMillis = durationGA / 1_000_000.0;

            // Виведення результату
            System.out.println("Nodes: " + numNodes);
            System.out.println("Time for Sequential: " + durationInMillis + " ms");

        }

        for (int numNodes = 3000; numNodes <= 10000; numNodes += 1000) { // Розмір графа
            // Оновлення констант з параметрів
            Constants.NUM_NODES = numNodes;

            String filename = "graph_" + numNodes + "_" + (int) (Constants.CHANCE_EDGE_EXISTS* 100) + "_" + Constants.POPULATION_SIZE + ".txt";
            generateGraphInput(filename);

            // Завантаження графа в пам'ять
            int[][] graph = new int[Constants.NUM_NODES][Constants.NUM_NODES];
            loadGraph(filename, graph);

            // Вимірювання часу виконання для ShortestPathGAIslandSequential
            long startTimeGA = System.nanoTime();
            ShortestPathGAIslandSequential.run(graph);
            long endTimeGA = System.nanoTime();
            long durationGA = endTimeGA - startTimeGA;

            // Переведення часу в мілісекунди
            double durationInMillis = durationGA / 1_000_000.0;

            // Виведення результату
            System.out.println("Nodes: " + numNodes);
            System.out.println("Time for Sequential: " + durationInMillis + " ms");

        }
    }
}

