package com.sparky.libx.data;

import com.sparky.libx.math.Vector3D;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Advanced Data Structures and Algorithms Framework for Minecraft Plugins
 * Provides implementations of advanced data structures, graph algorithms, and computational geometry
 * 
 * @author Андрій Будильников
 */
public class AdvancedDataStructures {
    
    /**
     * Represents a balanced binary search tree (AVL Tree)
     */
    public static class AVLTree<T extends Comparable<T>> {
        private Node<T> root;
        
        private static class Node<T> {
            T data;
            Node<T> left, right;
            int height;
            
            Node(T data) {
                this.data = data;
                this.height = 1;
            }
        }
        
        /**
         * Inserts a value into the tree
         */
        public void insert(T data) {
            root = insert(root, data);
        }
        
        private Node<T> insert(Node<T> node, T data) {
            // Perform normal BST insertion
            if (node == null) {
                return new Node<>(data);
            }
            
            int cmp = data.compareTo(node.data);
            if (cmp < 0) {
                node.left = insert(node.left, data);
            } else if (cmp > 0) {
                node.right = insert(node.right, data);
            } else {
                // Duplicate keys not allowed
                return node;
            }
            
            // Update height of this ancestor node
            node.height = 1 + Math.max(height(node.left), height(node.right));
            
            // Get the balance factor of this ancestor node
            int balance = getBalance(node);
            
            // If this node becomes unbalanced, then there are 4 cases
            
            // Left Left Case
            if (balance > 1 && data.compareTo(node.left.data) < 0) {
                return rightRotate(node);
            }
            
            // Right Right Case
            if (balance < -1 && data.compareTo(node.right.data) > 0) {
                return leftRotate(node);
            }
            
            // Left Right Case
            if (balance > 1 && data.compareTo(node.left.data) > 0) {
                node.left = leftRotate(node.left);
                return rightRotate(node);
            }
            
            // Right Left Case
            if (balance < -1 && data.compareTo(node.right.data) < 0) {
                node.right = rightRotate(node.right);
                return leftRotate(node);
            }
            
            // Return the (unchanged) node pointer
            return node;
        }
        
        /**
         * Deletes a value from the tree
         */
        public void delete(T data) {
            root = delete(root, data);
        }
        
        private Node<T> delete(Node<T> node, T data) {
            // Perform standard BST delete
            if (node == null) {
                return node;
            }
            
            int cmp = data.compareTo(node.data);
            if (cmp < 0) {
                node.left = delete(node.left, data);
            } else if (cmp > 0) {
                node.right = delete(node.right, data);
            } else {
                // Node with only one child or no child
                if ((node.left == null) || (node.right == null)) {
                    Node<T> temp = (node.left != null) ? node.left : node.right;
                    
                    // No child case
                    if (temp == null) {
                        temp = node;
                        node = null;
                    } else { // One child case
                        node = temp; // Copy the contents of the non-empty child
                    }
                } else {
                    // Node with two children: Get the inorder successor (smallest
                    // in the right subtree)
                    Node<T> temp = minValueNode(node.right);
                    
                    // Copy the inorder successor's data to this node
                    node.data = temp.data;
                    
                    // Delete the inorder successor
                    node.right = delete(node.right, temp.data);
                }
            }
            
            // If the tree had only one node then return
            if (node == null) {
                return node;
            }
            
            // Update height of the current node
            node.height = Math.max(height(node.left), height(node.right)) + 1;
            
            // Get the balance factor of this node
            int balance = getBalance(node);
            
            // If this node becomes unbalanced, then there are 4 cases
            
            // Left Left Case
            if (balance > 1 && getBalance(node.left) >= 0) {
                return rightRotate(node);
            }
            
            // Left Right Case
            if (balance > 1 && getBalance(node.left) < 0) {
                node.left = leftRotate(node.left);
                return rightRotate(node);
            }
            
            // Right Right Case
            if (balance < -1 && getBalance(node.right) <= 0) {
                return leftRotate(node);
            }
            
            // Right Left Case
            if (balance < -1 && getBalance(node.right) > 0) {
                node.right = rightRotate(node.right);
                return leftRotate(node);
            }
            
            return node;
        }
        
        /**
         * Searches for a value in the tree
         */
        public boolean search(T data) {
            return search(root, data);
        }
        
        private boolean search(Node<T> node, T data) {
            if (node == null) {
                return false;
            }
            
            int cmp = data.compareTo(node.data);
            if (cmp < 0) {
                return search(node.left, data);
            } else if (cmp > 0) {
                return search(node.right, data);
            } else {
                return true;
            }
        }
        
        /**
         * Gets the minimum value node
         */
        private Node<T> minValueNode(Node<T> node) {
            Node<T> current = node;
            while (current.left != null) {
                current = current.left;
            }
            return current;
        }
        
