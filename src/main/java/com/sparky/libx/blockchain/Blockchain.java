package com.sparky.libx.blockchain;

import com.sparky.libx.crypto.Cryptography;
import com.sparky.libx.math.Vector3D;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Blockchain Implementation for Minecraft Plugins
 * Provides capabilities for creating and managing blockchain networks, smart contracts, and cryptographic transactions
 * 
 * @author Андрій Будильников
 */
public class Blockchain {
    
    /**
     * Represents a transaction in the blockchain
     */
    public static class Transaction {
        private final String id;
        private final String sender;
        private final String recipient;
        private final double amount;
        private final long timestamp;
        private final String signature;
        private final Map<String, Object> metadata;
        
        public Transaction(String sender, String recipient, double amount, String signature) {
            this.id = UUID.randomUUID().toString();
            this.sender = sender;
            this.recipient = recipient;
            this.amount = amount;
            this.timestamp = System.currentTimeMillis();
            this.signature = signature;
            this.metadata = new ConcurrentHashMap<>();
        }
        
        public Transaction(String id, String sender, String recipient, double amount, 
                          long timestamp, String signature, Map<String, Object> metadata) {
            this.id = id;
            this.sender = sender;
            this.recipient = recipient;
            this.amount = amount;
            this.timestamp = timestamp;
            this.signature = signature;
            this.metadata = new ConcurrentHashMap<>(metadata);
        }
        
        public String calculateHash() {
            String data = sender + recipient + amount + timestamp + signature;
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                data += entry.getKey() + entry.getValue();
            }
            return Cryptography.sha256(data);
        }
        
        public boolean isValid() {
            // Check if transaction has valid signature
            if (signature == null || signature.isEmpty()) {
                return false;
            }
            
            // Check if amount is positive
            if (amount <= 0) {
                return false;
            }
            
            // Check if sender and recipient are different
            if (sender.equals(recipient)) {
                return false;
            }
            
            return true;
        }
        
        public String getId() {
            return id;
        }
        
        public String getSender() {
            return sender;
        }
        
        public String getRecipient() {
            return recipient;
        }
        
