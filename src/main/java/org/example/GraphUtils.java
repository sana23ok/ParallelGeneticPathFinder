package org.example;

import java.io.*;
import java.util.*;

public class GraphUtils {
    public static void generateGraphInput(String filename) throws IOException {
        Random rand = new Random();
        int numNodes = Constants.NUM_NODES;
        boolean[] hasEdge = new boolean[numNodes]; // To track connected nodes

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            // Ensure each node has at least one outgoing edge
            for (int i = 0; i < numNodes; i++) {
                int targetNode = rand.nextInt(numNodes);
                while (targetNode == i) {
                    targetNode = rand.nextInt(numNodes);
                }
                int weight = rand.nextInt(10) + 1;
                writer.write(i + " " + targetNode + " " + weight + "\n");
                hasEdge[i] = true;
                hasEdge[targetNode] = true;
            }

            // Add additional edges based on the chance of existence
            for (int i = 0; i < numNodes; i++) {
                for (int j = 0; j < numNodes; j++) {
                    if (i != j && rand.nextDouble() < Constants.CHANCE_EDGE_EXISTS) {
                        int weight = rand.nextInt(5) + 1;
                        writer.write(i + " " + j + " " + weight + "\n");
                        hasEdge[i] = true;
                        hasEdge[j] = true;
                    }
                }
            }

            // Check for any isolated nodes and fix them
            for (int i = 0; i < numNodes; i++) {
                if (!hasEdge[i]) {
                    int targetNode = rand.nextInt(numNodes);
                    while (targetNode == i) {
                        targetNode = rand.nextInt(numNodes);
                    }
                    int weight = rand.nextInt(5) + 1;
                    //writer.write(i + " " + targetNode + " " + weight + "\n");
                    writer.write(i + " " + targetNode + " " + weight + "\n");
                    writer.write(targetNode + " " + i + " " + weight + "\n");

                    hasEdge[i] = true;
                    hasEdge[targetNode] = true;
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