        /**
         * Gets the height of a node
         */
        private int height(Node<T> node) {
            return (node == null) ? 0 : node.height;
        }
        
        /**
         * Gets the balance factor of a node
         */
        private int getBalance(Node<T> node) {
            return (node == null) ? 0 : height(node.left) - height(node.right);
        }
        
        /**
         * Performs a right rotation
         */
        private Node<T> rightRotate(Node<T> y) {
            Node<T> x = y.left;
            Node<T> T2 = x.right;
            
            // Perform rotation
            x.right = y;
            y.left = T2;
            
            // Update heights
            y.height = Math.max(height(y.left), height(y.right)) + 1;
            x.height = Math.max(height(x.left), height(x.right)) + 1;
            
            // Return new root
            return x;
        }
        
        /**
         * Performs a left rotation
         */
        private Node<T> leftRotate(Node<T> x) {
            Node<T> y = x.right;
            Node<T> T2 = y.left;
            
            // Perform rotation
            y.left = x;
            x.right = T2;
            
            // Update heights
            x.height = Math.max(height(x.left), height(x.right)) + 1;
            y.height = Math.max(height(y.left), height(y.right)) + 1;
            
            // Return new root
            return y;
        }
        
        /**
         * Performs an inorder traversal
         */
        public List<T> inorderTraversal() {
            List<T> result = new ArrayList<>();
            inorderTraversal(root, result);
            return result;
        }
        
        private void inorderTraversal(Node<T> node, List<T> result) {
            if (node != null) {
                inorderTraversal(node.left, result);
                result.add(node.data);
                inorderTraversal(node.right, result);
            }
        }
        
        /**
         * Checks if the tree is balanced
         */
        public boolean isBalanced() {
            return isBalanced(root);
        }
        
        private boolean isBalanced(Node<T> node) {
            if (node == null) {
                return true;
            }
            
            int balance = getBalance(node);
            if (Math.abs(balance) > 1) {
                return false;
            }
            
            return isBalanced(node.left) && isBalanced(node.right);
        }
    }
    
    /**
     * Represents a red-black tree
     */
    public static class RedBlackTree<T extends Comparable<T>> {
        private static final boolean RED = true;
        private static final boolean BLACK = false;
        
        private Node<T> root;
        
        private static class Node<T> {
            T data;
            Node<T> left, right;
            boolean color;
            int size;
            
            Node(T data, boolean color, int size) {
                this.data = data;
                this.color = color;
                this.size = size;
            }
        }
        
        /**
         * Inserts a value into the tree
         */
        public void insert(T data) {
            root = insert(root, data);
            root.color = BLACK;
        }
        
        private Node<T> insert(Node<T> node, T data) {
            if (node == null) {
                return new Node<>(data, RED, 1);
            }
            
            int cmp = data.compareTo(node.data);
            if (cmp < 0) {
                node.left = insert(node.left, data);
            } else if (cmp > 0) {
                node.right = insert(node.right, data);
            } else {
                node.data = data; // Update existing key
            }
            
            // Fix-up operations
            if (isRed(node.right) && !isRed(node.left)) {
                node = rotateLeft(node);
            }
            if (isRed(node.left) && isRed(node.left.left)) {
                node = rotateRight(node);
            }
            if (isRed(node.left) && isRed(node.right)) {
                flipColors(node);
            }
            
            node.size = size(node.left) + size(node.right) + 1;
            return node;
        }
        
        /**
         * Searches for a value in the tree
         */
        public boolean search(T data) {
            return search(root, data);
        }
        
        private boolean search(Node<T> node, T data) {
            if (node == null) {
                return false;
            }
            
            int cmp = data.compareTo(node.data);
            if (cmp < 0) {
                return search(node.left, data);
            } else if (cmp > 0) {
                return search(node.right, data);
            } else {
                return true;
            }
        }
        
        /**
         * Checks if a node is red
         */
        private boolean isRed(Node<T> node) {
            if (node == null) {
                return false;
            }
            return node.color == RED;
        }
        
        /**
         * Gets the size of a subtree
         */
        private int size(Node<T> node) {
            if (node == null) {
                return 0;
            }
            return node.size;
        }
        
        /**
         * Performs a left rotation
         */
        private Node<T> rotateLeft(Node<T> node) {
            Node<T> x = node.right;
            node.right = x.left;
            x.left = node;
            x.color = node.color;
            node.color = RED;
            x.size = node.size;
            node.size = size(node.left) + size(node.right) + 1;
            return x;
        }
        
        /**
         * Performs a right rotation
         */
        private Node<T> rotateRight(Node<T> node) {
            Node<T> x = node.left;
            node.left = x.right;
            x.right = node;
            x.color = node.color;
            node.color = RED;
            x.size = node.size;
            node.size = size(node.left) + size(node.right) + 1;
            return x;
        }
        
