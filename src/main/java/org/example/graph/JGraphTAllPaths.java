//package org.example.graph;
//
//import org.jgrapht.Graph;
//import org.jgrapht.graph.DefaultWeightedEdge;
//import org.jgrapht.graph.SimpleWeightedGraph;
//
//import java.io.*;
//import java.util.*;
//
//public class JGraphTAllPaths {
//
//    public static void main(String[] args) {
//        // Шлях до файлу з матрицею суміжності
//        String filePath = "C:\\Users\\oksan\\IdeaProjects\\PathFinder\\graph.txt";
//
//        // Зчитування матриці суміжності з файлу
//        double[][] adjMatrix = readAdjacencyMatrixFromFile(filePath);
//
//        // Кількість вершин
//        int numNodes = adjMatrix.length;
//
//        // Створення графа JGraphT
//        Graph<Integer, DefaultWeightedEdge> graph = createGraphFromMatrix(adjMatrix);
//
//        // Визначення цільової вершини
//        int target = numNodes / 2;
//
//        // Список для зберігання всіх шляхів
//        List<List<Integer>> allPaths = new ArrayList<>();
//
//        // Функція для пошуку всіх шляхів між вершинами
//        findAllPaths(graph, 0, target, new ArrayList<>(), allPaths);
//
//        // Обчислення ваги кожного шляху
//        List<Pair<Double, List<Integer>>> weightedPaths = new ArrayList<>();
//        for (List<Integer> path : allPaths) {
//            double weight = calculatePathWeight(graph, path);
//            weightedPaths.add(new Pair<>(weight, path));
//        }
//
//        // Сортування шляхів за вагою у порядку зростання
//        weightedPaths.sort(Comparator.comparing(p -> p.getKey()));
//
//        // Виведення результату
//        for (int i = 0; i < weightedPaths.size(); i++) {
//            Pair<Double, List<Integer>> pair = weightedPaths.get(i);
//            System.out.println((i + 1) + ". Шлях: " + pair.getValue() + ", Вага: " + pair.getKey());
//        }
//    }
//
//    // Функція для зчитування матриці суміжності з файлу
//    public static double[][] readAdjacencyMatrixFromFile(String filePath) {
//        List<List<Double>> matrix = new ArrayList<>();
//        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
//            String line;
//            while ((line = br.readLine()) != null) {
//                String[] values = line.trim().split("\\s+"); // Розділення за пробілами
//                List<Double> row = new ArrayList<>();
//                for (String value : values) {
//                    row.add(Double.parseDouble(value));
//                }
//                matrix.add(row);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        // Перетворення List<List<Double>> в double[][]
//        double[][] adjMatrix = new double[matrix.size()][matrix.get(0).size()];
//        for (int i = 0; i < matrix.size(); i++) {
//            List<Double> row = matrix.get(i);
//            for (int j = 0; j < row.size(); j++) {
//                adjMatrix[i][j] = row.get(j);
//            }
//        }
//        return adjMatrix;
//    }
//
//    // Функція для створення графа JGraphT на основі матриці суміжності
//    public static Graph<Integer, DefaultWeightedEdge> createGraphFromMatrix(double[][] adjMatrix) {
//        int numNodes = adjMatrix.length;
//        Graph<Integer, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
//
//        // Додавання вершин
//        for (int i = 0; i < numNodes; i++) {
//            graph.addVertex(i);
//        }
//
//        // Додавання ребер з вагами
//        for (int i = 0; i < numNodes; i++) {
//            for (int j = 0; j < numNodes; j++) {
//                if (adjMatrix[i][j] != 0) { // Припускаємо, що 0 означає відсутність ребра
//                    DefaultWeightedEdge edge = graph.addEdge(i, j);
//                    if (edge != null) {
//                        graph.setEdgeWeight(edge, adjMatrix[i][j]);
//                    }
//                }
//            }
//        }
//        return graph;
//    }
//
//
//    // Функція для пошуку всіх шляхів між вершинами (аналог NetworkX find_all_paths)
//    public static void findAllPaths(Graph<Integer, DefaultWeightedEdge> graph, Integer start, Integer end, List<Integer> path, List<List<Integer>> allPaths) {
//        path.add(start);
//        if (start.equals(end)) {
//            allPaths.add(new ArrayList<>(path)); // Додаємо копію шляху
//        } else {
//            Set<Integer> neighbors = Graphs.neighborSetOf(graph, start); // Отримання сусідів для JGraphT
//            for (Integer neighbor : neighbors) {
//                if (!path.contains(neighbor)) {
//                    findAllPaths(graph, neighbor, end, new ArrayList<>(path), allPaths); // Створюємо новий список для кожної рекурсії
//                }
//            }
//        }
//    }
//
//    // Функція для обчислення ваги шляху
//    public static double calculatePathWeight(Graph<Integer, DefaultWeightedEdge> graph, List<Integer> path) {
//        double weight = 0;
//        for (int i = 0; i < path.size() - 1; i++) {
//            DefaultWeightedEdge edge = graph.getEdge(path.get(i), path.get(i + 1));
//            if (edge != null) {
//                weight += graph.getEdgeWeight(edge);
//            }
//            //  else {
//            //      System.out.println("Warning: Edge not found between " + path.get(i) + " and " + path.get(i + 1));
//            //  }
//        }
//        return weight;
//    }
//
//    //Клас Pair для зберігання пари <вага, шлях>
//    private static class Pair<K, V> {
//        private final K key;
//        private final V value;
//
//        public Pair(K key, V value) {
//            this.key = key;
//            this.value = value;
//        }
//
//        public K getKey() {
//            return key;
//        }
//
//        public V getValue() {
//            return value;
//        }
//    }
//}
//
////Клас Graphs, якого немає в jgrapht, тому реалізуємо самі
//class Graphs {
//    /**
//     * Returns the set of neighbors of a vertex in a graph.
//     *
//     * @param <V> the graph vertex type.
//     * @param <E> the graph edge type.
//     * @param graph the graph.
//     * @param vertex the vertex whose neighbors are sought.
//     * @return the set of neighbors of the vertex in the graph.
//     * @throws NullPointerException if any of the arguments is {@code null}.
//     */
//    public static <V, E> Set<V> neighborSetOf(Graph<V, E> graph, V vertex) {
//        Objects.requireNonNull(graph, "Graph must not be null");
//        Objects.requireNonNull(vertex, "Vertex must not be null");
//
//        Set<V> neighbors = new HashSet<>();
//        for (E edge : graph.edgesOf(vertex)) {
//            V source = graph.getEdgeSource(edge);
//            V target = graph.getEdgeTarget(edge);
//            if (source.equals(vertex)) {
//                neighbors.add(target);
//            } else {
//                neighbors.add(source);
//            }
//        }
//        return neighbors;
//    }
//}
