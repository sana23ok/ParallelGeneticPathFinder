//package org.example;
//
//import java.util.*;
//import java.util.concurrent.*;
//import java.util.stream.*;
//import static org.example.Constants.*;
//
//public class ShortestPathGAIsland {
//
//    private final int[][] graph;
//    private final int startNode = 0;
//    private final int endNode = NUM_NODES / 2 + 1;
//    private final Random random = new Random();
//    private final static int MIGRATION_INTERVAL = 25;
//
//    private final int numIslands = Runtime.getRuntime().availableProcessors();
//    private final ExecutorService executor = ForkJoinPool.commonPool();
//
//    private final List<ConcurrentLinkedQueue<List<Integer>>> islands = new ArrayList<>();
//    private final Map<List<Integer>, Integer> fitnessCache = new ConcurrentHashMap<>();
//    private final ConcurrentLinkedQueue<List<Integer>> migrationPool = new ConcurrentLinkedQueue<>();
//
//    public ShortestPathGAIsland(int[][] graph) {
//        this.graph = graph;
//        for (int i = 0; i < numIslands; i++) {
//            islands.add(new ConcurrentLinkedQueue<>());
//        }
//    }
//
//    public List<Integer> findShortestPath() throws InterruptedException {
//        CountDownLatch latch = new CountDownLatch(numIslands);
//
//        for (int i = 0; i < numIslands; i++) {
//            final int islandIndex = i;
//            executor.submit(() -> {
//                runIsland(islandIndex);
//                latch.countDown();
//            });
//        }
//
//        latch.await();
//
//        return islands.stream()
//                .flatMap(ConcurrentLinkedQueue::stream)
//                .min(Comparator.comparingInt(this::calculateFitness))
//                .orElse(null);
//    }
//
//    private void runIsland(int islandIndex) {
//        ConcurrentLinkedQueue<List<Integer>> population = initializePopulation();
//
//        for (int gen = 0; gen < GENERATIONS; gen++) {
//            population = evolvePopulation(population);
//
//            if (gen % MIGRATION_INTERVAL == 0) {
//                migrate(population);
//                receiveMigrants(population);
//            }
//        }
//
//        islands.set(islandIndex, population);
//    }
//
//    private ConcurrentLinkedQueue<List<Integer>> initializePopulation() {
//        return IntStream.range(0, POPULATION_SIZE / numIslands)
//                .mapToObj(i -> {
//                    List<Integer> path;
//                    do {
//                        path = generateRandomPath();
//                    } while (!isValidPath(path));
//                    return path;
//                }).collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
//    }
//
//    private List<Integer> generateRandomPath() {
//        List<Integer> path = new ArrayList<>();
//        path.add(startNode);
//        int currentNode = startNode;
//        while (currentNode != endNode && path.size() < graph.length) {
//            List<Integer> neighbors = getNeighbors(currentNode);
//            if (neighbors.isEmpty()) break;
//            int nextNode = neighbors.get(random.nextInt(neighbors.size()));
//            path.add(nextNode);
//            currentNode = nextNode;
//        }
//        if (path.get(path.size() - 1) != endNode) path.add(endNode);
//        return path;
//    }
//
//    private List<Integer> getNeighbors(int node) {
//        List<Integer> neighbors = new ArrayList<>();
//        for (int i = 0; i < graph.length; i++) {
//            if (graph[node][i] > 0) neighbors.add(i);
//        }
//        return neighbors;
//    }
//
//    private boolean isValidPath(List<Integer> path) {
//        if (path.get(0) != startNode || path.get(path.size() - 1) != endNode) return false;
//        for (int i = 0; i < path.size() - 1; i++) {
//            if (graph[path.get(i)][path.get(i + 1)] == 0) return false;
//        }
//        return true;
//    }
//
//    private ConcurrentLinkedQueue<List<Integer>> evolvePopulation(ConcurrentLinkedQueue<List<Integer>> population) {
//        Map<List<Integer>, Integer> evaluated = evaluatePopulation(population);
//        return IntStream.range(0, population.size())
//                .mapToObj(i -> {
//                    List<Integer> parent1 = tournamentSelection(population);
//                    List<Integer> parent2 = tournamentSelection(population);
//                    List<Integer> child = crossover(parent1, parent2);
//                    mutate(child);
//                    return isValidPath(child) ? child : parent1; // Замінити некоректне дитя на одне з батьків.
//                }).collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
//    }
//
//    private Map<List<Integer>, Integer> evaluatePopulation(ConcurrentLinkedQueue<List<Integer>> population) {
//        return population.parallelStream()
//                .collect(Collectors.toConcurrentMap(
//                        path -> path,
//                        this::calculateFitness
//                ));
//    }
//
//    private List<Integer> tournamentSelection(ConcurrentLinkedQueue<List<Integer>> population) {
//        return IntStream.range(0, TOURNAMENT_SIZE)
//                .mapToObj(i -> population.stream()
//                        .skip(random.nextInt(population.size()))
//                        .findFirst().orElse(null))
//                .min(Comparator.comparingInt(this::calculateFitness))
//                .orElse(null);
//    }
//
//    private List<Integer> crossover(List<Integer> parent1, List<Integer> parent2) {
//        Set<Integer> visited = new HashSet<>();
//        List<Integer> child = new ArrayList<>();
//        int crossoverPoint = random.nextInt(Math.min(parent1.size(), parent2.size()));
//        for (int i = 0; i < crossoverPoint; i++) {
//            child.add(parent1.get(i));
//            visited.add(parent1.get(i));
//        }
//        for (int i = crossoverPoint; i < parent2.size(); i++) {
//            if (visited.add(parent2.get(i))) {
//                child.add(parent2.get(i));
//            }
//        }
//        return child;
//    }
//
//    private void mutate(List<Integer> path) {
//        if (random.nextDouble() < MUTATION_RATE && path.size() > 2) {
//            int index1 = 1 + random.nextInt(path.size() - 2);
//            int index2 = 1 + random.nextInt(path.size() - 2);
//            Collections.swap(path, index1, index2);
//        }
//    }
//
//    private synchronized int calculateFitness(List<Integer> path) {
//        return fitnessCache.computeIfAbsent(path, p -> {
//            int fitness = 0;
//            for (int i = 0; i < p.size() - 1; i++) {
//                fitness += graph[p.get(i)][p.get(i + 1)];
//            }
//            return fitness;
//        });
//    }
//
//    private void migrate(ConcurrentLinkedQueue<List<Integer>> population) {
//        population.stream()
//                .sorted(Comparator.comparingInt(this::calculateFitness))
//                .limit(3)
//                .forEach(migrationPool::offer);
//    }
//
//    private void receiveMigrants(ConcurrentLinkedQueue<List<Integer>> population) {
//        IntStream.range(0, 3).forEach(i -> {
//            List<Integer> migrant = migrationPool.poll();
//            if (migrant != null) population.offer(migrant);
//        });
//    }
//
//    public static void run(int[][] graph) throws InterruptedException {
//        ShortestPathGAIsland ga = new ShortestPathGAIsland(graph);
//        List<Integer> shortestPath = ga.findShortestPath();
//
//        System.out.println("Fitness: " + ga.calculateFitness(shortestPath));
//        System.out.println("Shortest path: " + shortestPath);
//
//        if (NUM_NODES <= 20) {
//            GraphVisualizer visualizer = new GraphVisualizer(graph);
//            visualizer.showGraph(shortestPath);
//        }
//    }
//}
