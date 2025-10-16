package com.sparky.libx.distributed;

import com.sparky.libx.math.Vector3D;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
import java.security.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.zip.*;
import java.util.stream.*;

/**
 * Advanced Distributed Computing Framework for Minecraft Plugins
 * Provides capabilities for distributed computing, MapReduce, consensus algorithms, and distributed systems
 * 
 * @author Андрій Будильников
 */
public class AdvancedDistributedComputing {
    
    /**
     * Represents a distributed MapReduce framework
     */
    public static class MapReduceFramework {
        private final List<String> clusterNodes;
        private final ExecutorService executorService;
        
        public MapReduceFramework(List<String> clusterNodes) {
            this.clusterNodes = new ArrayList<>(clusterNodes);
            this.executorService = Executors.newCachedThreadPool();
        }
        
        /**
         * Executes a MapReduce job
         */
        public <K, V> Map<K, List<V>> execute(MapReduceJob<K, V> job) {
            try {
                // Split input data into chunks
                List<List<String>> chunks = splitData(job.getInputData(), clusterNodes.size());
                
                // Execute map phase in parallel
                List<Future<List<KeyValuePair<K, V>>>> mapFutures = new ArrayList<>();
                for (int i = 0; i < chunks.size(); i++) {
                    final int index = i;
                    Future<List<KeyValuePair<K, V>>> future = executorService.submit(new Callable<List<KeyValuePair<K, V>>>() {
                        @Override
                        public List<KeyValuePair<K, V>> call() throws Exception {
                            return job.getMapFunction().apply(chunks.get(index));
                        }
                    });
                    mapFutures.add(future);
                }
                
                // Collect map results
                List<KeyValuePair<K, V>> mapResults = new ArrayList<>();
                for (Future<List<KeyValuePair<K, V>>> future : mapFutures) {
                    try {
                        mapResults.addAll(future.get());
                    } catch (ExecutionException e) {
                        throw new RuntimeException("Map task failed", e.getCause());
                    }
                }
                
                // Shuffle and sort by key
                Map<K, List<V>> shuffledData = shuffleAndSort(mapResults);
                
                // Execute reduce phase in parallel
                Map<K, Future<V>> reduceFutures = new HashMap<>();
                for (Map.Entry<K, List<V>> entry : shuffledData.entrySet()) {
                    final K key = entry.getKey();
                    final List<V> values = entry.getValue();
                    Future<V> future = executorService.submit(new Callable<V>() {
                        @Override
                        public V call() throws Exception {
                            return job.getReduceFunction().apply(key, values);
                        }
                    });
                    reduceFutures.put(key, future);
                }
                
                // Collect reduce results
                Map<K, List<V>> finalResults = new HashMap<>();
                for (Map.Entry<K, Future<V>> entry : reduceFutures.entrySet()) {
                    K key = entry.getKey();
                    try {
                        V reducedValue = entry.getValue().get();
                        finalResults.put(key, Arrays.asList(reducedValue));
                    } catch (ExecutionException e) {
                        throw new RuntimeException("Reduce task failed for key " + key, e.getCause());
                    }
                }
                
                return finalResults;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("MapReduce job interrupted", e);
            }
        }
        
        /**
         * Splits input data into chunks
         */
        private List<List<String>> splitData(List<String> data, int numChunks) {
            List<List<String>> chunks = new ArrayList<>();
            int chunkSize = Math.max(1, data.size() / numChunks);
            
            for (int i = 0; i < data.size(); i += chunkSize) {
                int end = Math.min(i + chunkSize, data.size());
                chunks.add(data.subList(i, end));
            }
            
            return chunks;
        }
        
        /**
         * Shuffles and sorts map results by key
         */
        private <K, V> Map<K, List<V>> shuffleAndSort(List<KeyValuePair<K, V>> mapResults) {
            Map<K, List<V>> shuffledData = new HashMap<>();
            
            for (KeyValuePair<K, V> pair : mapResults) {
                shuffledData.computeIfAbsent(pair.getKey(), k -> new ArrayList<>()).add(pair.getValue());
            }
            
            return shuffledData;
        }
        
