package com.sparky.libx.blockchain;

import com.sparky.libx.crypto.Cryptography;
import java.util.*;
import java.util.concurrent.*;

/**
 * Implementation of a blockchain system
 * @author Андрій Будильников
 */
public class Blockchain {
    
    /**
     * Represents a transaction in the blockchain
     */
    public static class Transaction {
        private final String sender;
        private final String recipient;
        private final double amount;
        private final long timestamp;
        private final String signature;
        
        public Transaction(String sender, String recipient, double amount, String signature) {
            this.sender = sender;
            this.recipient = recipient;
            this.amount = amount;
            this.timestamp = System.currentTimeMillis();
            this.signature = signature;
        }
        
        public boolean isValid(Chain blockchain) {
            // check if transaction is valid
            if (sender == null || recipient == null || amount <= 0) {
                return false;
            }
            
            // verify signature
            String transactionData = sender + recipient + amount + timestamp;
            if (!Cryptography.verifySignature(transactionData, signature, sender)) {
                return false;
            }
            
            // check sender balance
            if (blockchain.getBalance(sender) < amount) {
                return false;
            }
            
            return true;
        }
        
        // getters
        public String getSender() { return sender; }
        public String getRecipient() { return recipient; }
        public double getAmount() { return amount; }
        public long getTimestamp() { return timestamp; }
        public String getSignature() { return signature; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Transaction that = (Transaction) o;
            return Double.compare(that.amount, amount) == 0 &&
                   timestamp == that.timestamp &&
                   Objects.equals(sender, that.sender) &&
                   Objects.equals(recipient, that.recipient) &&
                   Objects.equals(signature, that.signature);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(sender, recipient, amount, timestamp, signature);
        }
        
        @Override
        public String toString() {
            return "Transaction{" +
                   "sender='" + sender + '\'' +
                   ", recipient='" + recipient + '\'' +
                   ", amount=" + amount +
                   ", timestamp=" + timestamp +
                   ", signature='" + signature + '\'' +
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
        private final int nonce;
        
        public Block(int index, List<Transaction> transactions, String previousHash) {
            this.index = index;
            this.timestamp = System.currentTimeMillis();
            this.transactions = new ArrayList<>(transactions);
            this.previousHash = previousHash;
            this.nonce = 0;
            this.hash = calculateHash();
        }
        
        public Block(int index, long timestamp, List<Transaction> transactions, 
                    String previousHash, String hash, int nonce) {
            this.index = index;
            this.timestamp = timestamp;
            this.transactions = new ArrayList<>(transactions);
            this.previousHash = previousHash;
            this.hash = hash;
            this.nonce = nonce;
        }
        
        public String calculateHash() {
            String dataToHash = index + timestamp + transactions.toString() + previousHash + nonce;
            return Cryptography.sha256(dataToHash);
        }
        
        public boolean hasValidTransactions(Chain blockchain) {
            for (Transaction transaction : transactions) {
                if (!transaction.isValid(blockchain)) {
                    return false;
                }
            }
            return true;
        }
        
        // getters
        public int getIndex() { return index; }
        public long getTimestamp() { return timestamp; }
        public List<Transaction> getTransactions() { return new ArrayList<>(transactions); }
        public String getPreviousHash() { return previousHash; }
        public String getHash() { return hash; }
        public int getNonce() { return nonce; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Block block = (Block) o;
            return index == block.index &&
                   timestamp == block.timestamp &&
                   nonce == block.nonce &&
                   Objects.equals(transactions, block.transactions) &&
                   Objects.equals(previousHash, block.previousHash) &&
                   Objects.equals(hash, block.hash);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(index, timestamp, transactions, previousHash, hash, nonce);
        }
        
        @Override
        public String toString() {
            return "Block{" +
                   "index=" + index +
                   ", timestamp=" + timestamp +
                   ", transactions=" + transactions +
                   ", previousHash='" + previousHash + '\'' +
                   ", hash='" + hash + '\'' +
                   ", nonce=" + nonce +
                   '}';
        }
    }
    
