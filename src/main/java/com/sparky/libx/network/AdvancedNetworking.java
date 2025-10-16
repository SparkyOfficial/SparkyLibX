package com.sparky.libx.network;

import com.sparky.libx.math.Vector3D;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.*;
import java.nio.*;
import java.nio.channels.*;
import java.security.*;
import javax.net.ssl.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.util.zip.*;

/**
 * Advanced Networking Framework for Minecraft Plugins
 * Provides capabilities for TCP/UDP communication, HTTP clients, distributed systems, and network protocols
 * 
 * @author Андрій Будильников
 */
public class AdvancedNetworking {
    
    /**
     * Represents a TCP server
     */
    public static class TCPServer {
        private final int port;
        private final ServerSocket serverSocket;
        private final ExecutorService threadPool;
        private final List<ClientHandler> clients;
        private final Lock clientsLock;
        private volatile boolean running;
        
        public TCPServer(int port) throws IOException {
            this.port = port;
            this.serverSocket = new ServerSocket(port);
            this.threadPool = Executors.newCachedThreadPool();
            this.clients = new ArrayList<>();
            this.clientsLock = new ReentrantLock();
            this.running = false;
        }
        
        /**
         * Starts the server
         */
        public void start() {
            if (running) {
                throw new IllegalStateException("Server is already running");
            }
            
            running = true;
            threadPool.submit(this::acceptClients);
            System.out.println("TCP Server started on port " + port);
        }
        
        /**
         * Stops the server
         */
        public void stop() {
            if (!running) {
                return;
            }
            
            running = false;
            
            try {
                // Close all client connections
                clientsLock.lock();
                try {
                    for (ClientHandler client : clients) {
                        client.disconnect();
                    }
                    clients.clear();
                } finally {
                    clientsLock.unlock();
                }
                
                // Close server socket
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error stopping server: " + e.getMessage());
            }
            
            // Shutdown thread pool
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
            
            System.out.println("TCP Server stopped");
        }
        