        /**
         * Flips the colors of a node and its children
         */
        private void flipColors(Node<T> node) {
            node.color = !node.color;
            node.left.color = !node.left.color;
            node.right.color = !node.right.color;
        }
        
        /**
         * Performs an inorder traversal
         */
        public List<T> inorderTraversal() {
            List<T> result = new ArrayList<>();
            inorderTraversal(root, result);
            return result;
        }
        
        private void inorderTraversal(Node<T> node, List<T> result) {
            if (node != null) {
                inorderTraversal(node.left, result);
                result.add(node.data);
                inorderTraversal(node.right, result);
            }
        }
    }
    
    /**
     * Represents a B-tree
     */
    public static class BTree<T extends Comparable<T>> {
        private static final int DEFAULT_DEGREE = 3;
        private final int degree;
        private Node<T> root;
        
        public BTree() {
            this(DEFAULT_DEGREE);
        }
        
        public BTree(int degree) {
            this.degree = degree;
            this.root = new Node<>(true);
        }
        
        private static class Node<T> {
            boolean leaf;
            List<T> keys;
            List<Node<T>> children;
            
            Node(boolean leaf) {
                this.leaf = leaf;
                this.keys = new ArrayList<>();
                this.children = new ArrayList<>();
            }
        }
        
        /**
         * Inserts a value into the tree
         */
        public void insert(T data) {
            Node<T> r = root;
            
            // If root is full, split it
            if (r.keys.size() == 2 * degree - 1) {
                Node<T> s = new Node<>(false);
                root = s;
                s.children.add(r);
                splitChild(s, 0, r);
                insertNonFull(s, data);
            } else {
                insertNonFull(r, data);
            }
        }
        
        private void insertNonFull(Node<T> node, T data) {
            int i = node.keys.size() - 1;
            
            if (node.leaf) {
                // Insert into leaf node
                node.keys.add(null); // Make space for new key
                while (i >= 0 && data.compareTo(node.keys.get(i)) < 0) {
                    node.keys.set(i + 1, node.keys.get(i));
                    i--;
                }
                node.keys.set(i + 1, data);
            } else {
                // Insert into internal node
                while (i >= 0 && data.compareTo(node.keys.get(i)) < 0) {
                    i--;
                }
                i++;
                
                if (node.children.get(i).keys.size() == 2 * degree - 1) {
                    splitChild(node, i, node.children.get(i));
                    if (data.compareTo(node.keys.get(i)) > 0) {
                        i++;
                    }
                }
                insertNonFull(node.children.get(i), data);
            }
        }
        
        private void splitChild(Node<T> parent, int i, Node<T> fullChild) {
            Node<T> z = new Node<>(fullChild.leaf);
            Node<T> y = fullChild;
            
            // Copy the second half of y's keys to z
            for (int j = 0; j < degree - 1; j++) {
                z.keys.add(y.keys.get(j + degree));
            }
            
            // Copy the second half of y's children to z (if not leaf)
            if (!y.leaf) {
                for (int j = 0; j < degree; j++) {
                    z.children.add(y.children.get(j + degree));
                }
            }
            
            // Reduce y to have only the first half of keys
            while (y.keys.size() > degree - 1) {
                y.keys.remove(y.keys.size() - 1);
            }
            while (y.children.size() > degree) {
                y.children.remove(y.children.size() - 1);
            }
            
            // Insert z as a child of parent
            parent.children.add(i + 1, z);
            
            // Move the middle key of y to parent
            parent.keys.add(i, y.keys.get(degree - 1));
            y.keys.remove(degree - 1);
        }
        
        /**
         * Searches for a value in the tree
         */
        public boolean search(T data) {
            return search(root, data);
        }
        
        private boolean search(Node<T> node, T data) {
            int i = 0;
            while (i < node.keys.size() && data.compareTo(node.keys.get(i)) > 0) {
                i++;
            }
            
            if (i < node.keys.size() && data.compareTo(node.keys.get(i)) == 0) {
                return true;
            }
            
            if (node.leaf) {
                return false;
            }
            
            return search(node.children.get(i), data);
        }
        
        /**
         * Performs an inorder traversal
         */
        public List<T> inorderTraversal() {
            List<T> result = new ArrayList<>();
            inorderTraversal(root, result);
            return result;
        }
        
        private void inorderTraversal(Node<T> node, List<T> result) {
            for (int i = 0; i < node.keys.size(); i++) {
                if (!node.leaf) {
                    inorderTraversal(node.children.get(i), result);
                }
                result.add(node.keys.get(i));
            }
            
            if (!node.leaf) {
                inorderTraversal(node.children.get(node.children.size() - 1), result);
            }
        }
    }
    
    /**
     * Represents a graph data structure
     */
    public static class Graph<T> {
        private final Map<T, Set<T>> adjacencyList;
        private final boolean directed;
        
        public Graph() {
            this(false);
        }
        
