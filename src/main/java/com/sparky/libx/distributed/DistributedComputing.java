package com.sparky.libx.distributed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Distributed Computing Framework for Minecraft Plugins
 * Provides capabilities for distributed task processing, parallel computing, and cluster management
 * 
 * @author Андрій Будильников
 */
public class DistributedComputing {
    
    /**
     * Represents a distributed task that can be executed across multiple nodes
     */
    public static class DistributedTask<T, R> {
        private final UUID id;
        private final Function<T, R> taskFunction;
        private final T inputData;
        private R result;
        private TaskStatus status;
        private String assignedNode;
        private long createdAt;
        private long startedAt;
        private long completedAt;
        
        public enum TaskStatus {
            PENDING, RUNNING, COMPLETED, FAILED
        }
        
        public DistributedTask(Function<T, R> taskFunction, T inputData) {
            this.id = UUID.randomUUID();
            this.taskFunction = taskFunction;
            this.inputData = inputData;
            this.status = TaskStatus.PENDING;
            this.createdAt = System.currentTimeMillis();
        }
        
        public R execute() {
            try {
                startedAt = System.currentTimeMillis();
                status = TaskStatus.RUNNING;
                result = taskFunction.apply(inputData);
                status = TaskStatus.COMPLETED;
                completedAt = System.currentTimeMillis();
                return result;
            } catch (Exception e) {
                status = TaskStatus.FAILED;
                completedAt = System.currentTimeMillis();
                throw e;
            }
        }
        
        public UUID getId() {
            return id;
        }
        
        public T getInputData() {
            return inputData;
        }
        
        public R getResult() {
            return result;
        }
        
        public TaskStatus getStatus() {
            return status;
        }
        
        public String getAssignedNode() {
            return assignedNode;
        }
        
        public void setAssignedNode(String assignedNode) {
            this.assignedNode = assignedNode;
        }
        
        public long getCreatedAt() {
            return createdAt;
        }
        
        public long getStartedAt() {
            return startedAt;
        }
        
        public long getCompletedAt() {
            return completedAt;
        }
        
        public long getExecutionTime() {
            if (startedAt > 0 && completedAt > 0) {
                return completedAt - startedAt;
            }
            return 0;
        }
    }
    
    /**
     * Represents a node in a distributed computing cluster
     */
    public static class ComputeNode {
        private final String id;
        private final String address;
        private final int port;
        private final int coreCount;
        private final long memoryBytes;
        private NodeStatus status;
        private final List<DistributedTask<?, ?>> assignedTasks;
        private final ExecutorService executorService;
        private long lastHeartbeat;
        
        public enum NodeStatus {
            ONLINE, OFFLINE, BUSY, MAINTENANCE
        }
        
        public ComputeNode(String id, String address, int port, int coreCount, long memoryBytes) {
            this.id = id;
            this.address = address;
            this.port = port;
            this.coreCount = coreCount;
            this.memoryBytes = memoryBytes;
            this.status = NodeStatus.ONLINE;
            this.assignedTasks = new CopyOnWriteArrayList<>();
            this.executorService = Executors.newFixedThreadPool(coreCount);
            this.lastHeartbeat = System.currentTimeMillis();
        }
        
        public void submitTask(DistributedTask<?, ?> task) {
            if (status == NodeStatus.ONLINE) {
                assignedTasks.add(task);
                task.setAssignedNode(id);
                
                executorService.submit(() -> {
                    try {
                        task.execute();
                    } catch (Exception e) {
                        System.err.println("Task execution failed on node " + id + ": " + e.getMessage());
                    } finally {
                        assignedTasks.remove(task);
                        updateStatus();
                    }
                });
                
                updateStatus();
            }
        }
        
        public void updateStatus() {
            if (assignedTasks.size() >= coreCount) {
                status = NodeStatus.BUSY;
            } else if (assignedTasks.isEmpty()) {
                status = NodeStatus.ONLINE;
            }
        }
        
        public void sendHeartbeat() {
            lastHeartbeat = System.currentTimeMillis();
        }
        
        public boolean isAlive() {
            return System.currentTimeMillis() - lastHeartbeat < 30000; // 30 seconds timeout
        }
        
        public void shutdown() {
            status = NodeStatus.OFFLINE;
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        public String getId() {
            return id;
        }
        
        public String getAddress() {
            return address;
        }
        
        public int getPort() {
            return port;
        }
        
        public int getCoreCount() {
            return coreCount;
        }
        
        public long getMemoryBytes() {
            return memoryBytes;
        }
        
        public NodeStatus getStatus() {
            return status;
        }
        
        public void setStatus(NodeStatus status) {
            this.status = status;
        }
        
        public List<DistributedTask<?, ?>> getAssignedTasks() {
            return new ArrayList<>(assignedTasks);
        }
        
        public int getAvailableSlots() {
            return coreCount - assignedTasks.size();
        }
        
        public long getLastHeartbeat() {
            return lastHeartbeat;
        }
    }
    