        /**
         * Shuts down the framework
         */
        public void shutdown() {
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
    
    /**
     * Represents a MapReduce job
     */
    public static class MapReduceJob<K, V> {
        private final List<String> inputData;
        private final Function<List<String>, List<KeyValuePair<K, V>>> mapFunction;
        private final ReduceFunction<K, V> reduceFunction;
        
        public MapReduceJob(List<String> inputData, 
                          Function<List<String>, List<KeyValuePair<K, V>>> mapFunction,
                          ReduceFunction<K, V> reduceFunction) {
            this.inputData = new ArrayList<>(inputData);
            this.mapFunction = mapFunction;
            this.reduceFunction = reduceFunction;
        }
        
        public List<String> getInputData() {
            return new ArrayList<>(inputData);
        }
        
        public Function<List<String>, List<KeyValuePair<K, V>>> getMapFunction() {
            return mapFunction;
        }
        
        public ReduceFunction<K, V> getReduceFunction() {
            return reduceFunction;
        }
    }
    
    /**
     * Represents a key-value pair
     */
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
        
        @Override
        public String toString() {
            return "KeyValuePair{key=" + key + ", value=" + value + "}";
        }
    }
    
    /**
     * Functional interface for map function
     */
    @FunctionalInterface
    public interface Function<T, R> {
        R apply(T input) throws Exception;
    }
    
    /**
     * Functional interface for reduce function
     */
    @FunctionalInterface
    public interface ReduceFunction<K, V> {
        V apply(K key, List<V> values) throws Exception;
    }
    
    /**
     * Represents a distributed consensus algorithm (Raft-like)
     */
    public static class DistributedConsensus {
        private final String nodeId;
        private final List<String> clusterNodes;
        private final Map<String, Long> nodeHeartbeats;
        private final Lock lock;
        private volatile NodeState state;
        private volatile String leaderId;
        private final ScheduledExecutorService scheduler;
        private final Random random;
        
        public enum NodeState {
            FOLLOWER, CANDIDATE, LEADER
        }
        
        public DistributedConsensus(String nodeId, List<String> clusterNodes) {
            this.nodeId = nodeId;
            this.clusterNodes = new ArrayList<>(clusterNodes);
            this.nodeHeartbeats = new ConcurrentHashMap<>();
            this.lock = new ReentrantLock();
            this.state = NodeState.FOLLOWER;
            this.leaderId = null;
            this.scheduler = Executors.newScheduledThreadPool(2);
            this.random = new Random();
            
            // Initialize heartbeats
            for (String node : clusterNodes) {
                nodeHeartbeats.put(node, System.currentTimeMillis());
            }
            
            // Start consensus protocol
            startConsensus();
        }
        
        /**
         * Starts the consensus protocol
         */
        private void startConsensus() {
            // Schedule election timeout
            scheduler.scheduleWithFixedDelay(this::checkElectionTimeout, 
                                           150 + random.nextInt(150), // 150-300ms
                                           100, // Check every 100ms
                                           TimeUnit.MILLISECONDS);
            
            // Schedule heartbeat (if leader)
            scheduler.scheduleWithFixedDelay(this::sendHeartbeat, 
                                           50, 50, TimeUnit.MILLISECONDS);
        }
        
        /**
         * Checks for election timeout
         */
        private void checkElectionTimeout() {
            lock.lock();
            try {
                if (state == NodeState.LEADER) {
                    return; // Leader doesn't timeout
                }
                
                long lastHeartbeat = nodeHeartbeats.getOrDefault(leaderId, 0L);
                long now = System.currentTimeMillis();
                
                // If no heartbeat for more than 500ms, start election
                if (now - lastHeartbeat > 500) {
                    startElection();
                }
            } finally {
                lock.unlock();
            }
        }
        
        /**
         * Starts a new election
         */
        private void startElection() {
            lock.lock();
            try {
                if (state != NodeState.CANDIDATE) {
                    state = NodeState.CANDIDATE;
                    leaderId = null;
                    System.out.println(nodeId + " became candidate");
                }
                
                // Request votes from other nodes
                int votes = 1; // Vote for self
                for (String node : clusterNodes) {
                    if (!node.equals(nodeId) && requestVote(node)) {
                        votes++;
                    }
                }
                
                // If majority votes, become leader
                if (votes > clusterNodes.size() / 2) {
                    becomeLeader();
                }
            } finally {
                lock.unlock();
            }
        }
        
