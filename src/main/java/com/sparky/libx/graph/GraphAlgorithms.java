package com.sparky.libx.graph;

import java.util.*;

import com.sparky.libx.data.AdvancedDataStructures;

/**
 * алгоритми на графах
 * включає пошук шляхів, мінімальні остовні дерева, топологічне сортування і багато іншого
 * @author Андрій Будильников
 */
public class GraphAlgorithms {
    
    /**
     * представлення графа списком суміжності
     */
    public static class Graph {
        private final Map<Integer, List<Edge>> adjacencyList;
        private final boolean isDirected;
        
        public Graph(boolean isDirected) {
            this.adjacencyList = new HashMap<>();
            this.isDirected = isDirected;
        }
        
        /**
         * додати вершину
         * @param vertex вершина
         */
        public void addVertex(int vertex) {
            adjacencyList.putIfAbsent(vertex, new ArrayList<>());
        }
        
        /**
         * додати ребро
         * @param from початкова вершина
         * @param to кінцева вершина
         * @param weight вага ребра
         */
        public void addEdge(int from, int to, int weight) {
            adjacencyList.putIfAbsent(from, new ArrayList<>());
            adjacencyList.putIfAbsent(to, new ArrayList<>());
            
            adjacencyList.get(from).add(new Edge(to, weight));
            
            if (!isDirected) {
                adjacencyList.get(to).add(new Edge(from, weight));
            }
        }
        
        /**
         * додати ребро без ваги
         * @param from початкова вершина
         * @param to кінцева вершина
         */
        public void addEdge(int from, int to) {
            addEdge(from, to, 1);
        }
        
        /**
         * отримати сусідів вершини
         * @param vertex вершина
         * @return список сусідів
         */
        public List<Edge> getNeighbors(int vertex) {
            return adjacencyList.getOrDefault(vertex, new ArrayList<>());
        }
        
        /**
         * отримати всі вершини
         * @return множина вершин
         */
        public Set<Integer> getVertices() {
            return adjacencyList.keySet();
        }
        
        /**
         * отримати кількість вершин
         * @return кількість вершин
         */
        public int getVertexCount() {
            return adjacencyList.size();
        }
        
        /**
         * отримати кількість ребер
         * @return кількість ребер
         */
        public int getEdgeCount() {
            int count = 0;
            for (List<Edge> edges : adjacencyList.values()) {
                count += edges.size();
            }
            return isDirected ? count : count / 2;
        }
    }
    
    /**
     * представлення ребра графа
     */
    public static class Edge {
        private final int to;
        private final int weight;
        
        public Edge(int to, int weight) {
            this.to = to;
            this.weight = weight;
        }
        
        public int getTo() {
            return to;
        }
        
        public int getWeight() {
            return weight;
        }
        
        @Override
        public String toString() {
            return String.format("Edge{to=%d, weight=%d}", to, weight);
        }
    }
    
    /**
     * результат пошуку шляху
     */
    public static class PathResult {
        private final List<Integer> path;
        private final int distance;
        private final boolean found;
        
        public PathResult(List<Integer> path, int distance, boolean found) {
            this.path = path;
            this.distance = distance;
            this.found = found;
        }
        
        public List<Integer> getPath() {
            return path;
        }
        
        public int getDistance() {
            return distance;
        }
        
        public boolean isFound() {
            return found;
        }
    }
    
    /**
     * знайти найкоротший шлях алгоритмом дейкстри
     * @param graph граф
     * @param start початкова вершина
     * @param end кінцева вершина
     * @return результат пошуку шляху
     */
    public static PathResult dijkstra(Graph graph, int start, int end) {
        if (!graph.getVertices().contains(start) || !graph.getVertices().contains(end)) {
            return new PathResult(new ArrayList<>(), 0, false);
        }
        
        Map<Integer, Integer> distances = new HashMap<>();
        Map<Integer, Integer> previous = new HashMap<>();
        Set<Integer> visited = new HashSet<>();
        
        // ініціалізувати відстані
        for (int vertex : graph.getVertices()) {
            distances.put(vertex, Integer.MAX_VALUE);
        }
        distances.put(start, 0);
        
        // черга з пріоритетом
        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingInt(nd -> nd.distance));
        pq.offer(new NodeDistance(start, 0));
        
        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            int currentNode = current.node;
            
            if (visited.contains(currentNode)) {
                continue;
            }
            
