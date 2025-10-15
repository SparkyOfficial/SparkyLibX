package com.sparky.libx.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;

/**
 * просунуті структури даних для складних обчислень
 * включає дерева, графи, хеш-таблиці і спеціалізовані структури
 * @author Андрій Будильников
 */
public class AdvancedDataStructures {
    
    /**
     * бінарне дерево пошуку
     */
    public static class BinarySearchTree<T extends Comparable<T>> {
        private Node root;
        
        private class Node {
            T data;
            Node left;
            Node right;
            
            Node(T data) {
                this.data = data;
            }
        }
        
        /**
         * вставити елемент
         * @param data елемент для вставки
         */
        public void insert(T data) {
            root = insertRecursive(root, data);
        }
        
        private Node insertRecursive(Node node, T data) {
            if (node == null) {
                return new Node(data);
            }
            
            if (data.compareTo(node.data) < 0) {
                node.left = insertRecursive(node.left, data);
            } else if (data.compareTo(node.data) > 0) {
                node.right = insertRecursive(node.right, data);
            }
            
            return node;
        }
        
        /**
         * знайти елемент
         * @param data елемент для пошуку
         * @return true якщо елемент знайдено
         */
        public boolean contains(T data) {
            return containsRecursive(root, data);
        }
        
        private boolean containsRecursive(Node node, T data) {
            if (node == null) {
                return false;
            }
            
            if (data.compareTo(node.data) == 0) {
                return true;
            }
            
            if (data.compareTo(node.data) < 0) {
                return containsRecursive(node.left, data);
            } else {
                return containsRecursive(node.right, data);
            }
        }
        
        /**
         * видалити елемент
         * @param data елемент для видалення
         */
        public void delete(T data) {
            root = deleteRecursive(root, data);
        }
        
        private Node deleteRecursive(Node node, T data) {
            if (node == null) {
                return null;
            }
            
            if (data.compareTo(node.data) < 0) {
                node.left = deleteRecursive(node.left, data);
            } else if (data.compareTo(node.data) > 0) {
                node.right = deleteRecursive(node.right, data);
            } else {
                // вузол знайдено
                if (node.left == null) {
                    return node.right;
                } else if (node.right == null) {
                    return node.left;
                }
                
                // вузол з двома дітьми
                node.data = findMin(node.right);
                node.right = deleteRecursive(node.right, node.data);
            }
            
            return node;
        }
        
        private T findMin(Node node) {
            while (node.left != null) {
                node = node.left;
            }
            return node.data;
        }
        
        /**
         * отримати список елементів в порядку зростання
         * @return список елементів
         */
        public List<T> inorderTraversal() {
            List<T> result = new ArrayList<>();
            inorderRecursive(root, result);
            return result;
        }
        
        private void inorderRecursive(Node node, List<T> result) {
            if (node != null) {
                inorderRecursive(node.left, result);
                result.add(node.data);
                inorderRecursive(node.right, result);
            }
        }
        
        /**
         * отримати висоту дерева
         * @return висота дерева
         */
        public int height() {
            return heightRecursive(root);
        }
        
        private int heightRecursive(Node node) {
            if (node == null) {
                return 0;
            }
            
            return 1 + Math.max(heightRecursive(node.left), heightRecursive(node.right));
        }
        
        /**
         * перевірити чи дерево збалансоване
         * @return true якщо дерево збалансоване
         */
        public boolean isBalanced() {
            return isBalancedRecursive(root) != -1;
        }
        
        private int isBalancedRecursive(Node node) {
            if (node == null) {
                return 0;
            }
            
            int leftHeight = isBalancedRecursive(node.left);
            if (leftHeight == -1) {
                return -1;
            }
            
            int rightHeight = isBalancedRecursive(node.right);
            if (rightHeight == -1) {
                return -1;
            }
            
            if (Math.abs(leftHeight - rightHeight) > 1) {
                return -1;
            }
            
            return 1 + Math.max(leftHeight, rightHeight);
        }
    }
    
    /**
     * червоно-чорне дерево
     */
    public static class RedBlackTree<T extends Comparable<T>> {
        private static final boolean RED = true;
        private static final boolean BLACK = false;
        
        private Node root;
        
        private class Node {
            T data;
            Node left;
            Node right;
            boolean color;
            
            Node(T data, boolean color) {
                this.data = data;
                this.color = color;
            }
        }
        
        /**
         * вставити елемент
         * @param data елемент для вставки
         */
        public void insert(T data) {
            root = insertRecursive(root, data);
            root.color = BLACK;
        }
        