        /**
         * Accepts incoming client connections
         */
        private void acceptClients() {
            while (running && !serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    
                    clientsLock.lock();
                    try {
                        clients.add(clientHandler);
                    } finally {
                        clientsLock.unlock();
                    }
                    
                    threadPool.submit(clientHandler);
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error accepting client: " + e.getMessage());
                    }
                }
            }
        }
        
        /**
         * Sends a message to all connected clients
         */
        public void broadcast(String message) {
            clientsLock.lock();
            try {
                Iterator<ClientHandler> iterator = clients.iterator();
                while (iterator.hasNext()) {
                    ClientHandler client = iterator.next();
                    if (!client.sendMessage(message)) {
                        iterator.remove();
                    }
                }
            } finally {
                clientsLock.unlock();
            }
        }
        
        /**
         * Gets the number of connected clients
         */
        public int getClientCount() {
            clientsLock.lock();
            try {
                return clients.size();
            } finally {
                clientsLock.unlock();
            }
        }
        
        /**
         * Handles a client connection
         */
        private class ClientHandler implements Runnable {
            private final Socket socket;
            private final BufferedReader reader;
            private final PrintWriter writer;
            private volatile boolean connected;
            
            public ClientHandler(Socket socket) throws IOException {
                this.socket = socket;
                this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.writer = new PrintWriter(socket.getOutputStream(), true);
                this.connected = true;
            }
            
            @Override
            public void run() {
                try {
                    String inputLine;
                    while (connected && (inputLine = reader.readLine()) != null) {
                        // Process client message
                        handleMessage(inputLine);
                    }
                } catch (IOException e) {
                    if (connected) {
                        System.err.println("Error reading from client: " + e.getMessage());
                    }
                } finally {
                    disconnect();
                }
            }
            
            /**
             * Handles a message from the client
             */
            private void handleMessage(String message) {
                System.out.println("Received from client: " + message);
                // Echo the message back to the client
                sendMessage("Echo: " + message);
            }
            
            /**
             * Sends a message to the client
             */
            public boolean sendMessage(String message) {
                if (!connected) {
                    return false;
                }
                
                try {
                    writer.println(message);
                    return true;
                } catch (Exception e) {
                    System.err.println("Error sending message to client: " + e.getMessage());
                    disconnect();
                    return false;
                }
            }
            
            /**
             * Disconnects the client
             */
            public void disconnect() {
                if (!connected) {
                    return;
                }
                
                connected = false;
                
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println("Error closing reader: " + e.getMessage());
                }
                
                writer.close();
                
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing socket: " + e.getMessage());
                }
                
                // Remove client from server list
                clientsLock.lock();
                try {
                    clients.remove(this);
                } finally {
                    clientsLock.unlock();
                }
                
                System.out.println("Client disconnected");
            }
        }
    }
    
    /**
     * Represents a TCP client
     */
    public static class TCPClient {
        private Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;
        private volatile boolean connected;
        
        /**
         * Connects to a server
         */
        public void connect(String host, int port) throws IOException {
            if (connected) {
                throw new IllegalStateException("Client is already connected");
            }
            
            socket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            connected = true;
            
            System.out.println("Connected to server at " + host + ":" + port);
        }
        
        /**
         * Disconnects from the server
         */
        public void disconnect() {
            if (!connected) {
                return;
            }
            
            connected = false;
            
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing reader: " + e.getMessage());
            }
            
            if (writer != null) {
                writer.close();
            }
            
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
            
            System.out.println("Disconnected from server");
        }
        
        /**
         * Sends a message to the server
         */
        public boolean sendMessage(String message) {
            if (!connected) {
                throw new IllegalStateException("Client is not connected");
            }
            
            try {
                writer.println(message);
                return true;
            } catch (Exception e) {
                System.err.println("Error sending message: " + e.getMessage());
                disconnect();
                return false;
            }
        }
        
        /**
         * Receives a message from the server
         */
        public String receiveMessage() throws IOException {
            if (!connected) {
                throw new IllegalStateException("Client is not connected");
            }
            
            return reader.readLine();
        }
        
        /**
         * Checks if the client is connected
         */
        public boolean isConnected() {
            return connected;
        }
    }
    
    /**
     * Represents a UDP server
     */
    public static class UDPServer {
        private final int port;
        private final DatagramSocket socket;
        private final ExecutorService threadPool;
        private volatile boolean running;
        
        public UDPServer(int port) throws SocketException {
            this.port = port;
            this.socket = new DatagramSocket(port);
            this.threadPool = Executors.newCachedThreadPool();
            this.running = false;
        }
        
        /**
         * Starts the server
         */
        public void start() {
            if (running) {
                throw new IllegalStateException("Server is already running");
            }
            
            running = true;
            threadPool.submit(this::receivePackets);
            System.out.println("UDP Server started on port " + port);
        }
        
        /**
         * Stops the server
         */
        public void stop() {
            if (!running) {
                return;
            }
            
            running = false;
            socket.close();
            
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
            
            System.out.println("UDP Server stopped");
        }
        
        /**
         * Receives UDP packets
         */
        private void receivePackets() {
            byte[] buffer = new byte[1024];
            
            while (running && !socket.isClosed()) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    
                    // Process packet in a separate thread
                    threadPool.submit(() -> processPacket(packet));
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error receiving packet: " + e.getMessage());
                    }
                }
            }
        }
        
        /**
         * Processes a received packet
         */
        private void processPacket(DatagramPacket packet) {
            String message = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Received UDP packet from " + packet.getAddress() + ":" + packet.getPort() + " - " + message);
            
            // Echo the message back
            try {
                byte[] responseData = ("Echo: " + message).getBytes();
                DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, 
                                                                  packet.getAddress(), packet.getPort());
                socket.send(responsePacket);
            } catch (IOException e) {
                System.err.println("Error sending response: " + e.getMessage());
            }
        }
    }
    
    /**
     * Represents a UDP client
     */
    public static class UDPClient {
        private final DatagramSocket socket;
        private final int serverPort;
        private final InetAddress serverAddress;
        
        public UDPClient(String serverHost, int serverPort) throws UnknownHostException, SocketException {
            this.socket = new DatagramSocket();
            this.serverPort = serverPort;
            this.serverAddress = InetAddress.getByName(serverHost);
        }
        
        /**
         * Sends a message to the server
         */
        public void sendMessage(String message) throws IOException {
            byte[] data = message.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, serverPort);
            socket.send(packet);
        }
        
        /**
         * Receives a message from the server
         */
        public String receiveMessage() throws IOException {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            return new String(packet.getData(), 0, packet.getLength());
        }
        
        /**
         * Closes the client
         */
        public void close() {
            socket.close();
        }
    }
    
    /**
     * Represents an HTTP client
     */
    public static class HTTPClient {
        private final int timeout;
        
        public HTTPClient() {
            this(5000); // 5 second default timeout
        }
        
        public HTTPClient(int timeout) {
            this.timeout = timeout;
        }
        
        /**
         * Sends an HTTP GET request
         */
        public HTTPResponse get(String url) throws IOException {
            return sendRequest("GET", url, null, null);
        }
        
        /**
         * Sends an HTTP POST request
         */
        public HTTPResponse post(String url, String contentType, String body) throws IOException {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", contentType);
            return sendRequest("POST", url, headers, body);
        }
        
        /**
         * Sends an HTTP PUT request
         */
        public HTTPResponse put(String url, String contentType, String body) throws IOException {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", contentType);
            return sendRequest("PUT", url, headers, body);
        }
        
        /**
         * Sends an HTTP DELETE request
         */
        public HTTPResponse delete(String url) throws IOException {
            return sendRequest("DELETE", url, null, null);
        }
        
        /**
         * Sends an HTTP request
         */
        private HTTPResponse sendRequest(String method, String url, Map<String, String> headers, String body) throws IOException {
            URL urlObj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            
            try {
                // Set request method
                connection.setRequestMethod(method);
                
                // Set timeout
                connection.setConnectTimeout(timeout);
                connection.setReadTimeout(timeout);
                
                // Set headers
                if (headers != null) {
                    for (Map.Entry<String, String> entry : headers.entrySet()) {
                        connection.setRequestProperty(entry.getKey(), entry.getValue());
                    }
                }
                
                // Set request body for POST/PUT
                if (body != null && ("POST".equals(method) || "PUT".equals(method))) {
                    connection.setDoOutput(true);
                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] input = body.getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }
                }
                
                // Get response
                int statusCode = connection.getResponseCode();
                String statusMessage = connection.getResponseMessage();
                
                // Read response body
                String responseBody;
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    responseBody = response.toString();
                } catch (IOException e) {
                    // Try to read error stream
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(
                                    connection.getErrorStream(), "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        responseBody = response.toString();
                    } catch (IOException e2) {
                        responseBody = "";
                    }
                }
                
                // Get response headers
                Map<String, List<String>> responseHeaders = connection.getHeaderFields();
                
                return new HTTPResponse(statusCode, statusMessage, responseBody, responseHeaders);
            } finally {
                connection.disconnect();
            }
        }
    }
    
    /**
     * Represents an HTTP response
     */
    public static class HTTPResponse {
        private final int statusCode;
        private final String statusMessage;
        private final String body;
        private final Map<String, List<String>> headers;
        
        public HTTPResponse(int statusCode, String statusMessage, String body, Map<String, List<String>> headers) {
            this.statusCode = statusCode;
            this.statusMessage = statusMessage;
            this.body = body;
            this.headers = headers;
        }
        
        public int getStatusCode() {
            return statusCode;
        }
        
        public String getStatusMessage() {
            return statusMessage;
        }
        
        public String getBody() {
            return body;
        }
        
        public Map<String, List<String>> getHeaders() {
            return headers;
        }
        
        public String getHeader(String name) {
            List<String> values = headers.get(name);
            return values != null && !values.isEmpty() ? values.get(0) : null;
        }
        
        public boolean isSuccess() {
            return statusCode >= 200 && statusCode < 300;
        }
        
        @Override
        public String toString() {
            return "HTTPResponse{statusCode=" + statusCode + ", statusMessage='" + statusMessage + 
                   "', body='" + body + "'}";
        }
    }
    
    /**
     * Represents a WebSocket client
     */
    public static class WebSocketClient {
        private final URI uri;
        private Session session;
        private volatile boolean connected;
        
        public WebSocketClient(String uri) throws URISyntaxException {
            this.uri = new URI(uri);
        }
        
        /**
         * Connects to the WebSocket server
         */
        public void connect() throws IOException {
            if (connected) {
                throw new IllegalStateException("Client is already connected");
            }
            
            // Note: This is a simplified implementation
            // In practice, you would use a WebSocket library like Java-WebSocket
            System.out.println("Connecting to WebSocket at " + uri);
            connected = true;
        }
        
        /**
         * Sends a message to the server
         */
        public void sendMessage(String message) {
            if (!connected) {
                throw new IllegalStateException("Client is not connected");
            }
            
            System.out.println("Sending WebSocket message: " + message);
        }
        
        /**
         * Disconnects from the server
         */
        public void disconnect() {
            if (!connected) {
                return;
            }
            
            connected = false;
            System.out.println("Disconnected from WebSocket");
        }
        
        /**
         * Checks if the client is connected
         */
        public boolean isConnected() {
            return connected;
        }
        
        /**
         * Represents a WebSocket session
         */
        private static class Session {
            // Implementation would depend on the WebSocket library used
        }
    }
    
    /**
     * Represents a distributed key-value store
     */
    public static class DistributedKeyValueStore {
        private final Map<String, String> localStore;
        private final List<String> clusterNodes;
        private final String nodeId;
        private final Lock storeLock;
        
        public DistributedKeyValueStore(String nodeId) {
            this.localStore = new ConcurrentHashMap<>();
            this.clusterNodes = new CopyOnWriteArrayList<>();
            this.nodeId = nodeId;
            this.storeLock = new ReentrantLock();
        }
        
        /**
         * Adds a node to the cluster
         */
        public void addNode(String nodeAddress) {
            clusterNodes.add(nodeAddress);
            System.out.println("Added node " + nodeAddress + " to cluster");
        }
        
        /**
         * Removes a node from the cluster
         */
        public void removeNode(String nodeAddress) {
            clusterNodes.remove(nodeAddress);
            System.out.println("Removed node " + nodeAddress + " from cluster");
        }
        
        /**
         * Puts a key-value pair in the store
         */
        public void put(String key, String value) {
            // Determine which node should store this key (consistent hashing)
            String targetNode = getTargetNode(key);
            
            if (nodeId.equals(targetNode)) {
                // Store locally
                localStore.put(key, value);
                System.out.println("Stored key " + key + " locally");
            } else {
                // Forward to target node
                forwardPut(targetNode, key, value);
            }
        }
        
        /**
         * Gets a value from the store
         */
        public String get(String key) {
            // Check local store first
            if (localStore.containsKey(key)) {
                return localStore.get(key);
            }
            
            // Check other nodes
            for (String node : clusterNodes) {
                if (!node.equals(nodeId)) {
                    String value = fetchFromNode(node, key);
                    if (value != null) {
                        return value;
                    }
                }
            }
            
            return null;
        }
        
        /**
         * Removes a key-value pair from the store
         */
        public void remove(String key) {
            // Determine which node stores this key
            String targetNode = getTargetNode(key);
            
            if (nodeId.equals(targetNode)) {
                // Remove locally
                localStore.remove(key);
                System.out.println("Removed key " + key + " locally");
            } else {
                // Forward to target node
                forwardRemove(targetNode, key);
            }
        }
        
        /**
         * Determines which node should store a key
         */
        private String getTargetNode(String key) {
            // Simple hash-based distribution
            int hash = key.hashCode();
            int index = Math.abs(hash) % (clusterNodes.size() + 1); // +1 for local node
            
            if (index == clusterNodes.size()) {
                return nodeId; // Local node
            } else {
                return clusterNodes.get(index);
            }
        }
        
        /**
         * Forwards a put operation to another node
         */
        private void forwardPut(String targetNode, String key, String value) {
            // In a real implementation, this would send a network request
            System.out.println("Forwarding put(" + key + ", " + value + ") to node " + targetNode);
        }
        
        /**
         * Forwards a remove operation to another node
         */
        private void forwardRemove(String targetNode, String key) {
            // In a real implementation, this would send a network request
            System.out.println("Forwarding remove(" + key + ") to node " + targetNode);
        }
        
        /**
         * Fetches a value from another node
         */
        private String fetchFromNode(String node, String key) {
            // In a real implementation, this would send a network request
            System.out.println("Fetching key " + key + " from node " + node);
            return null; // Simulated response
        }
        
        /**
         * Gets all keys in the local store
         */
        public Set<String> getLocalKeys() {
            return new HashSet<>(localStore.keySet());
        }
        
        /**
         * Gets the node ID
         */
        public String getNodeId() {
            return nodeId;
        }
        
        /**
         * Gets the cluster nodes
         */
        public List<String> getClusterNodes() {
            return new ArrayList<>(clusterNodes);
        }
    }
    
    /**
     * Represents a message queue for distributed systems
     */
    public static class MessageQueue {
        private final BlockingQueue<Message> queue;
        private final List<MessageConsumer> consumers;
        private final ExecutorService consumerPool;
        private volatile boolean running;
        
        public MessageQueue() {
            this.queue = new LinkedBlockingQueue<>();
            this.consumers = new CopyOnWriteArrayList<>();
            this.consumerPool = Executors.newCachedThreadPool();
            this.running = false;
        }
        
        /**
         * Starts the message queue
         */
        public void start() {
            if (running) {
                throw new IllegalStateException("Message queue is already running");
            }
            
            running = true;
            startConsumers();
            System.out.println("Message queue started");
        }
        
        /**
         * Stops the message queue
         */
        public void stop() {
            if (!running) {
                return;
            }
            
            running = false;
            consumerPool.shutdown();
            
            try {
                if (!consumerPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    consumerPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                consumerPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
            
            System.out.println("Message queue stopped");
        }
        
        /**
         * Sends a message to the queue
         */
        public void sendMessage(String topic, String content) {
            Message message = new Message(topic, content, System.currentTimeMillis());
            try {
                queue.put(message);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while sending message", e);
            }
        }
        
        /**
         * Adds a consumer to the queue
         */
        public void addConsumer(MessageConsumer consumer) {
            consumers.add(consumer);
            if (running) {
                consumerPool.submit(new ConsumerWorker(consumer));
            }
        }
        
        /**
         * Removes a consumer from the queue
         */
        public void removeConsumer(MessageConsumer consumer) {
            consumers.remove(consumer);
        }
        
        /**
         * Starts consumer workers
         */
        private void startConsumers() {
            for (MessageConsumer consumer : consumers) {
                consumerPool.submit(new ConsumerWorker(consumer));
            }
        }
        
        /**
         * Represents a message in the queue
         */
        public static class Message {
            private final String topic;
            private final String content;
            private final long timestamp;
            
            public Message(String topic, String content, long timestamp) {
                this.topic = topic;
                this.content = content;
                this.timestamp = timestamp;
            }
            
            public String getTopic() {
                return topic;
            }
            
            public String getContent() {
                return content;
            }
            
            public long getTimestamp() {
                return timestamp;
            }
        }
        
        /**
         * Represents a message consumer
         */
        public interface MessageConsumer {
            boolean canConsume(Message message);
            void consume(Message message);
        }
        
        /**
         * Worker that processes messages for a consumer
         */
        private class ConsumerWorker implements Runnable {
            private final MessageConsumer consumer;
            
            public ConsumerWorker(MessageConsumer consumer) {
                this.consumer = consumer;
            }
            
            @Override
            public void run() {
                while (running) {
                    try {
                        Message message = queue.take();
                        if (consumer.canConsume(message)) {
                            consumer.consume(message);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        System.err.println("Error processing message: " + e.getMessage());
                    }
                }
            }
        }
    }
    
    /**
     * Represents a network packet for custom protocols
     */
    public static class NetworkPacket {
        private final int packetId;
        private final byte[] data;
        private final long timestamp;
        private final String source;
        private final String destination;
        
        public NetworkPacket(int packetId, byte[] data, String source, String destination) {
            this.packetId = packetId;
            this.data = data != null ? data.clone() : new byte[0];
            this.timestamp = System.currentTimeMillis();
            this.source = source;
            this.destination = destination;
        }
        
        public int getPacketId() {
            return packetId;
        }
        
        public byte[] getData() {
            return data.clone();
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public String getSource() {
            return source;
        }
        
        public String getDestination() {
            return destination;
        }
        
        /**
         * Serializes the packet to bytes
         */
        public byte[] serialize() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            
            dos.writeInt(packetId);
            dos.writeUTF(source);
            dos.writeUTF(destination);
            dos.writeLong(timestamp);
            dos.writeInt(data.length);
            dos.write(data);
            
            return baos.toByteArray();
        }
        
        /**
         * Deserializes a packet from bytes
         */
        public static NetworkPacket deserialize(byte[] bytes) throws IOException {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            DataInputStream dis = new DataInputStream(bais);
            
            int packetId = dis.readInt();
            String source = dis.readUTF();
            String destination = dis.readUTF();
            long timestamp = dis.readLong();
            int dataLength = dis.readInt();
            byte[] data = new byte[dataLength];
            dis.readFully(data);
            
            NetworkPacket packet = new NetworkPacket(packetId, data, source, destination);
            // Note: We can't directly set timestamp, so we'll have to live with the current time
            return packet;
        }
        
        @Override
        public String toString() {
            return "NetworkPacket{packetId=" + packetId + ", source='" + source + 
                   "', destination='" + destination + "', dataLength=" + data.length + "}";
        }
    }
    
    /**
     * Represents a network security utility
     */
    public static class NetworkSecurity {
        /**
         * Encrypts data using AES
         */
        public static byte[] encryptAES(byte[] data, byte[] key) throws Exception {
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(data);
        }
        
        /**
         * Decrypts data using AES
         */
        public static byte[] decryptAES(byte[] encryptedData, byte[] key) throws Exception {
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(encryptedData);
        }
        
        /**
         * Generates a hash of data using SHA-256
         */
        public static byte[] hashSHA256(byte[] data) throws NoSuchAlgorithmException {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(data);
        }
        
        /**
         * Generates a digital signature
         */
        public static byte[] signData(byte[] data, PrivateKey privateKey) throws Exception {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(data);
            return signature.sign();
        }
        
        /**
         * Verifies a digital signature
         */
        public static boolean verifySignature(byte[] data, byte[] signature, PublicKey publicKey) throws Exception {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(data);
            return sig.verify(signature);
        }
        
        /**
         * Compresses data using gzip
         */
        public static byte[] compressGzip(byte[] data) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
                gzos.write(data);
            }
            return baos.toByteArray();
        }
        
        /**
         * Decompresses data using gzip
         */
        public static byte[] decompressGzip(byte[] compressedData) throws IOException {
            ByteArrayInputStream bais = new ByteArrayInputStream(compressedData);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (GZIPInputStream gzis = new GZIPInputStream(bais)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = gzis.read(buffer)) > 0) {
                    baos.write(buffer, 0, len);
                }
            }
            return baos.toByteArray();
        }
    }
    
    /**
     * Represents a network load balancer
     */
    public static class LoadBalancer {
        private final List<String> servers;
        private final LoadBalancingAlgorithm algorithm;
        private final Map<String, Integer> serverWeights;
        private final AtomicInteger currentIndex;
        
        public enum LoadBalancingAlgorithm {
            ROUND_ROBIN, WEIGHTED_ROUND_ROBIN, LEAST_CONNECTIONS, IP_HASH
        }
        
        public LoadBalancer() {
            this.servers = new ArrayList<>();
            this.algorithm = LoadBalancingAlgorithm.ROUND_ROBIN;
            this.serverWeights = new HashMap<>();
            this.currentIndex = new AtomicInteger(0);
        }
        
        public LoadBalancer(LoadBalancingAlgorithm algorithm) {
            this.servers = new ArrayList<>();
            this.algorithm = algorithm;
            this.serverWeights = new HashMap<>();
            this.currentIndex = new AtomicInteger(0);
        }
        
        /**
         * Adds a server to the load balancer
         */
        public void addServer(String serverAddress) {
            addServer(serverAddress, 1);
        }
        
        /**
         * Adds a server with weight to the load balancer
         */
        public void addServer(String serverAddress, int weight) {
            servers.add(serverAddress);
            serverWeights.put(serverAddress, weight);
            System.out.println("Added server " + serverAddress + " with weight " + weight);
        }
        
        /**
         * Removes a server from the load balancer
         */
        public void removeServer(String serverAddress) {
            servers.remove(serverAddress);
            serverWeights.remove(serverAddress);
            System.out.println("Removed server " + serverAddress);
        }
        
        /**
         * Selects a server for a request
         */
        public String selectServer(String clientIp) {
            if (servers.isEmpty()) {
                throw new IllegalStateException("No servers available");
            }
            
            switch (algorithm) {
                case ROUND_ROBIN:
                    return selectRoundRobin();
                case WEIGHTED_ROUND_ROBIN:
                    return selectWeightedRoundRobin();
                case LEAST_CONNECTIONS:
                    return selectLeastConnections();
                case IP_HASH:
                    return selectIpHash(clientIp);
                default:
                    return selectRoundRobin();
            }
        }
        
        /**
         * Selects a server using round-robin algorithm
         */
        private String selectRoundRobin() {
            int index = currentIndex.getAndIncrement() % servers.size();
            return servers.get(index);
        }
        
        /**
         * Selects a server using weighted round-robin algorithm
         */
        private String selectWeightedRoundRobin() {
            // Simplified implementation - in practice, you would use a more sophisticated algorithm
            return selectRoundRobin();
        }
        
        /**
         * Selects a server with the least connections
         */
        private String selectLeastConnections() {
            // Simplified implementation - in practice, you would track connection counts
            return selectRoundRobin();
        }
        
        /**
         * Selects a server using IP hash
         */
        private String selectIpHash(String clientIp) {
            int hash = clientIp.hashCode();
            int index = Math.abs(hash) % servers.size();
            return servers.get(index);
        }
        
        /**
         * Gets the list of servers
         */
        public List<String> getServers() {
            return new ArrayList<>(servers);
        }
        
        /**
         * Gets the load balancing algorithm
         */
        public LoadBalancingAlgorithm getAlgorithm() {
            return algorithm;
        }
    }
    
    /**
     * Represents a network monitoring utility
     */
    public static class NetworkMonitor {
        private final Map<String, ServerStats> serverStats;
        private final ScheduledExecutorService scheduler;
        
        private static class ServerStats {
            volatile long requestCount;
            volatile long errorCount;
            volatile long totalResponseTime;
            volatile long lastCheckTime;
            
            ServerStats() {
                this.requestCount = 0;
                this.errorCount = 0;
                this.totalResponseTime = 0;
                this.lastCheckTime = System.currentTimeMillis();
            }
        }
        
        public NetworkMonitor() {
            this.serverStats = new ConcurrentHashMap<>();
            this.scheduler = Executors.newScheduledThreadPool(1);
        }
        
        /**
         * Starts monitoring
         */
        public void startMonitoring() {
            scheduler.scheduleAtFixedRate(this::printStats, 10, 10, TimeUnit.SECONDS);
        }
        
        /**
         * Stops monitoring
         */
        public void stopMonitoring() {
            scheduler.shutdown();
        }
        
        /**
         * Records a successful request
         */
        public void recordSuccess(String server, long responseTime) {
            ServerStats stats = serverStats.computeIfAbsent(server, k -> new ServerStats());
            stats.requestCount++;
            stats.totalResponseTime += responseTime;
        }
        
        /**
         * Records a failed request
         */
        public void recordError(String server) {
            ServerStats stats = serverStats.computeIfAbsent(server, k -> new ServerStats());
            stats.requestCount++;
            stats.errorCount++;
        }
        
        /**
         * Gets statistics for a server
         */
        public ServerStatistics getStatistics(String server) {
            ServerStats stats = serverStats.get(server);
            if (stats == null) {
                return new ServerStatistics(0, 0, 0, 0);
            }
            
            long requestCount = stats.requestCount;
            long errorCount = stats.errorCount;
            double errorRate = requestCount > 0 ? (double) errorCount / requestCount : 0;
            double avgResponseTime = requestCount > 0 ? (double) stats.totalResponseTime / requestCount : 0;
            
            return new ServerStatistics(requestCount, errorCount, errorRate, avgResponseTime);
        }
        
        /**
         * Prints statistics for all servers
         */
        private void printStats() {
            System.out.println("=== Network Monitor Statistics ===");
            for (Map.Entry<String, ServerStats> entry : serverStats.entrySet()) {
                String server = entry.getKey();
                ServerStatistics stats = getStatistics(server);
                System.out.printf("Server %s: Requests=%d, Errors=%d, ErrorRate=%.2f%%, AvgResponseTime=%.2fms%n",
                    server, stats.getRequestCount(), stats.getErrorCount(), 
                    stats.getErrorRate() * 100, stats.getAverageResponseTime());
            }
            System.out.println("==================================");
        }
        
        /**
         * Represents server statistics
         */
        public static class ServerStatistics {
            private final long requestCount;
            private final long errorCount;
            private final double errorRate;
            private final double averageResponseTime;
            
            public ServerStatistics(long requestCount, long errorCount, double errorRate, double averageResponseTime) {
                this.requestCount = requestCount;
                this.errorCount = errorCount;
                this.errorRate = errorRate;
                this.averageResponseTime = averageResponseTime;
            }
            
            public long getRequestCount() {
                return requestCount;
            }
            
            public long getErrorCount() {
                return errorCount;
            }
            
            public double getErrorRate() {
                return errorRate;
            }
            
            public double getAverageResponseTime() {
                return averageResponseTime;
            }
        }
    }
}