    /**
     * Represents a distributed computing cluster
     */
    public static class ComputeCluster {
        private final String clusterId;
        private final Map<String, ComputeNode> nodes;
        private final Queue<DistributedTask<?, ?>> taskQueue;
        private final ScheduledExecutorService scheduler;
        private boolean running;
        
        public ComputeCluster(String clusterId) {
            this.clusterId = clusterId;
            this.nodes = new ConcurrentHashMap<>();
            this.taskQueue = new ConcurrentLinkedQueue<>();
            this.scheduler = Executors.newScheduledThreadPool(2);
            this.running = false;
        }
        
        public void start() {
            if (!running) {
                running = true;
                // Start task scheduler
                scheduler.scheduleAtFixedRate(this::scheduleTasks, 0, 1, TimeUnit.SECONDS);
                // Start node health checker
                scheduler.scheduleAtFixedRate(this::checkNodeHealth, 0, 5, TimeUnit.SECONDS);
            }
        }
        
        public void stop() {
            if (running) {
                running = false;
                scheduler.shutdown();
                try {
                    if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                        scheduler.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    scheduler.shutdownNow();
                    Thread.currentThread().interrupt();
                }
                
                // Shutdown all nodes
                for (ComputeNode node : nodes.values()) {
                    node.shutdown();
                }
            }
        }
        
        public ComputeNode addNode(String id, String address, int port, int coreCount, long memoryBytes) {
            ComputeNode node = new ComputeNode(id, address, port, coreCount, memoryBytes);
            nodes.put(id, node);
            return node;
        }
        
        public void removeNode(String nodeId) {
            ComputeNode node = nodes.remove(nodeId);
            if (node != null) {
                node.shutdown();
            }
        }
        
        public <T, R> void submitTask(DistributedTask<T, R> task) {
            taskQueue.offer(task);
        }
        
        private void scheduleTasks() {
            if (!running) return;
            
            // Distribute tasks to available nodes
            while (!taskQueue.isEmpty()) {
                DistributedTask<?, ?> task = taskQueue.peek();
                ComputeNode node = findBestNode();
                
                if (node != null && node.getAvailableSlots() > 0) {
                    taskQueue.poll(); // Remove from queue
                    node.submitTask(task);
                } else {
                    // No available nodes, break to avoid infinite loop
                    break;
                }
            }
        }
        
        private ComputeNode findBestNode() {
            return nodes.values().stream()
                .filter(node -> node.getStatus() == ComputeNode.NodeStatus.ONLINE && node.getAvailableSlots() > 0)
                .max(Comparator.comparingInt(ComputeNode::getAvailableSlots))
                .orElse(null);
        }
        
        private void checkNodeHealth() {
            if (!running) return;
            
            long currentTime = System.currentTimeMillis();
            Iterator<Map.Entry<String, ComputeNode>> iterator = nodes.entrySet().iterator();
            
            while (iterator.hasNext()) {
                Map.Entry<String, ComputeNode> entry = iterator.next();
                ComputeNode node = entry.getValue();
                
                // Check if node is still alive
                if (!node.isAlive()) {
                    System.out.println("Node " + node.getId() + " is offline due to missed heartbeat");
                    // Move tasks back to queue
                    for (DistributedTask<?, ?> task : node.getAssignedTasks()) {
                        taskQueue.offer(task);
                    }
                    iterator.remove();
                }
            }
        }
        
        public Collection<ComputeNode> getNodes() {
            return new ArrayList<>(nodes.values());
        }
        
        public ComputeNode getNode(String id) {
            return nodes.get(id);
        }
        
        public int getTaskQueueSize() {
            return taskQueue.size();
        }
        
        public String getClusterId() {
            return clusterId;
        }
        
        public boolean isRunning() {
            return running;
        }
    }
    
    /**
     * Represents a map-reduce framework for distributed data processing
     */
    public static class MapReduceFramework {
        public static class KeyValuePair<K, V> {
            private final K key;
            private final V value;
            
            public KeyValuePair(K key, V value) {
                this.key = key;
                this.value = value;
            }
            
            public K getKey() {
                return key;
            }
            
            public V getValue() {
                return value;
            }
        }
        