        private Node insertRecursive(Node node, T data) {
            if (node == null) {
                return new Node(data, RED);
            }
            
            if (data.compareTo(node.data) < 0) {
                node.left = insertRecursive(node.left, data);
            } else if (data.compareTo(node.data) > 0) {
                node.right = insertRecursive(node.right, data);
            }
            
            // балансування
            if (isRed(node.right) && !isRed(node.left)) {
                node = rotateLeft(node);
            }
            if (isRed(node.left) && isRed(node.left.left)) {
                node = rotateRight(node);
            }
            if (isRed(node.left) && isRed(node.right)) {
                flipColors(node);
            }
            
            return node;
        }
        
        private boolean isRed(Node node) {
            if (node == null) {
                return false;
            }
            return node.color == RED;
        }
        
        private Node rotateLeft(Node node) {
            Node newRoot = node.right;
            node.right = newRoot.left;
            newRoot.left = node;
            newRoot.color = node.color;
            node.color = RED;
            return newRoot;
        }
        
        private Node rotateRight(Node node) {
            Node newRoot = node.left;
            node.left = newRoot.right;
            newRoot.right = node;
            newRoot.color = node.color;
            node.color = RED;
            return newRoot;
        }
        
        private void flipColors(Node node) {
            node.color = RED;
            node.left.color = BLACK;
            node.right.color = BLACK;
        }
        
        /**
         * знайти елемент
         * @param data елемент для пошуку
         * @return true якщо елемент знайдено
         */
        public boolean contains(T data) {
            return containsRecursive(root, data);
        }
        
        private boolean containsRecursive(Node node, T data) {
            if (node == null) {
                return false;
            }
            
            if (data.compareTo(node.data) == 0) {
                return true;
            }
            
            if (data.compareTo(node.data) < 0) {
                return containsRecursive(node.left, data);
            } else {
                return containsRecursive(node.right, data);
            }
        }
    }
    
    /**
     * префіксне дерево (trie)
     */
    public static class Trie {
        private TrieNode root;
        
        private class TrieNode {
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
         * вставити слово
         * @param word слово для вставки
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
         * знайти слово
         * @param word слово для пошуку
         * @return true якщо слово знайдено
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
         * перевірити чи існує префікс
         * @param prefix префікс для перевірки
         * @return true якщо префікс існує
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
         * отримати всі слова з заданим префіксом
         * @param prefix префікс
         * @return список слів
         */
        public List<String> getWordsWithPrefix(String prefix) {
            List<String> result = new ArrayList<>();
            TrieNode current = root;
            
            // знайти вузол для префікса
            for (char ch : prefix.toCharArray()) {
                if (!current.children.containsKey(ch)) {
                    return result;
                }
                current = current.children.get(ch);
            }
            
            // зібрати всі слова
            collectWords(current, prefix, result);
            return result;
        }
        
        private void collectWords(TrieNode node, String prefix, List<String> result) {
            if (node.isEndOfWord) {
                result.add(prefix);
            }
            
            for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
                collectWords(entry.getValue(), prefix + entry.getKey(), result);
            }
        }
    }
    
    /**
     * двостороння черга (deque)
     */
    public static class Deque<T> {
        private final LinkedList<T> list;
        
        public Deque() {
            list = new LinkedList<>();
        }
        
        /**
         * додати елемент на початок
         * @param item елемент для додавання
         */
        public void addFirst(T item) {
            list.addFirst(item);
        }
        
        /**
         * додати елемент в кінець
         * @param item елемент для додавання
         */
        public void addLast(T item) {
            list.addLast(item);
        }
        
        /**
         * видалити і повернути перший елемент
         * @return перший елемент
         */
        public T removeFirst() {
            if (isEmpty()) {
                throw new IllegalStateException("Deque is empty");
            }
            return list.removeFirst();
        }
        
        /**
         * видалити і повернути останній елемент
         * @return останній елемент
         */
        public T removeLast() {
            if (isEmpty()) {
                throw new IllegalStateException("Deque is empty");
            }
            return list.removeLast();
        }
        
        /**
         * отримати перший елемент без видалення
         * @return перший елемент
         */
        public T peekFirst() {
            if (isEmpty()) {
                return null;
            }
            return list.peekFirst();
        }
        
        /**
         * отримати останній елемент без видалення
         * @return останній елемент
         */
        public T peekLast() {
            if (isEmpty()) {
                return null;
            }
            return list.peekLast();
        }
        