        public Graph(boolean directed) {
            this.adjacencyList = new HashMap<>();
            this.directed = directed;
        }
        
        /**
         * Adds a vertex to the graph
         */
        public void addVertex(T vertex) {
            adjacencyList.putIfAbsent(vertex, new HashSet<>());
        }
        
        /**
         * Adds an edge to the graph
         */
        public void addEdge(T from, T to) {
            addVertex(from);
            addVertex(to);
            adjacencyList.get(from).add(to);
            
            if (!directed) {
                adjacencyList.get(to).add(from);
            }
        }
        
        /**
         * Removes a vertex from the graph
         */
        public void removeVertex(T vertex) {
            adjacencyList.remove(vertex);
            
            // Remove all edges to this vertex
            for (Set<T> neighbors : adjacencyList.values()) {
                neighbors.remove(vertex);
            }
        }
        
        /**
         * Removes an edge from the graph
         */
        public void removeEdge(T from, T to) {
            if (adjacencyList.containsKey(from)) {
                adjacencyList.get(from).remove(to);
            }
            
            if (!directed && adjacencyList.containsKey(to)) {
                adjacencyList.get(to).remove(from);
            }
        }
        
        /**
         * Gets the neighbors of a vertex
         */
        public Set<T> getNeighbors(T vertex) {
            return new HashSet<>(adjacencyList.getOrDefault(vertex, new HashSet<>()));
        }
        
        /**
         * Gets all vertices in the graph
         */
        public Set<T> getVertices() {
            return new HashSet<>(adjacencyList.keySet());
        }
        
        /**
         * Gets the number of vertices
         */
        public int getVertexCount() {
            return adjacencyList.size();
        }
        
        /**
         * Gets the number of edges
         */
        public int getEdgeCount() {
            int count = 0;
            for (Set<T> neighbors : adjacencyList.values()) {
                count += neighbors.size();
            }
            
            return directed ? count : count / 2;
        }
        
        /**
         * Checks if the graph contains a vertex
         */
        public boolean containsVertex(T vertex) {
            return adjacencyList.containsKey(vertex);
        }
        
        /**
         * Checks if the graph contains an edge
         */
        public boolean containsEdge(T from, T to) {
            return adjacencyList.containsKey(from) && adjacencyList.get(from).contains(to);
        }
        
        /**
         * Performs breadth-first search
         */
        public List<T> breadthFirstSearch(T start) {
            if (!containsVertex(start)) {
                throw new IllegalArgumentException("Start vertex not found in graph");
            }
            
            List<T> result = new ArrayList<>();
            Set<T> visited = new HashSet<>();
            Queue<T> queue = new LinkedList<>();
            
            visited.add(start);
            queue.offer(start);
            
            while (!queue.isEmpty()) {
                T vertex = queue.poll();
                result.add(vertex);
                
                for (T neighbor : getNeighbors(vertex)) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.offer(neighbor);
                    }
                }
            }
            
