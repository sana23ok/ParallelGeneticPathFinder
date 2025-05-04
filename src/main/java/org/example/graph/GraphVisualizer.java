package org.example.graph;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import java.util.List;

public class GraphVisualizer {

    private int[][] adjacencyMatrix;

    public GraphVisualizer(int[][] adjacencyMatrix) {
        this.adjacencyMatrix = adjacencyMatrix;
    }

    public void showGraph(List<Integer> bestPath) {
        Graph graph = new SingleGraph("Genetic Algorithm Path");

        int n = adjacencyMatrix.length;

        // Add nodes
        for (int i = 0; i < n; i++) {
            graph.addNode(String.valueOf(i)).setAttribute("ui.label", String.valueOf(i));
        }

        // Add edges
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (adjacencyMatrix[i][j] > 0) {
                    String edgeId = i + "-" + j;
                    graph.addEdge(edgeId, String.valueOf(i), String.valueOf(j))
                            .setAttribute("ui.label", adjacencyMatrix[i][j]);
                }
            }
        }

        // Highlight best path
        if (bestPath != null) {
            for (int i = 0; i < bestPath.size() - 1; i++) {
                String edgeId = bestPath.get(i) + "-" + bestPath.get(i + 1);
                String reverseEdgeId = bestPath.get(i + 1) + "-" + bestPath.get(i);
                if (graph.getEdge(edgeId) != null) {
                    graph.getEdge(edgeId).setAttribute("ui.style", "fill-color: red; size: 4px;");
                } else if (graph.getEdge(reverseEdgeId) != null) {
                    graph.getEdge(reverseEdgeId).setAttribute("ui.style", "fill-color: red; size: 4px;");
                }

            }
        }

        // Updated stylesheet for node labels (dark blue with white outline)
        graph.setAttribute("ui.stylesheet",
                "node { text-size: 16; text-color: #00008B; text-style: bold; text-background-mode: rounded-box; text-background-color: #FFFFFF; text-padding: 5px; fill-color: #87CEFA; }" +
                        "edge { text-size: 14; }");
        graph.display();
    }
}