        /**
         * Requests a vote from another node
         */
        private boolean requestVote(String node) {
            // In a real implementation, this would send a network request
            System.out.println(nodeId + " requesting vote from " + node);
            // Simulate random vote result
            return random.nextBoolean();
        }
        
        /**
         * Becomes the leader
         */
        private void becomeLeader() {
            lock.lock();
            try {
                state = NodeState.LEADER;
                leaderId = nodeId;
                System.out.println(nodeId + " became leader");
            } finally {
                lock.unlock();
            }
        }
        
        /**
         * Sends heartbeat to all nodes
         */
        private void sendHeartbeat() {
            lock.lock();
            try {
                if (state == NodeState.LEADER) {
                    long now = System.currentTimeMillis();
                    for (String node : clusterNodes) {
                        if (!node.equals(nodeId)) {
                            sendHeartbeatToNode(node);
                            nodeHeartbeats.put(node, now);
                        }
                    }
                }
            } finally {
                lock.unlock();
            }
        }
        
        /**
         * Sends heartbeat to a specific node
         */
        private void sendHeartbeatToNode(String node) {
            // In a real implementation, this would send a network request
            System.out.println(nodeId + " sending heartbeat to " + node);
        }
        
        /**
         * Receives heartbeat from another node
         */
        public void receiveHeartbeat(String leader) {
            lock.lock();
            try {
                nodeHeartbeats.put(leader, System.currentTimeMillis());
                
                if (state == NodeState.CANDIDATE || 
                    (state == NodeState.FOLLOWER && !leader.equals(leaderId))) {
                    state = NodeState.FOLLOWER;
                    leaderId = leader;
                    System.out.println(nodeId + " following leader " + leader);
                }
            } finally {
                lock.unlock();
            }
        }
        
        /**
         * Gets the current node state
         */
        public NodeState getState() {
            return state;
        }
        
        /**
         * Gets the current leader
         */
        public String getLeader() {
            return leaderId;
        }
        