        /**
         * перевірити чи черга порожня
         * @return true якщо черга порожня
         */
        public boolean isEmpty() {
            return list.isEmpty();
        }
        
        /**
         * отримати розмір черги
         * @return розмір черги
         */
        public int size() {
            return list.size();
        }
    }
    
    /**
     * бінарна купа (heap)
     */
    public static class BinaryHeap<T extends Comparable<T>> {
        private final List<T> heap;
        private final boolean isMinHeap;
        
        public BinaryHeap(boolean isMinHeap) {
            this.heap = new ArrayList<>();
            this.isMinHeap = isMinHeap;
        }
        
        /**
         * вставити елемент
         * @param item елемент для вставки
         */
        public void insert(T item) {
            heap.add(item);
            heapifyUp(heap.size() - 1);
        }
        
        /**
         * видалити і повернути кореневий елемент
         * @return кореневий елемент
         */
        public T extract() {
            if (heap.isEmpty()) {
                throw new IllegalStateException("Heap is empty");
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
         * отримати кореневий елемент без видалення
         * @return кореневий елемент
         */
        public T peek() {
            if (heap.isEmpty()) {
                throw new IllegalStateException("Heap is empty");
            }
            return heap.get(0);
        }
        
        private void heapifyUp(int index) {
            if (index == 0) {
                return;
            }
            
            int parentIndex = (index - 1) / 2;
            T current = heap.get(index);
            T parent = heap.get(parentIndex);
            
            boolean shouldSwap = isMinHeap ? 
                current.compareTo(parent) < 0 : 
                current.compareTo(parent) > 0;
                
            if (shouldSwap) {
                heap.set(index, parent);
                heap.set(parentIndex, current);
                heapifyUp(parentIndex);
            }
        }
        
        private void heapifyDown(int index) {
            int leftChildIndex = 2 * index + 1;
            int rightChildIndex = 2 * index + 2;
            int swapIndex = index;
            
            if (leftChildIndex < heap.size()) {
                T current = heap.get(index);
                T leftChild = heap.get(leftChildIndex);
                
                boolean shouldSwapLeft = isMinHeap ? 
                    leftChild.compareTo(current) < 0 : 
                    leftChild.compareTo(current) > 0;
                    
                if (shouldSwapLeft) {
                    swapIndex = leftChildIndex;
                }
            }
            
            if (rightChildIndex < heap.size()) {
                T current = heap.get(swapIndex);
                T rightChild = heap.get(rightChildIndex);
                
                boolean shouldSwapRight = isMinHeap ? 
                    rightChild.compareTo(current) < 0 : 
                    rightChild.compareTo(current) > 0;
                    
                if (shouldSwapRight) {
                    swapIndex = rightChildIndex;
                }
            }
            
            if (swapIndex != index) {
                T temp = heap.get(index);
                heap.set(index, heap.get(swapIndex));
                heap.set(swapIndex, temp);
                heapifyDown(swapIndex);
            }
        }
        
        /**
         * перевірити чи купа порожня
         * @return true якщо купа порожня
         */
        public boolean isEmpty() {
            return heap.isEmpty();
        }
        
        /**
         * отримати розмір купи
         * @return розмір купи
         */
        public int size() {
            return heap.size();
        }
    }
    
    /**
     * bloom фільтр
     */
    public static class BloomFilter<T> {
        private final int size;
        private final int[] bitArray;
        private final int numHashFunctions;
        private final Random random;
        
        public BloomFilter(int size, int numHashFunctions) {
            this.size = size;
            this.bitArray = new int[size];
            this.numHashFunctions = numHashFunctions;
            this.random = new Random();
        }
        
        /**
         * додати елемент до фільтра
         * @param item елемент для додавання
         */
        public void add(T item) {
            for (int i = 0; i < numHashFunctions; i++) {
                int hash = hash(item, i) % size;
                bitArray[hash] = 1;
            }
        }
        
        /**
         * перевірити чи елемент можливо присутній
         * @param item елемент для перевірки
         * @return true якщо елемент можливо присутній
         */
        public boolean mightContain(T item) {
            for (int i = 0; i < numHashFunctions; i++) {
                int hash = hash(item, i) % size;
                if (bitArray[hash] == 0) {
                    return false;
                }
            }
            return true;
        }
        
        private int hash(T item, int seed) {
            // проста хеш-функція для демонстрації
            return Math.abs(item.hashCode() + seed * 31);
        }
    }
}