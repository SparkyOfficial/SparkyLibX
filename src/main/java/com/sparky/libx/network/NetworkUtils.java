package com.sparky.libx.network;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.*;

/**
 * утиліти для роботи з мережею
 * включає клієнт-серверну комунікацію, передачу даних, сокети і мережеві протоколи
 * @author Андрій Будильников
 */
public class NetworkUtils {
    
    /**
     * простий TCP сервер
     */
    public static class TCPServer {
        private final int port;
        private ServerSocket serverSocket;
        private ExecutorService executorService;
        private volatile boolean running;
        
        public TCPServer(int port) {
            this.port = port;
            this.executorService = Executors.newCachedThreadPool();
        }
        
        /**
         * запустити сервер
         * @param handler обробник клієнтських з'єднань
         */
        public void start(ClientHandler handler) throws IOException {
            serverSocket = new ServerSocket(port);
            running = true;
            
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    executorService.submit(() -> handleClient(clientSocket, handler));
                } catch (IOException e) {
                    if (running) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
        private void handleClient(Socket clientSocket, ClientHandler handler) {
            try (Socket socket = clientSocket;
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                
                handler.handle(socket, in, out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        /**
         * зупинити сервер
         */
        public void stop() throws IOException {
            running = false;
            if (serverSocket != null) {
                serverSocket.close();
            }
            if (executorService != null) {
                executorService.shutdown();
            }
        }
        
        @FunctionalInterface
        public interface ClientHandler {
            void handle(Socket socket, BufferedReader in, PrintWriter out) throws IOException;
        }
    }
    
    /**
     * простий TCP клієнт
     */
    public static class TCPClient {
        private final String host;
        private final int port;
        
        public TCPClient(String host, int port) {
            this.host = host;
            this.port = port;
        }
        
        /**
         * надіслати повідомлення і отримати відповідь
         * @param message повідомлення
         * @return відповідь
         */
        public String sendMessage(String message) throws IOException {
            try (Socket socket = new Socket(host, port);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                
                out.println(message);
                return in.readLine();
            }
        }
    }
    
    /**
     * UDP сервер
     */
    public static class UDPServer {
        private final int port;
        private DatagramSocket socket;
        private volatile boolean running;
        
        public UDPServer(int port) {
            this.port = port;
        }
        
        /**
         * запустити сервер
         * @param handler обробник повідомлень
         */
        public void start(MessageHandler handler) throws IOException {
            socket = new DatagramSocket(port);
            running = true;
            
            byte[] buffer = new byte[1024];
            
            while (running) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    handler.handle(packet);
                } catch (IOException e) {
                    if (running) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
        /**
         * зупинити сервер
         */
        public void stop() {
            running = false;
            if (socket != null) {
                socket.close();
            }
        }
        
        @FunctionalInterface
        public interface MessageHandler {
            void handle(DatagramPacket packet);
        }
    }
    
    /**
     * UDP клієнт
     */
    public static class UDPClient {
        private final String host;
        private final int port;
        
        public UDPClient(String host, int port) {
            this.host = host;
            this.port = port;
        }
        
        /**
         * надіслати повідомлення
         * @param message повідомлення
         */
        public void sendMessage(String message) throws IOException {
            try (DatagramSocket socket = new DatagramSocket()) {
                byte[] data = message.getBytes(StandardCharsets.UTF_8);
                InetAddress address = InetAddress.getByName(host);
                DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                socket.send(packet);
            }
        }
        
        /**
         * надіслати повідомлення і отримати відповідь
         * @param message повідомлення
         * @return відповідь
         */
        public String sendMessageWithResponse(String message) throws IOException {
            try (DatagramSocket socket = new DatagramSocket()) {
                // надіслати повідомлення
                byte[] data = message.getBytes(StandardCharsets.UTF_8);
                InetAddress address = InetAddress.getByName(host);
                DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                socket.send(packet);
                
                // отримати відповідь
                byte[] buffer = new byte[1024];
                DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(responsePacket);
                
                return new String(responsePacket.getData(), 0, responsePacket.getLength(), StandardCharsets.UTF_8);
            }
        }
    }
    
    /**
     * HTTP клієнт
     */
    public static class HTTPClient {
        
        /**
         * виконати GET запит
         * @param url URL
         * @return відповідь
         */
        public static String get(String url) throws IOException {
            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            connection.setRequestMethod("GET");
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                        response.append("\n");
                    }
                    
                    return response.toString();
                }
            } else {
                throw new IOException("HTTP error code: " + responseCode);
            }
        }
        
        /**
         * виконати POST запит
         * @param url URL
         * @param data дані
         * @return відповідь
         */
        public static String post(String url, String data) throws IOException {
            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = data.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                        response.append("\n");
                    }
                    
                    return response.toString();
                }
            } else {
                throw new IOException("HTTP error code: " + responseCode);
            }
        }
    }
    
    /**
     * сокетний сервер з NIO
     */
    public static class NIOServer {
        private final int port;
        private Selector selector;
        private ServerSocketChannel serverChannel;
        private volatile boolean running;
        
        public NIOServer(int port) throws IOException {
            this.port = port;
            this.selector = Selector.open();
            this.serverChannel = ServerSocketChannel.open();
            this.serverChannel.configureBlocking(false);
            this.serverChannel.bind(new InetSocketAddress(port));
            this.serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        }
        
        /**
         * запустити сервер
         */
        public void start() throws IOException {
            running = true;
            
            while (running) {
                selector.select();
                
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();
                
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    }
                    
                    if (key.isReadable()) {
                        handleRead(key);
                    }
                    
                    iter.remove();
                }
            }
        }
        
        private void handleAccept(SelectionKey key) throws IOException {
            ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
            SocketChannel clientChannel = serverChannel.accept();
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ);
        }
        
        private void handleRead(SelectionKey key) throws IOException {
            SocketChannel clientChannel = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            
            int bytesRead = clientChannel.read(buffer);
            if (bytesRead > 0) {
                buffer.flip();
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);
                String message = new String(data, StandardCharsets.UTF_8);
                
                // ехо повідомлення назад
                buffer.clear();
                buffer.put(("Echo: " + message).getBytes(StandardCharsets.UTF_8));
                buffer.flip();
                clientChannel.write(buffer);
            } else if (bytesRead < 0) {
                clientChannel.close();
            }
        }
        
        /**
         * зупинити сервер
         */
        public void stop() throws IOException {
            running = false;
            if (selector != null) {
                selector.close();
            }
            if (serverChannel != null) {
                serverChannel.close();
            }
        }
    }
    
    /**
     * сокетний клієнт з NIO
     */
    public static class NIOClient {
        private final String host;
        private final int port;
        
        public NIOClient(String host, int port) {
            this.host = host;
            this.port = port;
        }
        
        /**
         * надіслати повідомлення
         * @param message повідомлення
         * @return відповідь
         */
        public String sendMessage(String message) throws IOException {
            try (SocketChannel socketChannel = SocketChannel.open()) {
                socketChannel.connect(new InetSocketAddress(host, port));
                socketChannel.configureBlocking(false);
                
                // надіслати повідомлення
                ByteBuffer buffer = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));
                socketChannel.write(buffer);
                
                // отримати відповідь
                buffer.clear();
                buffer = ByteBuffer.allocate(1024);
                
                while (socketChannel.read(buffer) == 0) {
                    // очікувати на дані
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                
                buffer.flip();
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);
                return new String(data, StandardCharsets.UTF_8);
            }
        }
    }
    
    /**
     * інструменти для роботи з IP адресами
     */
    public static class IPUtils {
        
        /**
         * перевірити чи IP адреса є валідною
         * @param ip IP адреса
         * @return true якщо адреса валідна
         */
        public static boolean isValidIP(String ip) {
            try {
                InetAddress.getByName(ip);
                return true;
            } catch (UnknownHostException e) {
                return false;
            }
        }
        
        /**
         * отримати локальну IP адресу
         * @return локальна IP адреса
         */
        public static String getLocalIP() {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                return "127.0.0.1";
            }
        }
        
        /**
         * перевірити чи IP адреса належить до локальної мережі
         * @param ip IP адреса
         * @return true якщо адреса локальна
         */
        public static boolean isLocalIP(String ip) {
            if (ip.startsWith("127.") || ip.startsWith("10.") || 
                ip.startsWith("192.168.") || ip.startsWith("172.")) {
                return true;
            }
            
            try {
                InetAddress address = InetAddress.getByName(ip);
                return address.isLoopbackAddress() || address.isSiteLocalAddress();
            } catch (UnknownHostException e) {
                return false;
            }
        }
    }
    
    /**
     * інструменти для роботи з портами
     */
    public static class PortUtils {
        
        /**
         * перевірити чи порт відкритий
         * @param host хост
         * @param port порт
         * @return true якщо порт відкритий
         */
        public static boolean isPortOpen(String host, int port) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), 1000);
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        
        /**
         * знайти доступний порт
         * @param startPort початковий порт
         * @return доступний порт або -1 якщо не знайдено
         */
        public static int findAvailablePort(int startPort) {
            for (int port = startPort; port < 65536; port++) {
                if (!isPortOpen("localhost", port)) {
                    return port;
                }
            }
            return -1;
        }
    }
    
    /**
     * інструменти для роботи з URL
     */
    public static class URLUtils {
        
        /**
         * отримати домен з URL
         * @param url URL
         * @return домен
         */
        public static String getDomain(String url) {
            try {
                URI uri = new URI(url);
                return uri.getHost();
            } catch (URISyntaxException e) {
                return null;
            }
        }
        
        /**
         * отримати протокол з URL
         * @param url URL
         * @return протокол
         */
        public static String getProtocol(String url) {
            try {
                URI uri = new URI(url);
                return uri.getScheme();
            } catch (URISyntaxException e) {
                return null;
            }
        }
        
        /**
         * отримати параметри запиту з URL
         * @param url URL
         * @return мапа параметрів
         */
        public static Map<String, String> getQueryParameters(String url) {
            Map<String, String> params = new HashMap<>();
            try {
                URI uri = new URI(url);
                String query = uri.getQuery();
                if (query != null) {
                    String[] pairs = query.split("&");
                    for (String pair : pairs) {
                        String[] keyValue = pair.split("=");
                        if (keyValue.length == 2) {
                            params.put(keyValue[0], keyValue[1]);
                        }
                    }
                }
            } catch (URISyntaxException e) {
                // ігнорувати
            }
            return params;
        }
    }
    
    /**
     * інструменти для роботи з даними
     */
    public static class DataUtils {
        
        /**
         * стиснути дані методом GZIP
         * @param data дані
         * @return стиснуті дані
         */
        public static byte[] compress(byte[] data) throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
            try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
                gzip.write(data);
            }
            return bos.toByteArray();
        }
        
        /**
         * розпакувати дані методом GZIP
         * @param compressedData стиснуті дані
         * @return розпаковані дані
         */
        public static byte[] decompress(byte[] compressedData) throws IOException {
            ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (GZIPInputStream gzip = new GZIPInputStream(bis)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = gzip.read(buffer)) > 0) {
                    bos.write(buffer, 0, len);
                }
            }
            return bos.toByteArray();
        }
        
        /**
         * закодувати дані в Base64
         * @param data дані
         * @return закодовані дані
         */
        public static String encodeBase64(byte[] data) {
            return Base64.getEncoder().encodeToString(data);
        }
        
        /**
         * декодувати дані з Base64
         * @param encodedData закодовані дані
         * @return декодовані дані
         */
        public static byte[] decodeBase64(String encodedData) {
            return Base64.getDecoder().decode(encodedData);
        }
    }
    
    /**
     * інструменти для роботи з мережевими інтерфейсами
     */
    public static class NetworkInterfaceUtils {
        
        /**
         * отримати список мережевих інтерфейсів
         * @return список інтерфейсів
         */
        public static List<String> getNetworkInterfaces() {
            List<String> interfaces = new ArrayList<>();
            try {
                Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
                while (networkInterfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = networkInterfaces.nextElement();
                    interfaces.add(networkInterface.getName() + " (" + networkInterface.getDisplayName() + ")");
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
            return interfaces;
        }
        
        /**
         * отримати IP адреси мережевого інтерфейсу
         * @param interfaceName назва інтерфейсу
         * @return список IP адрес
         */
        public static List<String> getIPAddresses(String interfaceName) {
            List<String> addresses = new ArrayList<>();
            try {
                NetworkInterface networkInterface = NetworkInterface.getByName(interfaceName);
                if (networkInterface != null) {
                    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        addresses.add(inetAddress.getHostAddress());
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
            return addresses;
        }
    }
}