        public static <T, K, V> List<KeyValuePair<K, V>> mapReduce(
                List<T> inputData,
                Function<T, List<KeyValuePair<K, V>>> mapFunction,
                Function<Map<K, List<V>>, List<KeyValuePair<K, V>>> reduceFunction) {
            
            // Map phase - distribute work across multiple threads
            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            List<Future<List<KeyValuePair<K, V>>>> futures = new ArrayList<>();
            
            // Split data into chunks for parallel processing
            int chunkSize = Math.max(1, inputData.size() / Runtime.getRuntime().availableProcessors());
            for (int i = 0; i < inputData.size(); i += chunkSize) {
                int end = Math.min(i + chunkSize, inputData.size());
                List<T> chunk = inputData.subList(i, end);
                
                Future<List<KeyValuePair<K, V>>> future = executor.submit(() -> {
                    List<KeyValuePair<K, V>> results = new ArrayList<>();
                    for (T item : chunk) {
                        results.addAll(mapFunction.apply(item));
                    }
                    return results;
                });
                
                futures.add(future);
            }
            
            // Collect map results
            List<KeyValuePair<K, V>> mappedResults = new ArrayList<>();
            for (Future<List<KeyValuePair<K, V>>> future : futures) {
                try {
                    mappedResults.addAll(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Map phase failed", e);
                }
            }
            
            executor.shutdown();
            
            // Group by key
            Map<K, List<V>> grouped = new HashMap<>();
            for (KeyValuePair<K, V> pair : mappedResults) {
                grouped.computeIfAbsent(pair.getKey(), k -> new ArrayList<>()).add(pair.getValue());
            }
            
            // Reduce phase
            return reduceFunction.apply(grouped);
        }
        
        /**
         * Example word count map function
         */
        public static List<KeyValuePair<String, Integer>> wordCountMap(String line) {
            List<KeyValuePair<String, Integer>> results = new ArrayList<>();
            String[] words = line.toLowerCase().trim().split("\\s+");
            
            for (String word : words) {
                if (!word.isEmpty()) {
                    results.add(new KeyValuePair<>(word, 1));
                }
            }
            
            return results;
        }
        
        /**
         * Example word count reduce function
         */
        public static List<KeyValuePair<String, Integer>> wordCountReduce(Map<String, List<Integer>> groupedData) {
            List<KeyValuePair<String, Integer>> results = new ArrayList<>();
            
            for (Map.Entry<String, List<Integer>> entry : groupedData.entrySet()) {
                String word = entry.getKey();
                List<Integer> counts = entry.getValue();
                
                int sum = counts.stream().mapToInt(Integer::intValue).sum();
                results.add(new KeyValuePair<>(word, sum));
            }
            
            return results;
        }
    }
    
    /**
     * Represents a distributed lock for coordinating access to shared resources
     */
    public static class DistributedLock {
        private final String lockName;
        private final Map<String, Long> lockOwners;
        private final long timeoutMs;
        
        public DistributedLock(String lockName, long timeoutMs) {
            this.lockName = lockName;
            this.lockOwners = new ConcurrentHashMap<>();
            this.timeoutMs = timeoutMs;
        }
        
        public boolean acquire(String ownerId) {
            synchronized (lockOwners) {
                // Check if lock is already held by someone else
                if (!lockOwners.isEmpty() && !lockOwners.containsKey(ownerId)) {
                    // Check for expired locks
                    long currentTime = System.currentTimeMillis();
                    lockOwners.entrySet().removeIf(entry -> currentTime - entry.getValue() > timeoutMs);
                }
                
                // If still locked by someone else, fail to acquire
                if (!lockOwners.isEmpty() && !lockOwners.containsKey(ownerId)) {
                    return false;
                }
                
                // Acquire the lock
                lockOwners.put(ownerId, System.currentTimeMillis());
                return true;
            }
        }
        
        public void release(String ownerId) {
            synchronized (lockOwners) {
                lockOwners.remove(ownerId);
            }
        }
        
        public boolean isLocked() {
            synchronized (lockOwners) {
                // Clean up expired locks
                long currentTime = System.currentTimeMillis();
                lockOwners.entrySet().removeIf(entry -> currentTime - entry.getValue() > timeoutMs);
                return !lockOwners.isEmpty();
            }
        }
        
        public String getLockOwner() {
            synchronized (lockOwners) {
                if (lockOwners.isEmpty()) {
                    return null;
                }
                return lockOwners.keySet().iterator().next();
            }
        }
        
        public String getLockName() {
            return lockName;
        }
    }
    
    /**
     * Represents a distributed cache for sharing data across nodes
     */
    public static class DistributedCache<K, V> {
        private final Map<K, CacheEntry<V>> cache;
        private final long defaultTtlMs;
        private final int maxSize;
        
