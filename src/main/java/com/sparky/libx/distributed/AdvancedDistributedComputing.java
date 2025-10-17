package com.sparky.libx.distributed;

import com.sparky.libx.network.NetworkUtils;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Advanced distributed computing framework
 * @author Андрій Будильников
 */
public class AdvancedDistributedComputing {
    
    /**
     * Represents a node in the distributed system
     */
    public static class Node {
        private final String id;
        private final String address;
        private final int port;
        private final Set<String> clusterNodes;
        private final AtomicBoolean active;
        private ServerSocket serverSocket;
        private final ExecutorService executorService;
        private final Map<String, Long> nodeHeartbeats;
        private final Lock nodeLock;
        
        public Node(String id, String address, int port, List<String> clusterNodes) {
            this.id = id;
            this.address = address;
            this.port = port;
            this.clusterNodes = new HashSet<>(clusterNodes);
            this.active = new AtomicBoolean(false);
            this.executorService = Executors.newCachedThreadPool();
            this.nodeHeartbeats = new ConcurrentHashMap<>();
            this.nodeLock = new ReentrantLock();
        }
        
        /**
         * Starts the node
         */
        public void start() throws IOException {
            if (active.get()) {
                throw new IllegalStateException("Node is already running");
            }
            
            active.set(true);
            serverSocket = new ServerSocket(port);
            
            // Start server thread
            executorService.submit(this::serverLoop);
            
            // Start heartbeat thread
            executorService.submit(this::heartbeatLoop);
            
            System.out.println("Node " + id + " started on " + address + ":" + port);
        }
        
        /**
         * Stops the node
         */
        public void stop() {
            if (!active.get()) {
                return;
            }
            
            active.set(false);
            
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
            
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
            
            System.out.println("Node " + id + " stopped");
        }
        
        /**
         * Server loop for handling incoming connections
         */
        private void serverLoop() {
            while (active.get() && !serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    executorService.submit(() -> handleClient(clientSocket));
                } catch (IOException e) {
                    if (active.get()) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        }
        
        /**
         * Handles a client connection
         */
        private void handleClient(Socket clientSocket) {
            try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                 ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {
                
                // Read message
                Object message = in.readObject();
                
                // Process message
                Object response = processMessage(message);
                
                // Send response
                if (response != null) {
                    out.writeObject(response);
                }
                
            } catch (Exception e) {
                System.err.println("Error handling client: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing client socket: " + e.getMessage());
                }
            }
        }
        
        /**
         * Processes an incoming message
         */
        private Object processMessage(Object message) {
            if (message instanceof HeartbeatMessage) {
                HeartbeatMessage heartbeat = (HeartbeatMessage) message;
                nodeHeartbeats.put(heartbeat.getNodeId(), System.currentTimeMillis());
                return new HeartbeatResponse(id);
            } else if (message instanceof TaskMessage) {
                TaskMessage taskMsg = (TaskMessage) message;
                return executeTask(taskMsg.getTask());
            }
            
            return null;
        }
        
        /**
         * Executes a task
         */
        private Object executeTask(Task task) {
            try {
                return task.execute();
            } catch (Exception e) {
                System.err.println("Error executing task: " + e.getMessage());
                return new TaskError(e.getMessage());
            }
        }
        