    /**
     * Represents the blockchain itself
     */
    public static class Chain {
        private final List<Block> chain;
        private final List<Transaction> pendingTransactions;
        private final int difficulty;
        private final double miningReward;
        
        public Chain(int difficulty, double miningReward) {
            this.chain = new CopyOnWriteArrayList<>();
            this.pendingTransactions = new CopyOnWriteArrayList<>();
            this.difficulty = difficulty;
            this.miningReward = miningReward;
            
            // create genesis block
            createGenesisBlock();
        }
        
        private void createGenesisBlock() {
            Block genesisBlock = new Block(0, new ArrayList<>(), "0");
            chain.add(genesisBlock);
        }
        
        public Block getLatestBlock() {
            return chain.get(chain.size() - 1);
        }
        
        public boolean addTransaction(Transaction transaction) {
            if (transaction == null) {
                return false;
            }
            
            if (!transaction.isValid(this)) {
                System.err.println("could not add invalid transaction to chain");
                return false;
            }
            
            pendingTransactions.add(transaction);
            return true;
        }
        
        public Block mineBlock(List<Transaction> transactions, String minerAddress) {
            // add mining reward transaction
            Transaction rewardTx = new Transaction("BLOCKCHAIN_REWARD", minerAddress, miningReward, "REWARD");
            List<Transaction> blockTransactions = new ArrayList<>(transactions);
            blockTransactions.add(rewardTx);
            
            Block newBlock = new Block(
                getLatestBlock().getIndex() + 1,
                blockTransactions,
                getLatestBlock().getHash()
            );
            
            // proof of work
            String target = new String(new char[difficulty]).replace('\0', '0');
            while (!newBlock.getHash().substring(0, difficulty).equals(target)) {
                newBlock = new Block(
                    newBlock.getIndex(),
                    newBlock.getTimestamp() + 1, // increment timestamp to change hash
                    newBlock.getTransactions(),
                    newBlock.getPreviousHash(),
                    newBlock.getHash(),
                    newBlock.getNonce() + 1
                );
            }
            
            chain.add(newBlock);
            return newBlock;
        }
        
        public double getBalance(String address) {
            double balance = 0;
            
            for (Block block : chain) {
                for (Transaction transaction : block.getTransactions()) {
                    if (transaction.getRecipient().equals(address)) {
                        balance += transaction.getAmount();
                    }
                    
                    if (transaction.getSender().equals(address)) {
                        balance -= transaction.getAmount();
                    }
                }
            }
            
            return balance;
        }
        
        public boolean isChainValid() {
            for (int i = 1; i < chain.size(); i++) {
                Block currentBlock = chain.get(i);
                Block previousBlock = chain.get(i - 1);
                
                // validate current block's hash
                if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
                    System.err.println("invalid block hash");
                    return false;
                }
                
                // validate link between blocks
                if (!currentBlock.getPreviousHash().equals(previousBlock.getHash())) {
                    System.err.println("invalid previous hash");
                    return false;
                }
                
                // validate transactions in block
                if (!currentBlock.hasValidTransactions(this)) {
                    System.err.println("invalid transactions in block");
                    return false;
                }
            }
            
            return true;
        }
        
        public int getChainLength() {
            return chain.size();
        }
        
        public List<Transaction> getPendingTransactions() {
            return new ArrayList<>(pendingTransactions);
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
            // send the transaction to all connected nodes
            for (Node node : connectedNodes) {
                node.receiveTransaction(transaction);
            }
        }
        
        private void broadcastBlock(Block block) {
            // send the block to all connected nodes
            for (Node node : connectedNodes) {
                node.receiveBlock(block);
            }
        }
        
        public void receiveTransaction(Transaction transaction) {
            if (blockchain.addTransaction(transaction)) {
                pendingTransactions.add(transaction);
            }
        }
        
        public void receiveBlock(Block block) {
            // validate and add the block to the chain
            if (block.hasValidTransactions(blockchain) && 
                block.getPreviousHash().equals(blockchain.getLatestBlock().getHash())) {
                blockchain.chain.add(block);
            }
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