        private static class CacheEntry<V> {
            private final V value;
            private final long expiryTime;
            
            public CacheEntry(V value, long ttlMs) {
                this.value = value;
                this.expiryTime = System.currentTimeMillis() + ttlMs;
            }
            
            public V getValue() {
                return value;
            }
            
            public boolean isExpired() {
                return System.currentTimeMillis() > expiryTime;
            }
        }
        
        public DistributedCache(long defaultTtlMs, int maxSize) {
            this.cache = new ConcurrentHashMap<>();
            this.defaultTtlMs = defaultTtlMs;
            this.maxSize = maxSize;
        }
        
        public void put(K key, V value) {
            put(key, value, defaultTtlMs);
        }
        
        public void put(K key, V value, long ttlMs) {
            // Evict oldest entries if cache is full
            if (cache.size() >= maxSize) {
                evictOldest();
            }
            
            cache.put(key, new CacheEntry<>(value, ttlMs));
        }
        
        public V get(K key) {
            CacheEntry<V> entry = cache.get(key);
            if (entry == null) {
                return null;
            }
            
            if (entry.isExpired()) {
                cache.remove(key);
                return null;
            }
            
            return entry.getValue();
        }
        
        public void remove(K key) {
            cache.remove(key);
        }
        
        public boolean containsKey(K key) {
            return get(key) != null;
        }
        
        public int size() {
            cleanupExpired();
            return cache.size();
        }
        
        public void clear() {
            cache.clear();
        }
        
        private void cleanupExpired() {
            long currentTime = System.currentTimeMillis();
            cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }
        
        private void evictOldest() {
            if (!cache.isEmpty()) {
                K oldestKey = cache.entrySet().stream()
                    .min(Map.Entry.comparingByValue(Comparator.comparing(e -> e.expiryTime)))
                    .map(Map.Entry::getKey)
                    .orElse(null);
                
                if (oldestKey != null) {
                    cache.remove(oldestKey);
                }
            }
        }
        
        public Set<K> keySet() {
            cleanupExpired();
            return new HashSet<>(cache.keySet());
        }
    }
    
    /**
     * Represents a distributed event system for message passing between nodes
     */
    public static class DistributedEventSystem {
        public static class Event {
            private final String type;
            private final Object data;
            private final long timestamp;
            private final String sourceNode;
            
            public Event(String type, Object data, String sourceNode) {
                this.type = type;
                this.data = data;
                this.timestamp = System.currentTimeMillis();
                this.sourceNode = sourceNode;
            }
            
            public String getType() {
                return type;
            }
            
            public Object getData() {
                return data;
            }
            
            public long getTimestamp() {
                return timestamp;
            }
            
            public String getSourceNode() {
                return sourceNode;
            }
        }
        
        public interface EventListener {
            void onEvent(Event event);
        }
        
        private final Map<String, List<EventListener>> listeners;
        private final Queue<Event> eventQueue;
        private final ExecutorService executorService;
        private boolean running;
        
        public DistributedEventSystem() {
            this.listeners = new ConcurrentHashMap<>();
            this.eventQueue = new ConcurrentLinkedQueue<>();
            this.executorService = Executors.newFixedThreadPool(4);
            this.running = false;
        }
        
        public void start() {
            if (!running) {
                running = true;
                // Start event processing thread
                executorService.submit(this::processEvents);
            }
        }
        
        public void stop() {
            if (running) {
                running = false;
                executorService.shutdown();
                try {
                    if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                        executorService.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executorService.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        public void subscribe(String eventType, EventListener listener) {
            listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
        }
        
        public void unsubscribe(String eventType, EventListener listener) {
            List<EventListener> eventListeners = listeners.get(eventType);
            if (eventListeners != null) {
                eventListeners.remove(listener);
            }
        }
        
        public void publish(Event event) {
            eventQueue.offer(event);
        }
        
        private void processEvents() {
            while (running || !eventQueue.isEmpty()) {
                Event event = eventQueue.poll();
                if (event != null) {
                    dispatchEvent(event);
                } else {
                    // No events, sleep briefly
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        private void dispatchEvent(Event event) {
            List<EventListener> eventListeners = listeners.get(event.getType());
            if (eventListeners != null) {
                for (EventListener listener : eventListeners) {
                    try {
                        listener.onEvent(event);
                    } catch (Exception e) {
                        System.err.println("Error in event listener: " + e.getMessage());
                    }
                }
            }
        }
        
        public int getQueueSize() {
            return eventQueue.size();
        }
        
        public Set<String> getEventTypes() {
            return new HashSet<>(listeners.keySet());
        }
    }
}