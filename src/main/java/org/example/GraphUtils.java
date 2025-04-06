package org.example;

import java.io.*;
import java.util.Arrays;
import java.util.Random;

public class GraphUtils {
    public static void generateGraphInput(String filename) throws IOException {
        Random rand = new Random();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (int i = 0; i < Constants.NUM_NODES; i++) {
                for (int j = 0; j < Constants.NUM_NODES; j++) {
                    if (i != j && rand.nextDouble() < Constants.CHANCE_EDGE_EXISTS) {
                        int weight = rand.nextInt(5) + 1;
                        writer.write(i + " " + j + " " + weight + "\n");
                    }
                }
            }
        }
    }

    public static void loadGraph(String filename, int[][] graph) throws IOException {
        for (int[] row : graph) {
            Arrays.fill(row, 0);
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                int u = Integer.parseInt(parts[0]);
                int v = Integer.parseInt(parts[1]);
                int w = Integer.parseInt(parts[2]);
                graph[u][v] = w;
            }
        }
    }
}