        /**
         * Shuts down the consensus algorithm
         */
        public void shutdown() {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Represents a distributed task scheduler
     */
    public static class DistributedTaskScheduler {
        private final String nodeId;
        private final List<String> clusterNodes;
        private final Map<String, Task> taskRegistry;
        private final PriorityQueue<ScheduledTask> taskQueue;
        private final Lock queueLock;
        private final ScheduledExecutorService scheduler;
        private final Random random;
        
        public DistributedTaskScheduler(String nodeId, List<String> clusterNodes) {
            this.nodeId = nodeId;
            this.clusterNodes = new ArrayList<>(clusterNodes);
            this.taskRegistry = new ConcurrentHashMap<>();
            this.taskQueue = new PriorityQueue<>();
            this.queueLock = new ReentrantLock();
            this.scheduler = Executors.newScheduledThreadPool(2);
            this.random = new Random();
            
            // Start task processing
            startTaskProcessing();
        }
        
        /**
         * Starts task processing
         */
        private void startTaskProcessing() {
            scheduler.scheduleWithFixedDelay(this::processTasks, 100, 100, TimeUnit.MILLISECONDS);
        }
        
        /**
         * Registers a task
         */
        public void registerTask(Task task) {
            taskRegistry.put(task.getId(), task);
            System.out.println("Registered task " + task.getId());
        }
        
        /**
         * Schedules a task
         */
        public void scheduleTask(String taskId, long delay, TimeUnit unit) {
            Task task = taskRegistry.get(taskId);
            if (task == null) {
                throw new IllegalArgumentException("Task not found: " + taskId);
            }
            
            long scheduledTime = System.currentTimeMillis() + unit.toMillis(delay);
            String assignedNode = selectNodeForTask(taskId);
            
            queueLock.lock();
            try {
                taskQueue.offer(new ScheduledTask(taskId, scheduledTime, assignedNode));
                System.out.println("Scheduled task " + taskId + " on node " + assignedNode + 
                                 " at " + new Date(scheduledTime));
            } finally {
                queueLock.unlock();
            }
        }
        
        /**
         * Selects a node to execute a task
         */
        private String selectNodeForTask(String taskId) {
            // Simple round-robin selection
            int index = taskId.hashCode() % clusterNodes.size();
            return clusterNodes.get(Math.abs(index));
        }
        
        /**
         * Processes scheduled tasks
         */
        private void processTasks() {
            long now = System.currentTimeMillis();
            
            queueLock.lock();
            try {
                while (!taskQueue.isEmpty() && taskQueue.peek().getScheduledTime() <= now) {
                    ScheduledTask scheduledTask = taskQueue.poll();
                    
                    if (scheduledTask.getAssignedNode().equals(nodeId)) {
                        // Execute task locally
                        executeTask(scheduledTask.getTaskId());
                    } else {
                        // Forward task to assigned node
                        forwardTask(scheduledTask.getTaskId(), scheduledTask.getAssignedNode());
                    }
                }
            } finally {
                queueLock.unlock();
            }
        }
        
        /**
         * Executes a task locally
         */
        private void executeTask(String taskId) {
            Task task = taskRegistry.get(taskId);
            if (task != null) {
                try {
                    System.out.println(nodeId + " executing task " + taskId);
                    task.execute();
                    System.out.println(nodeId + " completed task " + taskId);
                } catch (Exception e) {
                    System.err.println(nodeId + " failed to execute task " + taskId + ": " + e.getMessage());
                }
            }
        }
        
        /**
         * Forwards a task to another node
         */
        private void forwardTask(String taskId, String targetNode) {
            // In a real implementation, this would send a network request
            System.out.println(nodeId + " forwarding task " + taskId + " to " + targetNode);
        }
        
        /**
         * Represents a scheduled task
         */
        private static class ScheduledTask implements Comparable<ScheduledTask> {
            private final String taskId;
            private final long scheduledTime;
            private final String assignedNode;
            
            public ScheduledTask(String taskId, long scheduledTime, String assignedNode) {
                this.taskId = taskId;
                this.scheduledTime = scheduledTime;
                this.assignedNode = assignedNode;
            }
            
            public String getTaskId() {
                return taskId;
            }
            
            public long getScheduledTime() {
                return scheduledTime;
            }
            
            public String getAssignedNode() {
                return assignedNode;
            }
            
            @Override
            public int compareTo(ScheduledTask other) {
                return Long.compare(this.scheduledTime, other.scheduledTime);
            }
        }
        
        /**
         * Shuts down the task scheduler
         */
        public void shutdown() {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Represents a distributed task
     */
    public abstract static class Task {
        private final String id;
        
        public Task(String id) {
            this.id = id;
        }
        
        public String getId() {
            return id;
        }
        
        public abstract void execute();
    }
    
    /**
     * Represents a distributed lock service
     */
    public static class DistributedLockService {
        private final String nodeId;
        private final List<String> clusterNodes;
        private final Map<String, LockInfo> locks;
        private final Lock serviceLock;
        private final ScheduledExecutorService leaseScheduler;
        
        private static class LockInfo {
            String holder;
            long leaseExpiration;
            List<String> waiters;
            
            LockInfo(String holder, long leaseExpiration) {
                this.holder = holder;
                this.leaseExpiration = leaseExpiration;
                this.waiters = new ArrayList<>();
            }
        }
        
        public DistributedLockService(String nodeId, List<String> clusterNodes) {
            this.nodeId = nodeId;
            this.clusterNodes = new ArrayList<>(clusterNodes);
            this.locks = new ConcurrentHashMap<>();
            this.serviceLock = new ReentrantLock();
            this.leaseScheduler = Executors.newScheduledThreadPool(1);
            
            // Start lease renewal
            leaseScheduler.scheduleWithFixedDelay(this::renewLeases, 1000, 1000, TimeUnit.MILLISECONDS);
        }
        
        /**
         * Acquires a distributed lock
         */
        public boolean acquireLock(String lockName, long timeout, TimeUnit unit) {
            long deadline = System.currentTimeMillis() + unit.toMillis(timeout);
            
            while (System.currentTimeMillis() < deadline) {
                if (tryAcquireLock(lockName)) {
                    return true;
                }
                
                try {
                    Thread.sleep(100); // Wait before retry
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            
            return false;
        }
        
        /**
         * Tries to acquire a lock immediately
         */
        private boolean tryAcquireLock(String lockName) {
            serviceLock.lock();
            try {
                LockInfo lockInfo = locks.get(lockName);
                
                if (lockInfo == null) {
                    // Lock is free, acquire it
                    locks.put(lockName, new LockInfo(nodeId, System.currentTimeMillis() + 30000)); // 30 second lease
                    System.out.println(nodeId + " acquired lock " + lockName);
                    return true;
                } else if (lockInfo.holder.equals(nodeId)) {
                    // Already hold the lock, extend lease
                    lockInfo.leaseExpiration = System.currentTimeMillis() + 30000;
                    return true;
                } else if (System.currentTimeMillis() > lockInfo.leaseExpiration) {
                    // Lease expired, acquire lock
                    lockInfo.holder = nodeId;
                    lockInfo.leaseExpiration = System.currentTimeMillis() + 30000;
                    System.out.println(nodeId + " acquired expired lock " + lockName);
                    return true;
                } else {
                    // Lock held by another node, add to waiters
                    if (!lockInfo.waiters.contains(nodeId)) {
                        lockInfo.waiters.add(nodeId);
                    }
                    return false;
                }
            } finally {
                serviceLock.unlock();
            }
        }
        
        /**
         * Releases a distributed lock
         */
        public void releaseLock(String lockName) {
            serviceLock.lock();
            try {
                LockInfo lockInfo = locks.get(lockName);
                if (lockInfo != null && lockInfo.holder.equals(nodeId)) {
                    // Notify waiters
                    for (String waiter : lockInfo.waiters) {
                        notifyWaiter(lockName, waiter);
                    }
                    
                    locks.remove(lockName);
                    System.out.println(nodeId + " released lock " + lockName);
                }
            } finally {
                serviceLock.unlock();
            }
        }
        
        /**
         * Notifies a waiter that a lock is available
         */
        private void notifyWaiter(String lockName, String waiter) {
            // In a real implementation, this would send a network notification
            System.out.println(nodeId + " notifying " + waiter + " that lock " + lockName + " is available");
        }
        
        /**
         * Renews leases for locks held by this node
         */
        private void renewLeases() {
            long now = System.currentTimeMillis();
            
            serviceLock.lock();
            try {
                for (Map.Entry<String, LockInfo> entry : locks.entrySet()) {
                    String lockName = entry.getKey();
                    LockInfo lockInfo = entry.getValue();
                    
                    if (lockInfo.holder.equals(nodeId) && now > lockInfo.leaseExpiration - 10000) {
                        // Renew lease 10 seconds before expiration
                        lockInfo.leaseExpiration = now + 30000;
                        System.out.println(nodeId + " renewed lease for lock " + lockName);
                    }
                }
            } finally {
                serviceLock.unlock();
            }
        }
        
        /**
         * Shuts down the lock service
         */
        public void shutdown() {
            leaseScheduler.shutdown();
            try {
                if (!leaseScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    leaseScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                leaseScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Represents a distributed cache
     */
    public static class DistributedCache<K, V> {
        private final String nodeId;
        private final List<String> clusterNodes;
        private final Map<K, CacheEntry<V>> localCache;
        private final int maxSize;
        private final Lock cacheLock;
        
        private static class CacheEntry<V> {
            V value;
            long timestamp;
            long ttl;
            
            CacheEntry(V value, long ttl) {
                this.value = value;
                this.timestamp = System.currentTimeMillis();
                this.ttl = ttl;
            }
            
            boolean isExpired() {
                return System.currentTimeMillis() - timestamp > ttl;
            }
        }
        
        public DistributedCache(String nodeId, List<String> clusterNodes, int maxSize) {
            this.nodeId = nodeId;
            this.clusterNodes = new ArrayList<>(clusterNodes);
            this.localCache = new LinkedHashMap<K, CacheEntry<V>>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<K, CacheEntry<V>> eldest) {
                    return size() > maxSize;
                }
            };
            this.maxSize = maxSize;
            this.cacheLock = new ReentrantLock();
        }
        
        /**
         * Puts a value in the cache
         */
        public void put(K key, V value, long ttl, TimeUnit unit) {
            cacheLock.lock();
            try {
                localCache.put(key, new CacheEntry<>(value, unit.toMillis(ttl)));
                System.out.println(nodeId + " cached key " + key);
            } finally {
                cacheLock.unlock();
            }
        }
        
        /**
         * Gets a value from the cache
         */
        public V get(K key) {
            cacheLock.lock();
            try {
                CacheEntry<V> entry = localCache.get(key);
                if (entry != null) {
                    if (entry.isExpired()) {
                        localCache.remove(key);
                        System.out.println(nodeId + " removed expired key " + key);
                        return null;
                    }
                    return entry.value;
                }
                
                // Key not found locally, try other nodes
                return fetchFromOtherNodes(key);
            } finally {
                cacheLock.unlock();
            }
        }
        
        /**
         * Fetches a value from other nodes
         */
        private V fetchFromOtherNodes(K key) {
            for (String node : clusterNodes) {
                if (!node.equals(nodeId)) {
                    V value = fetchFromNode(node, key);
                    if (value != null) {
                        // Cache the value locally
                        put(key, value, 60, TimeUnit.SECONDS); // Cache for 60 seconds
                        return value;
                    }
                }
            }
            return null;
        }
        
        /**
         * Fetches a value from a specific node
         */
        private V fetchFromNode(String node, K key) {
            // In a real implementation, this would send a network request
            System.out.println(nodeId + " fetching key " + key + " from node " + node);
            return null; // Simulated response
        }
        
        /**
         * Removes a value from the cache
         */
        public void remove(K key) {
            cacheLock.lock();
            try {
                localCache.remove(key);
                System.out.println(nodeId + " removed key " + key);
            } finally {
                cacheLock.unlock();
            }
        }
        
        /**
         * Clears the cache
         */
        public void clear() {
            cacheLock.lock();
            try {
                localCache.clear();
                System.out.println(nodeId + " cleared cache");
            } finally {
                cacheLock.unlock();
            }
        }
        
        /**
         * Gets the cache size
         */
        public int size() {
            cacheLock.lock();
            try {
                return localCache.size();
            } finally {
                cacheLock.unlock();
            }
        }
    }
    
    /**
     * Represents a distributed event bus
     */
    public static class DistributedEventBus {
        private final String nodeId;
        private final List<String> clusterNodes;
        private final Map<String, List<EventListener>> localListeners;
        private final Lock listenersLock;
        private final ExecutorService eventExecutor;
        
        public interface EventListener {
            void onEvent(String eventType, Object eventData);
        }
        
        public DistributedEventBus(String nodeId, List<String> clusterNodes) {
            this.nodeId = nodeId;
            this.clusterNodes = new ArrayList<>(clusterNodes);
            this.localListeners = new ConcurrentHashMap<>();
            this.listenersLock = new ReentrantLock();
            this.eventExecutor = Executors.newCachedThreadPool();
        }
        
        /**
         * Registers an event listener
         */
        public void addListener(String eventType, EventListener listener) {
            localListeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
            System.out.println(nodeId + " registered listener for event " + eventType);
        }
        
        /**
         * Removes an event listener
         */
        public void removeListener(String eventType, EventListener listener) {
            List<EventListener> listeners = localListeners.get(eventType);
            if (listeners != null) {
                listeners.remove(listener);
                System.out.println(nodeId + " removed listener for event " + eventType);
            }
        }
        
        /**
         * Publishes an event
         */
        public void publishEvent(String eventType, Object eventData) {
            // Notify local listeners
            notifyLocalListeners(eventType, eventData);
            
            // Forward event to other nodes
            forwardEvent(eventType, eventData);
        }
        
        /**
         * Notifies local listeners of an event
         */
        private void notifyLocalListeners(String eventType, Object eventData) {
            List<EventListener> listeners = localListeners.get(eventType);
            if (listeners != null) {
                for (EventListener listener : listeners) {
                    eventExecutor.submit(() -> {
                        try {
                            listener.onEvent(eventType, eventData);
                        } catch (Exception e) {
                            System.err.println(nodeId + " error in event listener: " + e.getMessage());
                        }
                    });
                }
            }
        }
        
        /**
         * Forwards an event to other nodes
         */
        private void forwardEvent(String eventType, Object eventData) {
            for (String node : clusterNodes) {
                if (!node.equals(nodeId)) {
                    forwardEventToNode(node, eventType, eventData);
                }
            }
        }
        
        /**
         * Forwards an event to a specific node
         */
        private void forwardEventToNode(String node, String eventType, Object eventData) {
            // In a real implementation, this would send a network request
            System.out.println(nodeId + " forwarding event " + eventType + " to node " + node);
        }
        
        /**
         * Receives an event from another node
         */
        public void receiveEvent(String eventType, Object eventData) {
            System.out.println(nodeId + " received event " + eventType + " from remote node");
            notifyLocalListeners(eventType, eventData);
        }
        
        /**
         * Shuts down the event bus
         */
        public void shutdown() {
            eventExecutor.shutdown();
            try {
                if (!eventExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    eventExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                eventExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Represents a distributed configuration service
     */
    public static class DistributedConfigurationService {
        private final String nodeId;
        private final List<String> clusterNodes;
        private final Map<String, String> localConfig;
        private final Lock configLock;
        private final ScheduledExecutorService syncScheduler;
        
        public DistributedConfigurationService(String nodeId, List<String> clusterNodes) {
            this.nodeId = nodeId;
            this.clusterNodes = new ArrayList<>(clusterNodes);
            this.localConfig = new ConcurrentHashMap<>();
            this.configLock = new ReentrantLock();
            this.syncScheduler = Executors.newScheduledThreadPool(1);
            
            // Start periodic sync
            syncScheduler.scheduleWithFixedDelay(this::syncWithCluster, 5000, 5000, TimeUnit.MILLISECONDS);
        }
        
        /**
         * Sets a configuration value
         */
        public void setConfig(String key, String value) {
            configLock.lock();
            try {
                localConfig.put(key, value);
                System.out.println(nodeId + " set config " + key + " = " + value);
                
                // Propagate to other nodes
                propagateConfig(key, value);
            } finally {
                configLock.unlock();
            }
        }
        
        /**
         * Gets a configuration value
         */
        public String getConfig(String key) {
            configLock.lock();
            try {
                return localConfig.get(key);
            } finally {
                configLock.unlock();
            }
        }
        
        /**
         * Gets all configuration values
         */
        public Map<String, String> getAllConfig() {
            configLock.lock();
            try {
                return new HashMap<>(localConfig);
            } finally {
                configLock.unlock();
            }
        }
        
        /**
         * Propagates a configuration change to other nodes
         */
        private void propagateConfig(String key, String value) {
            for (String node : clusterNodes) {
                if (!node.equals(nodeId)) {
                    sendConfigUpdate(node, key, value);
                }
            }
        }
        
        /**
         * Sends a configuration update to a node
         */
        private void sendConfigUpdate(String node, String key, String value) {
            // In a real implementation, this would send a network request
            System.out.println(nodeId + " sending config update to " + node + ": " + key + " = " + value);
        }
        
        /**
         * Receives a configuration update from another node
         */
        public void receiveConfigUpdate(String key, String value) {
            configLock.lock();
            try {
                localConfig.put(key, value);
                System.out.println(nodeId + " received config update: " + key + " = " + value);
            } finally {
                configLock.unlock();
            }
        }
        
        /**
         * Synchronizes configuration with the cluster
         */
        private void syncWithCluster() {
            // In a real implementation, this would synchronize with other nodes
            System.out.println(nodeId + " syncing configuration with cluster");
        }
        
        /**
         * Shuts down the configuration service
         */
        public void shutdown() {
            syncScheduler.shutdown();
            try {
                if (!syncScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    syncScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                syncScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}