            return result;
        }
        
        /**
         * Performs depth-first search
         */
        public List<T> depthFirstSearch(T start) {
            if (!containsVertex(start)) {
                throw new IllegalArgumentException("Start vertex not found in graph");
            }
            
            List<T> result = new ArrayList<>();
            Set<T> visited = new HashSet<>();
            depthFirstSearch(start, visited, result);
            return result;
        }
        
        private void depthFirstSearch(T vertex, Set<T> visited, List<T> result) {
            visited.add(vertex);
            result.add(vertex);
            
            for (T neighbor : getNeighbors(vertex)) {
                if (!visited.contains(neighbor)) {
                    depthFirstSearch(neighbor, visited, result);
                }
            }
        }
        
        /**
         * Finds the shortest path between two vertices using BFS
         */
        public List<T> shortestPath(T start, T end) {
            if (!containsVertex(start) || !containsVertex(end)) {
                throw new IllegalArgumentException("Start or end vertex not found in graph");
            }
            
            if (start.equals(end)) {
                return Arrays.asList(start);
            }
            
            Map<T, T> previous = new HashMap<>();
            Set<T> visited = new HashSet<>();
            Queue<T> queue = new LinkedList<>();
            
            visited.add(start);
            queue.offer(start);
            
            while (!queue.isEmpty()) {
                T vertex = queue.poll();
                
                for (T neighbor : getNeighbors(vertex)) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        previous.put(neighbor, vertex);
                        queue.offer(neighbor);
                        
                        if (neighbor.equals(end)) {
                            // Reconstruct path
                            List<T> path = new ArrayList<>();
                            T current = end;
                            while (current != null) {
                                path.add(current);
                                current = previous.get(current);
                            }
                            Collections.reverse(path);
                            return path;
                        }
                    }
                }
            }
            
            return new ArrayList<>(); // No path found
        }
        
        /**
         * Detects cycles in the graph
         */
        public boolean hasCycle() {
            Set<T> visited = new HashSet<>();
            Set<T> recursionStack = new HashSet<>();
            
            for (T vertex : getVertices()) {
                if (!visited.contains(vertex)) {
                    if (hasCycle(vertex, visited, recursionStack)) {
                        return true;
                    }
                }
            }
            
            return false;
        }
        
        private boolean hasCycle(T vertex, Set<T> visited, Set<T> recursionStack) {
            visited.add(vertex);
            recursionStack.add(vertex);
            
            for (T neighbor : getNeighbors(vertex)) {
                if (!visited.contains(neighbor)) {
                    if (hasCycle(neighbor, visited, recursionStack)) {
                        return true;
                    }
                } else if (recursionStack.contains(neighbor)) {
                    return true;
                }
            }
            
            recursionStack.remove(vertex);
            return false;
        }
        
        /**
         * Performs topological sort (for directed acyclic graphs)
         */
        public List<T> topologicalSort() {
            if (!directed) {
                throw new IllegalStateException("Topological sort only applies to directed graphs");
            }
            
            if (hasCycle()) {
                throw new IllegalStateException("Graph contains a cycle, topological sort not possible");
            }
            
            List<T> result = new ArrayList<>();
            Set<T> visited = new HashSet<>();
            
            for (T vertex : getVertices()) {
                if (!visited.contains(vertex)) {
                    topologicalSort(vertex, visited, result);
                }
            }
            
            Collections.reverse(result);
            return result;
        }
        
        private void topologicalSort(T vertex, Set<T> visited, List<T> result) {
            visited.add(vertex);
            
            for (T neighbor : getNeighbors(vertex)) {
                if (!visited.contains(neighbor)) {
                    topologicalSort(neighbor, visited, result);
                }
            }
            
            result.add(vertex);
        }
    }
    
    /**
     * Represents a disjoint set (union-find) data structure
     */
    public static class DisjointSet<T> {
        private final Map<T, T> parent;
        private final Map<T, Integer> rank;
        
        public DisjointSet() {
            this.parent = new HashMap<>();
            this.rank = new HashMap<>();
        }
        
        /**
         * Creates a new set with a single element
         */
        public void makeSet(T element) {
            parent.put(element, element);
            rank.put(element, 0);
        }
        
        /**
         * Finds the representative (root) of the set containing the element
         */
        public T find(T element) {
            if (!parent.containsKey(element)) {
                throw new IllegalArgumentException("Element not found in disjoint set");
            }
            
            if (!parent.get(element).equals(element)) {
                // Path compression
                parent.put(element, find(parent.get(element)));
            }
            
            return parent.get(element);
        }
        
        /**
         * Unites the sets containing two elements
         */
        public void union(T element1, T element2) {
            T root1 = find(element1);
            T root2 = find(element2);
            
            if (root1.equals(root2)) {
                return; // Already in the same set
            }
            
            // Union by rank
            if (rank.get(root1) < rank.get(root2)) {
                parent.put(root1, root2);
            } else if (rank.get(root1) > rank.get(root2)) {
                parent.put(root2, root1);
            } else {
                parent.put(root2, root1);
                rank.put(root1, rank.get(root1) + 1);
            }
        }
        
        /**
         * Checks if two elements are in the same set
         */
        public boolean isConnected(T element1, T element2) {
            return find(element1).equals(find(element2));
        }
        
        /**
         * Gets the number of disjoint sets
         */
        public int getSetCount() {
            Set<T> roots = new HashSet<>();
            for (T element : parent.keySet()) {
                roots.add(find(element));
            }
            return roots.size();
        }
    }
    
    /**
     * Represents a segment tree for range queries
     */
    public static class SegmentTree {
        private final int[] tree;
        private final int n;
        
        public SegmentTree(int[] array) {
            this.n = array.length;
            // Size of segment tree is 2 * next power of 2 - 1
            int size = 2 * (int) Math.pow(2, Math.ceil(Math.log(n) / Math.log(2))) - 1;
            this.tree = new int[size];
            buildTree(array, 0, n - 1, 0);
        }
        
        private int buildTree(int[] array, int start, int end, int node) {
            if (start == end) {
                tree[node] = array[start];
                return tree[node];
            }
            
            int mid = (start + end) / 2;
            tree[node] = buildTree(array, start, mid, 2 * node + 1) +
                         buildTree(array, mid + 1, end, 2 * node + 2);
            return tree[node];
        }
        
        /**
         * Queries the sum in a range
         */
        public int querySum(int left, int right) {
            return querySum(0, n - 1, left, right, 0);
        }
        
        private int querySum(int start, int end, int left, int right, int node) {
            // No overlap
            if (right < start || left > end) {
                return 0;
            }
            
            // Complete overlap
            if (left <= start && right >= end) {
                return tree[node];
            }
            
            // Partial overlap
            int mid = (start + end) / 2;
            return querySum(start, mid, left, right, 2 * node + 1) +
                   querySum(mid + 1, end, left, right, 2 * node + 2);
        }
        
        /**
         * Updates a value at a specific index
         */
        public void update(int index, int value) {
            int diff = value - querySum(index, index);
            update(0, n - 1, index, diff, 0);
        }
        
        private void update(int start, int end, int index, int diff, int node) {
            if (index < start || index > end) {
                return;
            }
            
            tree[node] += diff;
            
            if (start != end) {
                int mid = (start + end) / 2;
                update(start, mid, index, diff, 2 * node + 1);
                update(mid + 1, end, index, diff, 2 * node + 2);
            }
        }
    }
    
    /**
     * Represents a trie (prefix tree) data structure
     */
    public static class Trie {
        private final TrieNode root;
        
        private static class TrieNode {
            Map<Character, TrieNode> children;
            boolean isEndOfWord;
            
            TrieNode() {
                children = new HashMap<>();
                isEndOfWord = false;
            }
        }
        
        public Trie() {
            root = new TrieNode();
        }
        
        /**
         * Inserts a word into the trie
         */
        public void insert(String word) {
            TrieNode current = root;
            
            for (char ch : word.toCharArray()) {
                current.children.putIfAbsent(ch, new TrieNode());
                current = current.children.get(ch);
            }
            
            current.isEndOfWord = true;
        }
        
        /**
         * Searches for a word in the trie
         */
        public boolean search(String word) {
            TrieNode current = root;
            
            for (char ch : word.toCharArray()) {
                if (!current.children.containsKey(ch)) {
                    return false;
                }
                current = current.children.get(ch);
            }
            
            return current.isEndOfWord;
        }
        
        /**
         * Checks if any word in the trie starts with the given prefix
         */
        public boolean startsWith(String prefix) {
            TrieNode current = root;
            
            for (char ch : prefix.toCharArray()) {
                if (!current.children.containsKey(ch)) {
                    return false;
                }
                current = current.children.get(ch);
            }
            
            return true;
        }
        
        /**
         * Gets all words with the given prefix
         */
        public List<String> getWordsWithPrefix(String prefix) {
            List<String> result = new ArrayList<>();
            TrieNode current = root;
            
            // Navigate to the prefix
            for (char ch : prefix.toCharArray()) {
                if (!current.children.containsKey(ch)) {
                    return result; // No words with this prefix
                }
                current = current.children.get(ch);
            }
            
            // Collect all words from this point
            collectWords(current, new StringBuilder(prefix), result);
            return result;
        }
        
        private void collectWords(TrieNode node, StringBuilder prefix, List<String> result) {
            if (node.isEndOfWord) {
                result.add(prefix.toString());
            }
            
            for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
                prefix.append(entry.getKey());
                collectWords(entry.getValue(), prefix, result);
                prefix.deleteCharAt(prefix.length() - 1);
            }
        }
        
        /**
         * Deletes a word from the trie
         */
        public boolean delete(String word) {
            return delete(root, word, 0);
        }
        
        private boolean delete(TrieNode current, String word, int index) {
            if (index == word.length()) {
                // Word found
                if (!current.isEndOfWord) {
                    return false; // Word doesn't exist
                }
                current.isEndOfWord = false;
                
                // Return true if current node has no other children
                return current.children.isEmpty();
            }
            
            char ch = word.charAt(index);
            TrieNode node = current.children.get(ch);
            if (node == null) {
                return false; // Word doesn't exist
            }
            
            boolean shouldDeleteCurrentNode = delete(node, word, index + 1);
            
            if (shouldDeleteCurrentNode) {
                current.children.remove(ch);
                // Return true if current node has no other children and is not end of another word
                return current.children.isEmpty() && !current.isEndOfWord;
            }
            
            return false;
        }
    }
    
    /**
     * Represents a suffix tree for string matching
     */
    public static class SuffixTree {
        private final SuffixTreeNode root;
        private final String text;
        
        private static class SuffixTreeNode {
            Map<Character, SuffixTreeNode> children;
            int startIndex;
            int endIndex;
            int suffixIndex;
            
            SuffixTreeNode() {
                children = new HashMap<>();
                startIndex = -1;
                endIndex = -1;
                suffixIndex = -1;
            }
            
            SuffixTreeNode(int start, int end, int index) {
                children = new HashMap<>();
                startIndex = start;
                endIndex = end;
                suffixIndex = index;
            }
        }
        
        public SuffixTree(String text) {
            this.text = text + "$"; // Add terminal symbol
            this.root = new SuffixTreeNode();
            buildSuffixTree();
        }
        
        private void buildSuffixTree() {
            // Simplified implementation - in practice, Ukkonen's algorithm would be used
            for (int i = 0; i < text.length(); i++) {
                insertSuffix(text.substring(i), i);
            }
        }
        
        private void insertSuffix(String suffix, int index) {
            SuffixTreeNode current = root;
            
            for (int i = 0; i < suffix.length(); i++) {
                char ch = suffix.charAt(i);
                if (!current.children.containsKey(ch)) {
                    current.children.put(ch, new SuffixTreeNode(i, text.length() - 1, index));
                    return;
                }
                current = current.children.get(ch);
            }
        }
        
        /**
         * Searches for a pattern in the text
         */
        public boolean search(String pattern) {
            SuffixTreeNode current = root;
            
            for (int i = 0; i < pattern.length(); i++) {
                char ch = pattern.charAt(i);
                if (!current.children.containsKey(ch)) {
                    return false;
                }
                current = current.children.get(ch);
            }
            
            return true;
        }
        
        /**
         * Gets all occurrences of a pattern in the text
         */
        public List<Integer> findAllOccurrences(String pattern) {
            List<Integer> occurrences = new ArrayList<>();
            findAllOccurrences(root, pattern, 0, occurrences);
            return occurrences;
        }
        
        private void findAllOccurrences(SuffixTreeNode node, String pattern, int patternIndex, List<Integer> occurrences) {
            if (patternIndex == pattern.length()) {
                collectAllSuffixes(node, occurrences);
                return;
            }
            
            char ch = pattern.charAt(patternIndex);
            if (node.children.containsKey(ch)) {
                findAllOccurrences(node.children.get(ch), pattern, patternIndex + 1, occurrences);
            }
        }
        
        private void collectAllSuffixes(SuffixTreeNode node, List<Integer> occurrences) {
            if (node.suffixIndex != -1) {
                occurrences.add(node.suffixIndex);
            }
            
            for (SuffixTreeNode child : node.children.values()) {
                collectAllSuffixes(child, occurrences);
            }
        }
    }
    
    /**
     * Represents a binary heap (priority queue)
     */
    public static class BinaryHeap<T extends Comparable<T>> {
        private final List<T> heap;
        private final boolean isMinHeap;
        
        public BinaryHeap() {
            this(true);
        }
        
        public BinaryHeap(boolean isMinHeap) {
            this.heap = new ArrayList<>();
            this.isMinHeap = isMinHeap;
        }
        
        /**
         * Inserts an element into the heap
         */
        public void insert(T element) {
            heap.add(element);
            heapifyUp(heap.size() - 1);
        }
        
        /**
         * Extracts the root element from the heap
         */
        public T extract() {
            if (heap.isEmpty()) {
                throw new NoSuchElementException("Heap is empty");
            }
            
            T root = heap.get(0);
            T last = heap.remove(heap.size() - 1);
            
            if (!heap.isEmpty()) {
                heap.set(0, last);
                heapifyDown(0);
            }
            
            return root;
        }
        
        /**
         * Gets the root element without removing it
         */
        public T peek() {
            if (heap.isEmpty()) {
                throw new NoSuchElementException("Heap is empty");
            }
            return heap.get(0);
        }
        
        /**
         * Checks if the heap is empty
         */
        public boolean isEmpty() {
            return heap.isEmpty();
        }
        
        /**
         * Gets the size of the heap
         */
        public int size() {
            return heap.size();
        }
        
        /**
         * Maintains the heap property by moving an element up
         */
        private void heapifyUp(int index) {
            if (index == 0) {
                return;
            }
            
            int parentIndex = (index - 1) / 2;
            T current = heap.get(index);
            T parent = heap.get(parentIndex);
            
            boolean shouldSwap = isMinHeap ? current.compareTo(parent) < 0 : current.compareTo(parent) > 0;
            
            if (shouldSwap) {
                heap.set(index, parent);
                heap.set(parentIndex, current);
                heapifyUp(parentIndex);
            }
        }
        
        /**
         * Maintains the heap property by moving an element down
         */
        private void heapifyDown(int index) {
            int leftChildIndex = 2 * index + 1;
            int rightChildIndex = 2 * index + 2;
            int smallestOrLargestIndex = index;
            
            if (leftChildIndex < heap.size()) {
                T current = heap.get(index);
                T leftChild = heap.get(leftChildIndex);
                
                boolean leftIsSmallerOrLarger = isMinHeap ? 
                    leftChild.compareTo(current) < 0 : leftChild.compareTo(current) > 0;
                
                if (leftIsSmallerOrLarger) {
                    smallestOrLargestIndex = leftChildIndex;
                }
            }
            
            if (rightChildIndex < heap.size()) {
                T current = heap.get(smallestOrLargestIndex);
                T rightChild = heap.get(rightChildIndex);
                
                boolean rightIsSmallerOrLarger = isMinHeap ? 
                    rightChild.compareTo(current) < 0 : rightChild.compareTo(current) > 0;
                
                if (rightIsSmallerOrLarger) {
                    smallestOrLargestIndex = rightChildIndex;
                }
            }
            
            if (smallestOrLargestIndex != index) {
                T temp = heap.get(index);
                heap.set(index, heap.get(smallestOrLargestIndex));
                heap.set(smallestOrLargestIndex, temp);
                heapifyDown(smallestOrLargestIndex);
            }
        }
    }
    
    /**
     * Represents a Fibonacci heap
     */
    public static class FibonacciHeap<T extends Comparable<T>> {
        private Node<T> min;
        private int size;
        
        private static class Node<T> {
            T data;
            int degree;
            boolean mark;
            Node<T> parent;
            Node<T> child;
            Node<T> left;
            Node<T> right;
            
            Node(T data) {
                this.data = data;
                this.degree = 0;
                this.mark = false;
                this.parent = null;
                this.child = null;
                this.left = this;
                this.right = this;
            }
        }
        
        public FibonacciHeap() {
            this.min = null;
            this.size = 0;
        }
        
        /**
         * Inserts an element into the heap
         */
        public void insert(T data) {
            Node<T> node = new Node<>(data);
            if (min == null) {
                min = node;
            } else {
                // Add to root list
                node.right = min;
                node.left = min.left;
                min.left.right = node;
                min.left = node;
                
                // Update min if necessary
                if (data.compareTo(min.data) < 0) {
                    min = node;
                }
            }
            size++;
        }
        
        /**
         * Extracts the minimum element from the heap
         */
        public T extractMin() {
            if (min == null) {
                throw new NoSuchElementException("Heap is empty");
            }
            
            Node<T> z = min;
            T result = z.data;
            
            if (z.child != null) {
                // Add children to root list
                Node<T> child = z.child;
                do {
                    Node<T> next = child.right;
                    child.parent = null;
                    child.left.right = child.right;
                    child.right.left = child.left;
                    child.right = min;
                    child.left = min.left;
                    min.left.right = child;
                    min.left = child;
                    child = next;
                } while (child != z.child);
            }
            
            // Remove z from root list
            z.left.right = z.right;
            z.right.left = z.left;
            
            if (z == z.right) {
                min = null;
            } else {
                min = z.right;
                consolidate();
            }
            
            size--;
            return result;
        }
        
        /**
         * Consolidates the heap after extraction
         */
        private void consolidate() {
            int maxDegree = (int) (Math.log(size) / Math.log(2)) + 1;
            Node<T>[] degreeTable = new Node[maxDegree + 1];
            
            // Initialize degree table
            for (int i = 0; i <= maxDegree; i++) {
                degreeTable[i] = null;
            }
            
            // Collect roots
            List<Node<T>> roots = new ArrayList<>();
            if (min != null) {
                Node<T> current = min;
                do {
                    roots.add(current);
                    current = current.right;
                } while (current != min);
            }
            
            // Process each root
            for (Node<T> root : roots) {
                int degree = root.degree;
                while (degreeTable[degree] != null) {
                    Node<T> other = degreeTable[degree];
                    
                    // Ensure root is smaller
                    if (root.data.compareTo(other.data) > 0) {
                        Node<T> temp = root;
                        root = other;
                        other = temp;
                    }
                    
                    // Link other to root
                    link(other, root);
                    degreeTable[degree] = null;
                    degree++;
                }
                degreeTable[degree] = root;
            }
            
            // Rebuild root list and find new min
            min = null;
            for (int i = 0; i <= maxDegree; i++) {
                if (degreeTable[i] != null) {
                    if (min == null) {
                        min = degreeTable[i];
                        min.right = min;
                        min.left = min;
                    } else {
                        // Add to root list
                        degreeTable[i].right = min;
                        degreeTable[i].left = min.left;
                        min.left.right = degreeTable[i];
                        min.left = degreeTable[i];
                        
                        // Update min if necessary
                        if (degreeTable[i].data.compareTo(min.data) < 0) {
                            min = degreeTable[i];
                        }
                    }
                }
            }
        }
        
        /**
         * Links two nodes in the heap
         */
        private void link(Node<T> child, Node<T> parent) {
            // Remove child from root list
            child.left.right = child.right;
            child.right.left = child.left;
            
            // Make child a child of parent
            child.parent = parent;
            if (parent.child == null) {
                parent.child = child;
                child.right = child;
                child.left = child;
            } else {
                child.right = parent.child;
                child.left = parent.child.left;
                parent.child.left.right = child;
                parent.child.left = child;
            }
            
            parent.degree++;
            child.mark = false;
        }
        
        /**
         * Gets the minimum element without removing it
         */
        public T peekMin() {
            if (min == null) {
                throw new NoSuchElementException("Heap is empty");
            }
            return min.data;
        }
        
        /**
         * Checks if the heap is empty
         */
        public boolean isEmpty() {
            return min == null;
        }
        
        /**
         * Gets the size of the heap
         */
        public int size() {
            return size;
        }
    }
}