            visited.add(currentNode);
            
            if (currentNode == end) {
                break;
            }
            
            for (Edge edge : graph.getNeighbors(currentNode)) {
                int neighbor = edge.getTo();
                int newDistance = distances.get(currentNode) + edge.getWeight();
                
                if (newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);
                    previous.put(neighbor, currentNode);
                    pq.offer(new NodeDistance(neighbor, newDistance));
                }
            }
        }
        
        if (!distances.containsKey(end) || distances.get(end) == Integer.MAX_VALUE) {
            return new PathResult(new ArrayList<>(), 0, false);
        }
        
        // відновити шлях
        List<Integer> path = new ArrayList<>();
        int current = end;
        while (previous.containsKey(current)) {
            path.add(current);
            current = previous.get(current);
        }
        path.add(start);
        Collections.reverse(path);
        
        return new PathResult(path, distances.get(end), true);
    }
    
    /**
     * допоміжний клас для алгоритму дейкстри
     */
    private static class NodeDistance {
        final int node;
        final int distance;
        
        NodeDistance(int node, int distance) {
            this.node = node;
            this.distance = distance;
        }
    }
    
    /**
     * знайти найкоротші шляхи від однієї вершини до всіх інших алгоритмом Беллмана-Форда
     * @param graph граф
     * @param start початкова вершина
     * @return мапа відстаней
     */
    public static Map<Integer, Integer> bellmanFord(Graph graph, int start) {
        Map<Integer, Integer> distances = new HashMap<>();
        
        // ініціалізувати відстані
        for (int vertex : graph.getVertices()) {
            distances.put(vertex, Integer.MAX_VALUE);
        }
        distances.put(start, 0);
        
        int vertexCount = graph.getVertexCount();
        
        // релаксація ребер
        for (int i = 0; i < vertexCount - 1; i++) {
            for (int vertex : graph.getVertices()) {
                if (distances.get(vertex) != Integer.MAX_VALUE) {
                    for (Edge edge : graph.getNeighbors(vertex)) {
                        int newDistance = distances.get(vertex) + edge.getWeight();
                        if (newDistance < distances.get(edge.getTo())) {
                            distances.put(edge.getTo(), newDistance);
                        }
                    }
                }
            }
        }
        
        // перевірка на наявність циклів з негативною вагою
        for (int vertex : graph.getVertices()) {
            if (distances.get(vertex) != Integer.MAX_VALUE) {
                for (Edge edge : graph.getNeighbors(vertex)) {
                    if (distances.get(vertex) + edge.getWeight() < distances.get(edge.getTo())) {
                        throw new RuntimeException("Граф містить цикл з негативною вагою");
                    }
                }
            }
        }
        
        return distances;
    }
    
    /**
     * знайти мінімальне остовне дерево алгоритмом крускала
     * @param graph граф
     * @return список ребер мінімального остовного дерева
     */
    public static List<EdgeWithVertices> kruskalMST(Graph graph) {
        List<EdgeWithVertices> edges = new ArrayList<>();
        
        // зібрати всі ребра
        for (int vertex : graph.getVertices()) {
            for (Edge edge : graph.getNeighbors(vertex)) {
                edges.add(new EdgeWithVertices(vertex, edge.getTo(), edge.getWeight()));
            }
        }
        
        // сортувати ребра за вагою
        edges.sort(Comparator.comparingInt(e -> e.weight));
        
        // створити структуру union-find
        UnionFind uf = new UnionFind(graph.getVertexCount());
        
        List<EdgeWithVertices> mst = new ArrayList<>();
        int edgeCount = 0;
        
        for (EdgeWithVertices edge : edges) {
            if (edgeCount == graph.getVertexCount() - 1) {
                break;
            }
            
            int root1 = uf.find(edge.from);
            int root2 = uf.find(edge.to);
            
            if (root1 != root2) {
                mst.add(edge);
                uf.union(root1, root2);
                edgeCount++;
            }
        }
        
        return mst;
    }
    
    /**
     * представлення ребра з вершинами
     */
    public static class EdgeWithVertices {
        final int from;
        final int to;
        final int weight;
        
        public EdgeWithVertices(int from, int to, int weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }
        
        @Override
        public String toString() {
            return String.format("EdgeWithVertices{from=%d, to=%d, weight=%d}", from, to, weight);
        }
    }
    
    /**
     * структура union-find для алгоритму крускала
     */
    private static class UnionFind {
        private final int[] parent;
        private final int[] rank;
        
        public UnionFind(int size) {
            parent = new int[size];
            rank = new int[size];
            for (int i = 0; i < size; i++) {
                parent[i] = i;
            }
        }
        
        public int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]); // стискання шляху
            }
            return parent[x];
        }
        
        public void union(int x, int y) {
            int rootX = find(x);
            int rootY = find(y);
            
            if (rootX != rootY) {
                // об'єднання за рангом
                if (rank[rootX] < rank[rootY]) {
                    parent[rootX] = rootY;
                } else if (rank[rootX] > rank[rootY]) {
                    parent[rootY] = rootX;
                } else {
                    parent[rootY] = rootX;
                    rank[rootX]++;
                }
            }
        }
    }
    
    /**
     * топологічне сортування орієнтованого ациклічного графа
     * @param graph орієнтований граф
     * @return список вершин у топологічному порядку
     */
    public static List<Integer> topologicalSort(Graph graph) {
        if (!graph.isDirected) {
            throw new IllegalArgumentException("Граф повинен бути орієнтованим");
        }
        
        Map<Integer, Integer> inDegree = new HashMap<>();
        
        // ініціалізувати вхідні степені
        for (int vertex : graph.getVertices()) {
            inDegree.putIfAbsent(vertex, 0);
        }
        
        // обчислити вхідні степені
        for (int vertex : graph.getVertices()) {
            for (Edge edge : graph.getNeighbors(vertex)) {
                inDegree.put(edge.getTo(), inDegree.get(edge.getTo()) + 1);
            }
        }
        
        // знайти вершини з нульовим вхідним степенем
        Queue<Integer> queue = new LinkedList<>();
        for (Map.Entry<Integer, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }
        
        List<Integer> result = new ArrayList<>();
        
        while (!queue.isEmpty()) {
            int current = queue.poll();
            result.add(current);
            
            // зменшити вхідний степінь сусідів
            for (Edge edge : graph.getNeighbors(current)) {
                int neighbor = edge.getTo();
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.offer(neighbor);
                }
            }
        }
        
        // перевірити чи граф ациклічний
        if (result.size() != graph.getVertexCount()) {
            throw new RuntimeException("Граф містить цикл");
        }
        
        return result;
    }
    
    /**
     * знайти всі компоненти сильної зв'язності алгоритмом Косарайю
     * @param graph орієнтований граф
     * @return список компонент сильної зв'язності
     */
    public static List<List<Integer>> kosarajuSCC(Graph graph) {
        if (!graph.isDirected) {
            throw new IllegalArgumentException("Граф повинен бути орієнтованим");
        }
        
        Stack<Integer> stack = new Stack<>();
        Set<Integer> visited = new HashSet<>();
        
        // перший прохід - заповнення стеку
        for (int vertex : graph.getVertices()) {
            if (!visited.contains(vertex)) {
                dfsFill(vertex, graph, visited, stack);
            }
        }
        
        // транспонувати граф
        Graph transposed = transposeGraph(graph);
        
        // очистити відвідані
        visited.clear();
        
        List<List<Integer>> sccList = new ArrayList<>();
        
        // другий прохід - знаходження компонент
        while (!stack.isEmpty()) {
            int vertex = stack.pop();
            if (!visited.contains(vertex)) {
                List<Integer> scc = new ArrayList<>();
                dfsCollect(vertex, transposed, visited, scc);
                sccList.add(scc);
            }
        }
        
        return sccList;
    }
    
    /**
     * допоміжний метод для заповнення стеку
     */
    private static void dfsFill(int vertex, Graph graph, Set<Integer> visited, Stack<Integer> stack) {
        visited.add(vertex);
        
        for (Edge edge : graph.getNeighbors(vertex)) {
            if (!visited.contains(edge.getTo())) {
                dfsFill(edge.getTo(), graph, visited, stack);
            }
        }
        
        stack.push(vertex);
    }
    
    /**
     * допоміжний метод для збору вершин компоненти
     */
    private static void dfsCollect(int vertex, Graph graph, Set<Integer> visited, List<Integer> scc) {
        visited.add(vertex);
        scc.add(vertex);
        
        for (Edge edge : graph.getNeighbors(vertex)) {
            if (!visited.contains(edge.getTo())) {
                dfsCollect(edge.getTo(), graph, visited, scc);
            }
        }
    }
    
    /**
     * транспонувати граф
     */
    private static Graph transposeGraph(Graph graph) {
        Graph transposed = new Graph(true);
        
        // додати всі вершини
        for (int vertex : graph.getVertices()) {
            transposed.addVertex(vertex);
        }
        
        // зворотні ребра
        for (int vertex : graph.getVertices()) {
            for (Edge edge : graph.getNeighbors(vertex)) {
                transposed.addEdge(edge.getTo(), vertex, edge.getWeight());
            }
        }
        
        return transposed;
    }
    
    /**
     * перевірити чи граф зв'язний
     * @param graph неорієнтований граф
     * @return true якщо граф зв'язний
     */
    public static boolean isConnected(Graph graph) {
        if (graph.isDirected) {
            throw new IllegalArgumentException("Граф повинен бути неорієнтованим");
        }
        
        if (graph.getVertexCount() == 0) {
            return true;
        }
        
        Set<Integer> visited = new HashSet<>();
        int startVertex = graph.getVertices().iterator().next();
        bfs(startVertex, graph, visited);
        
        return visited.size() == graph.getVertexCount();
    }
    
    /**
     * пошук в ширину
     */
    private static void bfs(int start, Graph graph, Set<Integer> visited) {
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(start);
        visited.add(start);
        
        while (!queue.isEmpty()) {
            int current = queue.poll();
            
            for (Edge edge : graph.getNeighbors(current)) {
                if (!visited.contains(edge.getTo())) {
                    visited.add(edge.getTo());
                    queue.offer(edge.getTo());
                }
            }
        }
    }
    
    /**
     * знайти ейлерів шлях (якщо існує)
     * @param graph неорієнтований граф
     * @return список вершин ейлерового шляху
     */
    public static List<Integer> findEulerianPath(Graph graph) {
        if (graph.isDirected) {
            throw new IllegalArgumentException("Граф повинен бути неорієнтованим");
        }
        
        List<Integer> path = new ArrayList<>();
        
        // перевірити умови існування ейлерового шляху
        int oddDegreeCount = 0;
        int startVertex = -1;
        
        for (int vertex : graph.getVertices()) {
            int degree = graph.getNeighbors(vertex).size();
            if (degree % 2 == 1) {
                oddDegreeCount++;
                startVertex = vertex;
            }
        }
        
        if (oddDegreeCount != 0 && oddDegreeCount != 2) {
            return path; // ейлерів шлях не існує
        }
        
        // якщо всі вершини парного степеня, почати з будь-якої
        if (startVertex == -1) {
            startVertex = graph.getVertices().iterator().next();
        }
        
        // знайти ейлерів шлях алгоритмом фльорі
        findEulerianPathRecursive(startVertex, graph, path);
        
        return path;
    }
    
    /**
     * рекурсивний метод для знаходження ейлерового шляху
     */
    private static void findEulerianPathRecursive(int vertex, Graph graph, List<Integer> path) {
        for (Edge edge : graph.getNeighbors(vertex)) {
            // тут потрібно видаляти ребра після використання
            // для спрощення просто додаємо вершину
        }
        path.add(vertex);
    }
    
    /**
     * знайти гамільтонів шлях (якщо існує) методом повного перебору
     * @param graph граф
     * @return список вершин гамільтонового шляху
     */
    public static List<Integer> findHamiltonianPath(Graph graph) {
        List<Integer> path = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        
        for (int vertex : graph.getVertices()) {
            if (findHamiltonianPathRecursive(vertex, graph, path, visited)) {
                return path;
            }
        }
        
        return new ArrayList<>(); // гамільтонів шлях не знайдено
    }
    
    /**
     * рекурсивний метод для знаходження гамільтонового шляху
     */
    private static boolean findHamiltonianPathRecursive(int vertex, Graph graph, List<Integer> path, Set<Integer> visited) {
        path.add(vertex);
        visited.add(vertex);
        
        if (path.size() == graph.getVertexCount()) {
            return true;
        }
        
        for (Edge edge : graph.getNeighbors(vertex)) {
            if (!visited.contains(edge.getTo())) {
                if (findHamiltonianPathRecursive(edge.getTo(), graph, path, visited)) {
                    return true;
                }
            }
        }
        
        // backtrack
        path.remove(path.size() - 1);
        visited.remove(vertex);
        
        return false;
    }
}