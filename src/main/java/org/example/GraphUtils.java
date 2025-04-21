package org.example;

import java.io.*;
import java.util.*;

public class GraphUtils {

    public static void generateGraphInput(String filename) throws IOException {
        Random rand = new Random();
        int numNodes = Constants.NUM_NODES;
        int[][] matrix = new int[numNodes][numNodes];

        // Генеруємо симетричну матрицю
        for (int i = 0; i < numNodes; i++) {
            for (int j = i + 1; j < numNodes; j++) {
                if (rand.nextDouble() < Constants.CHANCE_EDGE_EXISTS) {
                    int weight = rand.nextInt(9) + 1; // ваги від 1 до 9
                    matrix[i][j] = weight;
                    matrix[j][i] = weight;
                }
            }
        }

        // Забезпечуємо, що кожна вершина має хоча б одне ребро
        for (int i = 0; i < numNodes; i++) {
            boolean hasEdge = false;
            for (int j = 0; j < numNodes; j++) {
                if (matrix[i][j] != 0) {
                    hasEdge = true;
                    break;
                }
            }
            if (!hasEdge) {
                int target = rand.nextInt(numNodes);
                while (target == i) {
                    target = rand.nextInt(numNodes);
                }
                int weight = rand.nextInt(9) + 1;
                matrix[i][target] = weight;
                matrix[target][i] = weight;
            }
        }

        // Записуємо матрицю у файл
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (int i = 0; i < numNodes; i++) {
                for (int j = 0; j < numNodes; j++) {
                    writer.write(matrix[i][j] + (j == numNodes - 1 ? "" : " "));
                }
                writer.newLine();
            }
        }
    }

    public static void loadGraph(String filename, int[][] graph) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            int row = 0;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                for (int col = 0; col < parts.length; col++) {
                    graph[row][col] = Integer.parseInt(parts[col]);
                }
                row++;
            }
        }
    }
}
