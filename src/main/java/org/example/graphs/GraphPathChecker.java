package org.example.graphs;

import java.io.*;
import java.util.*;

public class GraphPathChecker {

//    public static void main(String[] args) {
//        // Зчитуємо матрицю суміжності з файлу
//        int[][] adjacencyMatrix = readAdjacencyMatrix("graph.txt");
//
//        // Заданий шлях
//        int[] path = {0, 1788, 555, 1703, 2886, 1202, 77, 488, 747, 637, 2393, 2898, 15, 1529, 2089, 1685, 1100, 2453, 1334, 1528, 2834, 1501};
//
//        // Перевірка шляху
//        boolean exists = pathExists(adjacencyMatrix, path);
//        System.out.println("Does path exists? : " + (exists ? "Yes" : "No"));
//    }

    public void check(String fileName, List<Integer> pathF) {
        // Зчитуємо матрицю суміжності з файлу
        int[] path = pathF.stream().mapToInt(Integer::intValue).toArray();

        int[][] adjacencyMatrix = readAdjacencyMatrix(fileName);

        // Перевірка шляху
        boolean exists = pathExists(adjacencyMatrix, path);
        System.out.println("Does path exists? : " + (exists ? "Yes" : "No"));
    }

    // Зчитування матриці з файлу
    private static int[][] readAdjacencyMatrix(String filename) {
        List<int[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.trim().split("\\s+|,");
                int[] row = Arrays.stream(tokens)
                        .mapToInt(Integer::parseInt)
                        .toArray();
                rows.add(row);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        }
        return rows.toArray(new int[0][]);
    }

    // Перевірка чи існує шлях у графі
    private static boolean pathExists(int[][] matrix, int[] path) {
        for (int i = 0; i < path.length - 1; i++) {
            int from = path[i];
            int to = path[i + 1];
            // Перевірка чи існує ребро між вершинами
            if (from >= matrix.length || to >= matrix.length || matrix[from][to] == 0) {
                return false;
            }
        }
        return true;
    }
}