        /**
         * Heartbeat loop for maintaining cluster connectivity
         */
        private void heartbeatLoop() {
            while (active.get()) {
                try {
                    sendHeartbeats();
                    Thread.sleep(1000); // Send heartbeats every second
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        /**
         * Sends heartbeats to all cluster nodes
         */
        private void sendHeartbeats() {
            HeartbeatMessage heartbeat = new HeartbeatMessage(id);
            
            for (String nodeAddress : clusterNodes) {
                if (!nodeAddress.equals(address + ":" + port)) {
                    try {
                        sendMessage(nodeAddress, heartbeat);
                    } catch (Exception e) {
                        System.err.println("Error sending heartbeat to " + nodeAddress + ": " + e.getMessage());
                    }
                }
            }
        }
        
        /**
         * Sends a message to another node
         */
        private Object sendMessage(String nodeAddress, Object message) throws Exception {
            String[] parts = nodeAddress.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            
            try (Socket socket = new Socket(host, port);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                
                // Send message
                out.writeObject(message);
                out.flush();
                
                // Read response
                return in.readObject();
            }
        }
        
        /**
         * Gets the node ID
         */
        public String getId() {
            return id;
        }
        
        /**
         * Gets the node address
         */
        public String getAddress() {
            return address;
        }
        
        /**
         * Gets the node port
         */
        public int getPort() {
            return port;
        }
        
        /**
         * Checks if the node is active
         */
        public boolean isActive() {
            return active.get();
        }
    }
    
    /**
     * Represents a heartbeat message
     */
    public static class HeartbeatMessage implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String nodeId;
        private final long timestamp;
        
        public HeartbeatMessage(String nodeId) {
            this.nodeId = nodeId;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getNodeId() {
            return nodeId;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
    }
    
    /**
     * Represents a heartbeat response
     */
    public static class HeartbeatResponse implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String nodeId;
        private final long timestamp;
        
        public HeartbeatResponse(String nodeId) {
            this.nodeId = nodeId;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getNodeId() {
            return nodeId;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
    }
    
    /**
     * Represents a task message
     */
    public static class TaskMessage implements Serializable {
        private static final long serialVersionUID = 1L;
        private final Task task;
        
        public TaskMessage(Task task) {
            this.task = task;
        }
        
        public Task getTask() {
            return task;
        }
    }
    
    /**
     * Represents a task error
     */
    public static class TaskError implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String message;
        
        public TaskError(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    /**
     * Represents a distributed task
     */
    public static abstract class Task implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String id;
        
        public Task(String id) {
            this.id = id;
        }
        
        public String getId() {
            return id;
        }
        
        public abstract Object execute() throws Exception;
    }
    
    /**
     * Represents a distributed consensus algorithm (simplified RAFT implementation)
     */
    public static class ConsensusAlgorithm {
        public enum NodeState {
            FOLLOWER, CANDIDATE, LEADER
        }
        
        private final String nodeId;
        private final List<String> clusterNodes;
        private final Map<String, Long> nodeHeartbeats;
        private final Lock lock;
        private final ScheduledExecutorService scheduler;
        private final Random random;
        private volatile NodeState state;
        private volatile String leaderId;
        private volatile long currentTerm;
        
        public ConsensusAlgorithm(String nodeId, List<String> clusterNodes) {
            this.nodeId = nodeId;
            this.clusterNodes = new ArrayList<>(clusterNodes);
            this.nodeHeartbeats = new ConcurrentHashMap<>();
            this.lock = new ReentrantLock();
            this.scheduler = Executors.newScheduledThreadPool(2);
            this.random = new Random();
            this.state = NodeState.FOLLOWER;
            this.leaderId = null;
            this.currentTerm = 0;
            
            // Start election timeout
            scheduler.scheduleWithFixedDelay(this::checkElectionTimeout, 150, 150, TimeUnit.MILLISECONDS);
            
            // Start heartbeat sending (if leader)
            scheduler.scheduleWithFixedDelay(this::sendHeartbeat, 50, 50, TimeUnit.MILLISECONDS);
        }
        
        /**
         * Checks for election timeout
         */
        private void checkElectionTimeout() {
            lock.lock();
            try {
                long now = System.currentTimeMillis();
                long lastHeartbeat = nodeHeartbeats.getOrDefault(leaderId, 0L);
                
                // If no heartbeat for a while, start election
                if (now - lastHeartbeat > 300) {
                    if (state == NodeState.FOLLOWER || state == NodeState.CANDIDATE) {
                        startElection();
                    }
                }
            } finally {
                lock.unlock();
            }
        }
        
        /**
         * Starts an election
         */
        private void startElection() {
            lock.lock();
            try {
                state = NodeState.CANDIDATE;
                currentTerm++;
                leaderId = null;
                
                int votes = 1; // Vote for self
                for (String node : clusterNodes) {
                    if (!node.equals(nodeId)) {
                        if (requestVote(node)) {
                            votes++;
                        }
                    }
                }
                
                // If we have majority of votes, become leader
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
            // send a network request to the node to request a vote
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
            // send a network request to the node with a heartbeat message
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
                    Object result = task.execute();
                    System.out.println("Executed task " + taskId + " with result: " + result);
                } catch (Exception e) {
                    System.err.println("Error executing task " + taskId + ": " + e.getMessage());
                }
            }
        }
        
        /**
         * Forwards a task to another node
         */
        private void forwardTask(String taskId, String targetNode) {
            // send a network request to the target node to execute the task
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
    public abstract static class DistributedTask extends Task {
        public DistributedTask(String id) {
            super(id);
        }
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
            // send a network notification to the waiter that the lock is available
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
            // send a network request to the node to fetch the key
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
         * Registers a local event listener
         */
        public void addListener(String eventType, EventListener listener) {
            listenersLock.lock();
            try {
                localListeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
            } finally {
                listenersLock.unlock();
            }
        }
        
        /**
         * Unregisters a local event listener
         */
        public void removeListener(String eventType, EventListener listener) {
            listenersLock.lock();
            try {
                List<EventListener> listeners = localListeners.get(eventType);
                if (listeners != null) {
                    listeners.remove(listener);
                }
            } finally {
                listenersLock.unlock();
            }
        }
        
        /**
         * Publishes an event locally
         */
        public void publishEvent(String eventType, Object eventData) {
            // Notify local listeners
            notifyLocalListeners(eventType, eventData);
            
            // Forward to other nodes
            forwardEvent(eventType, eventData);
        }
        
        /**
         * Notifies local listeners of an event
         */
        private void notifyLocalListeners(String eventType, Object eventData) {
            listenersLock.lock();
            try {
                List<EventListener> listeners = localListeners.get(eventType);
                if (listeners != null) {
                    for (EventListener listener : listeners) {
                        eventExecutor.submit(() -> listener.onEvent(eventType, eventData));
                    }
                }
            } finally {
                listenersLock.unlock();
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
            // send a network request to the node with the event
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
            // send a network request to the node with the configuration update
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
            // synchronize configuration with other nodes in the cluster
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