        public double getAmount() {
            return amount;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public String getSignature() {
            return signature;
        }
        
        public Map<String, Object> getMetadata() {
            return new HashMap<>(metadata);
        }
        
        public void addMetadata(String key, Object value) {
            metadata.put(key, value);
        }
        
        public <T> T getMetadata(String key, Class<T> type) {
            Object value = metadata.get(key);
            if (type.isInstance(value)) {
                return type.cast(value);
            }
            return null;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Transaction that = (Transaction) o;
            return id.equals(that.id);
        }
        
        @Override
        public int hashCode() {
            return id.hashCode();
        }
        
        @Override
        public String toString() {
            return "Transaction{" +
                    "id='" + id + '\'' +
                    ", sender='" + sender + '\'' +
                    ", recipient='" + recipient + '\'' +
                    ", amount=" + amount +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }
    
    /**
     * Represents a block in the blockchain
     */
    public static class Block {
        private final int index;
        private final long timestamp;
        private final List<Transaction> transactions;
        private final String previousHash;
        private final String hash;
        private final String merkleRoot;
        private final long nonce;
        
        public Block(int index, List<Transaction> transactions, String previousHash) {
            this.index = index;
            this.timestamp = System.currentTimeMillis();
            this.transactions = new ArrayList<>(transactions);
            this.previousHash = previousHash;
            this.merkleRoot = calculateMerkleRoot();
            this.nonce = 0;
            this.hash = calculateHash();
        }
        
        public Block(int index, long timestamp, List<Transaction> transactions, 
                    String previousHash, String merkleRoot, long nonce, String hash) {
            this.index = index;
            this.timestamp = timestamp;
            this.transactions = new ArrayList<>(transactions);
            this.previousHash = previousHash;
            this.merkleRoot = merkleRoot;
            this.nonce = nonce;
            this.hash = hash;
        }
        
        public String calculateHash() {
            String data = index + timestamp + merkleRoot + previousHash + nonce;
            return Cryptography.sha256(data);
        }
        
        private String calculateMerkleRoot() {
            if (transactions.isEmpty()) {
                return Cryptography.sha256("");
            }
            
            List<String> hashes = new ArrayList<>();
            for (Transaction transaction : transactions) {
                hashes.add(transaction.calculateHash());
            }
            
            while (hashes.size() > 1) {
                List<String> newHashes = new ArrayList<>();
                for (int i = 0; i < hashes.size(); i += 2) {
                    String left = hashes.get(i);
                    String right = (i + 1 < hashes.size()) ? hashes.get(i + 1) : left;
                    String combined = left + right;
                    newHashes.add(Cryptography.sha256(combined));
                }
                hashes = newHashes;
            }
            
            return hashes.get(0);
        }
        
        public boolean isValid(Block previousBlock) {
            // Check index
            if (index != previousBlock.index + 1) {
                return false;
            }
            
            // Check previous hash
            if (!previousHash.equals(previousBlock.hash)) {
                return false;
            }
            
            // Check hash
            if (!hash.equals(calculateHash())) {
                return false;
            }
            
            // Check timestamp
            if (timestamp <= previousBlock.timestamp) {
                return false;
            }
            
            // Check transactions
            for (Transaction transaction : transactions) {
                if (!transaction.isValid()) {
                    return false;
                }
            }
            
            return true;
        }
        
        public int getIndex() {
            return index;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public List<Transaction> getTransactions() {
            return new ArrayList<>(transactions);
        }
        
        public String getPreviousHash() {
            return previousHash;
        }
        
        public String getHash() {
            return hash;
        }
        
        public String getMerkleRoot() {
            return merkleRoot;
        }
        
        public long getNonce() {
            return nonce;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Block block = (Block) o;
            return index == block.index && hash.equals(block.hash);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(index, hash);
        }
        
        @Override
        public String toString() {
            return "Block{" +
                    "index=" + index +
                    ", timestamp=" + timestamp +
                    ", transactions=" + transactions.size() +
                    ", previousHash='" + previousHash + '\'' +
                    ", hash='" + hash + '\'' +
                    '}';
        }
    }
    
    /**
     * Represents a blockchain network
     */
    public static class Chain {
        private final List<Block> chain;
        private final Map<String, Double> balances;
        private final int difficulty;
        private final double miningReward;
        
        public Chain(int difficulty, double miningReward) {
            this.chain = new CopyOnWriteArrayList<>();
            this.balances = new ConcurrentHashMap<>();
            this.difficulty = difficulty;
            this.miningReward = miningReward;
            
            // Create genesis block
            createGenesisBlock();
        }
        
        private void createGenesisBlock() {
            List<Transaction> genesisTransactions = new ArrayList<>();
            Block genesisBlock = new Block(0, genesisTransactions, "0");
            chain.add(genesisBlock);
        }
        
        public Block getLatestBlock() {
            return chain.get(chain.size() - 1);
        }
        
        public Block mineBlock(List<Transaction> transactions, String minerAddress) {
            // Add mining reward transaction
            Transaction rewardTx = new Transaction("NETWORK", minerAddress, miningReward, "MINING_REWARD");
            transactions.add(rewardTx);
            
            // Create new block
            Block newBlock = new Block(chain.size(), transactions, getLatestBlock().getHash());
            
            // Mine the block (proof of work)
            Block minedBlock = mineBlockWithProofOfWork(newBlock);
            
            // Add to chain if valid
            if (isBlockValid(minedBlock, getLatestBlock())) {
                chain.add(minedBlock);
                updateBalances(minedBlock);
                return minedBlock;
            }
            
            return null;
        }
        
        private Block mineBlockWithProofOfWork(Block block) {
            String target = new String(new char[difficulty]).replace('\0', '0');
            long nonce = 0;
            
            while (!block.calculateHash().substring(0, difficulty).equals(target)) {
                nonce++;
                // Create new block with updated nonce
                Block newBlock = new Block(
                    block.getIndex(),
                    block.getTimestamp(),
                    block.getTransactions(),
                    block.getPreviousHash(),
                    block.getMerkleRoot(),
                    nonce,
                    "" // Hash will be recalculated
                );
                
                // Recalculate hash with new nonce
                String hash = newBlock.calculateHash();
                // Create final block with correct hash
                block = new Block(
                    block.getIndex(),
                    block.getTimestamp(),
                    block.getTransactions(),
                    block.getPreviousHash(),
                    block.getMerkleRoot(),
                    nonce,
                    hash
                );
            }
            
            return block;
        }
        
        public boolean addTransaction(Transaction transaction) {
            // Verify transaction
            if (!transaction.isValid()) {
                return false;
            }
            
            // Check if sender has sufficient balance
            if (getBalance(transaction.getSender()) < transaction.getAmount()) {
                return false;
            }
            
            // Add to pending transactions (in a real implementation, this would be stored separately)
            return true;
        }
        
        private boolean isBlockValid(Block newBlock, Block previousBlock) {
            return newBlock.isValid(previousBlock);
        }
        
        private void updateBalances(Block block) {
            for (Transaction transaction : block.getTransactions()) {
                // Deduct from sender
                balances.put(transaction.getSender(), 
                    balances.getOrDefault(transaction.getSender(), 0.0) - transaction.getAmount());
                
                // Add to recipient
                balances.put(transaction.getRecipient(), 
                    balances.getOrDefault(transaction.getRecipient(), 0.0) + transaction.getAmount());
            }
        }
        
        public double getBalance(String address) {
            return balances.getOrDefault(address, 0.0);
        }
        
        public boolean isChainValid() {
            // Check genesis block
            Block genesisBlock = chain.get(0);
            if (genesisBlock.getIndex() != 0 || !genesisBlock.getPreviousHash().equals("0")) {
                return false;
            }
            
            // Check all other blocks
            for (int i = 1; i < chain.size(); i++) {
                Block currentBlock = chain.get(i);
                Block previousBlock = chain.get(i - 1);
                
                if (!currentBlock.isValid(previousBlock)) {
                    return false;
                }
            }
            
            return true;
        }
        
        public List<Block> getChain() {
            return new ArrayList<>(chain);
        }
        
        public int getDifficulty() {
            return difficulty;
        }
        
        public double getMiningReward() {
            return miningReward;
        }
        
        public int getChainLength() {
            return chain.size();
        }
        
        public List<Transaction> getPendingTransactions() {
            // In a real implementation, this would return actual pending transactions
            return new ArrayList<>();
        }
    }
    
    /**
     * Represents a smart contract on the blockchain
     */
    public static abstract class SmartContract {
        protected final String address;
        protected final Map<String, Object> storage;
        protected double balance;
        
        public SmartContract() {
            this.address = "SC_" + UUID.randomUUID().toString().replace("-", "");
            this.storage = new ConcurrentHashMap<>();
            this.balance = 0;
        }
        
        public abstract void execute(String function, Object... args);
        
        public void deposit(double amount) {
            if (amount > 0) {
                balance += amount;
            }
        }
        
        public boolean withdraw(double amount) {
            if (amount > 0 && balance >= amount) {
                balance -= amount;
                return true;
            }
            return false;
        }
        
        protected void setStorage(String key, Object value) {
            storage.put(key, value);
        }
        
        protected <T> T getStorage(String key, Class<T> type) {
            Object value = storage.get(key);
            if (type.isInstance(value)) {
                return type.cast(value);
            }
            return null;
        }
        
        public String getAddress() {
            return address;
        }
        
        public double getBalance() {
            return balance;
        }
    }
    
    /**
     * Represents a cryptocurrency wallet
     */
    public static class Wallet {
        private final String address;
        private final String publicKey;
        private final String privateKey;
        private double balance;
        
        public Wallet() {
            // Generate key pair
            String keyPair = Cryptography.generateKeyPair();
            // For simplicity, we'll use the same string for both keys
            this.privateKey = keyPair;
            this.publicKey = keyPair;
            this.address = Cryptography.sha256(publicKey).substring(0, 32);
            this.balance = 0;
        }
        
        public Transaction createTransaction(String recipient, double amount, Chain blockchain) {
            // Check balance
            if (balance < amount) {
                throw new IllegalArgumentException("Insufficient balance");
            }
            
            // Create transaction data
            String transactionData = address + recipient + amount + System.currentTimeMillis();
            
            // Sign transaction
            String signature = Cryptography.sign(transactionData, privateKey);
            
            // Create transaction
            Transaction transaction = new Transaction(address, recipient, amount, signature);
            
            return transaction;
        }
        
        public void updateBalance(Chain blockchain) {
            this.balance = blockchain.getBalance(address);
        }
        
        public String getAddress() {
            return address;
        }
        
        public String getPublicKey() {
            return publicKey;
        }
        
        public String getPrivateKey() {
            return privateKey;
        }
        
        public double getBalance() {
            return balance;
        }
    }
    
    /**
     * Represents a node in the blockchain network
     */
    public static class Node {
        private final String id;
        private final String address;
        private final Chain blockchain;
        private final List<Transaction> pendingTransactions;
        private final Set<Node> connectedNodes;
        private boolean mining;
        
        public Node(String id, String address, int difficulty, double miningReward) {
            this.id = id;
            this.address = address;
            this.blockchain = new Chain(difficulty, miningReward);
            this.pendingTransactions = new CopyOnWriteArrayList<>();
            this.connectedNodes = new HashSet<>();
            this.mining = false;
        }
        
        public void connectToNode(Node node) {
            connectedNodes.add(node);
            node.connectedNodes.add(this);
        }
        
        public void disconnectFromNode(Node node) {
            connectedNodes.remove(node);
            node.connectedNodes.remove(this);
        }
        
        public void addTransaction(Transaction transaction) {
            if (blockchain.addTransaction(transaction)) {
                pendingTransactions.add(transaction);
                // Broadcast to other nodes
                broadcastTransaction(transaction);
            }
        }
        
        public Block minePendingTransactions(String minerAddress) {
            Block minedBlock = blockchain.mineBlock(new ArrayList<>(pendingTransactions), minerAddress);
            if (minedBlock != null) {
                pendingTransactions.clear();
                // Broadcast the new block
                broadcastBlock(minedBlock);
            }
            return minedBlock;
        }
        
        private void broadcastTransaction(Transaction transaction) {
            for (Node node : connectedNodes) {
                // In a real implementation, this would send the transaction over the network
                // node.receiveTransaction(transaction);
            }
        }
        
        private void broadcastBlock(Block block) {
            for (Node node : connectedNodes) {
                // In a real implementation, this would send the block over the network
                // node.receiveBlock(block);
            }
        }
        
        public void receiveTransaction(Transaction transaction) {
            if (blockchain.addTransaction(transaction)) {
                pendingTransactions.add(transaction);
            }
        }
        
        public void receiveBlock(Block block) {
            // In a real implementation, this would validate and add the block to the chain
        }
        
        public Chain getBlockchain() {
            return blockchain;
        }
        
        public List<Transaction> getPendingTransactions() {
            return new ArrayList<>(pendingTransactions);
        }
        
        public Set<Node> getConnectedNodes() {
            return new HashSet<>(connectedNodes);
        }
        
        public String getId() {
            return id;
        }
        
        public String getAddress() {
            return address;
        }
        
        public boolean isMining() {
            return mining;
        }
        
        public void setMining(boolean mining) {
            this.mining = mining;
        }
    }
    
    /**
     * Represents a decentralized application (DApp) running on the blockchain
     */
    public static class DApp {
        private final String name;
        private final String version;
        private final SmartContract smartContract;
        private final List<Node> nodes;
        private boolean running;
        
        public DApp(String name, String version, SmartContract smartContract) {
            this.name = name;
            this.version = version;
            this.smartContract = smartContract;
            this.nodes = new CopyOnWriteArrayList<>();
            this.running = false;
        }
        
        public void start() {
            if (!running) {
                running = true;
                // Initialize the DApp on all nodes
                for (Node node : nodes) {
                    // Deploy smart contract to node
                }
            }
        }
        
        public void stop() {
            if (running) {
                running = false;
                // Clean up resources
            }
        }
        
        public void addNode(Node node) {
            nodes.add(node);
            if (running) {
                // Deploy smart contract to new node
            }
        }
        
        public void removeNode(Node node) {
            nodes.remove(node);
        }
        
        public String getName() {
            return name;
        }
        
        public String getVersion() {
            return version;
        }
        
        public SmartContract getSmartContract() {
            return smartContract;
        }
        
        public List<Node> getNodes() {
            return new ArrayList<>(nodes);
        }
        
        public boolean isRunning() {
            return running;
        }
    }
}