package com.sparky.libx.crypto;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * криптографічні алгоритми та утиліти
 * включає хешування, шифрування, цифрові підписи і генерацію ключів
 * @author Андрій Будильников
 */
public class Cryptography {
    
    private static final SecureRandom random = new SecureRandom();
    
    /**
     * обчислити хеш SHA-256
     * @param input вхідні дані
     * @return хеш у вигляді шістнадцяткового рядка
     */
    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Алгоритм SHA-256 не доступний", e);
        }
    }
    
    /**
     * обчислити хеш SHA-512
     * @param input вхідні дані
     * @return хеш у вигляді шістнадцяткового рядка
     */
    public static String sha512(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Алгоритм SHA-512 не доступний", e);
        }
    }
    
    /**
     * обчислити хеш MD5
     * @param input вхідні дані
     * @return хеш у вигляді шістнадцяткового рядка
     */
    public static String md5(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Алгоритм MD5 не доступний", e);
        }
    }
    
    /**
     * перетворити байти в шістнадцятковий рядок
     * @param bytes масив байтів
     * @return шістнадцятковий рядок
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    
    /**
     * перетворити шістнадцятковий рядок в байти
     * @param hex шістнадцятковий рядок
     * @return масив байтів
     */
    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }
    
    /**
     * закодувати дані в Base64
     * @param data вхідні дані
     * @return закодовані дані
     */
    public static String base64Encode(String data) {
        return Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * декодувати дані з Base64
     * @param data закодовані дані
     * @return декодовані дані
     */
    public static String base64Decode(String data) {
        byte[] decoded = Base64.getDecoder().decode(data);
        return new String(decoded, StandardCharsets.UTF_8);
    }
    
    /**
     * шифр Цезаря
     */
    public static class CaesarCipher {
        
        /**
         * зашифрувати текст шифром Цезаря
         * @param text текст для шифрування
         * @param shift зсув
         * @return зашифрований текст
         */
        public static String encrypt(String text, int shift) {
            StringBuilder result = new StringBuilder();
            
            for (char c : text.toCharArray()) {
                if (Character.isLetter(c)) {
                    char base = Character.isUpperCase(c) ? 'A' : 'a';
                    c = (char) ((c - base + shift) % 26 + base);
                }
                result.append(c);
            }
            
            return result.toString();
        }
        
        /**
         * розшифрувати текст шифром Цезаря
         * @param text текст для розшифрування
         * @param shift зсув
         * @return розшифрований текст
         */
        public static String decrypt(String text, int shift) {
            return encrypt(text, 26 - shift);
        }
    }
    
    /**
     * шифр Віженера
     */
    public static class VigenereCipher {
        
        /**
         * зашифрувати текст шифром Віженера
         * @param text текст для шифрування
         * @param key ключ
         * @return зашифрований текст
         */
        public static String encrypt(String text, String key) {
            StringBuilder result = new StringBuilder();
            key = key.toUpperCase();
            int keyIndex = 0;
            
            for (char c : text.toCharArray()) {
                if (Character.isLetter(c)) {
                    char base = Character.isUpperCase(c) ? 'A' : 'a';
                    char keyChar = key.charAt(keyIndex % key.length());
                    int shift = keyChar - 'A';
                    c = (char) ((c - base + shift) % 26 + base);
                    keyIndex++;
                }
                result.append(c);
            }
            
            return result.toString();
        }
        
        /**
         * розшифрувати текст шифром Віженера
         * @param text текст для розшифрування
         * @param key ключ
         * @return розшифрований текст
         */
        public static String decrypt(String text, String key) {
            StringBuilder result = new StringBuilder();
            key = key.toUpperCase();
            int keyIndex = 0;
            
            for (char c : text.toCharArray()) {
                if (Character.isLetter(c)) {
                    char base = Character.isUpperCase(c) ? 'A' : 'a';
                    char keyChar = key.charAt(keyIndex % key.length());
                    int shift = keyChar - 'A';
                    c = (char) ((c - base - shift + 26) % 26 + base);
                    keyIndex++;
                }
                result.append(c);
            }
            
            return result.toString();
        }
    }
    
    /**
     * RSA криптосистема
     */
    public static class RSA {
        private final BigInteger n; // модуль
        private final BigInteger e; // відкритий експонент
        private final BigInteger d; // приватний експонент
        
        /**
         * створити RSA ключі
         * @param keySize розмір ключа в бітах
         */
        public RSA(int keySize) {
            // для спрощення генеруємо прості числа фіксованого розміру
            BigInteger p = generatePrime(keySize / 2);
            BigInteger q = generatePrime(keySize / 2);
            
            n = p.multiply(q);
            
            BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
            
            // відкритий експонент (зазвичай 65537)
            e = BigInteger.valueOf(65537);
            
            // приватний експонент
            d = e.modInverse(phi);
        }
        
        /**
         * згенерувати просте число
         * @param bitLength довжина в бітах
         * @return просте число
         */
        private BigInteger generatePrime(int bitLength) {
            // для демонстрації повертаємо фіксоване просте число
            // в реальній реалізації потрібно використовувати SecureRandom
            if (bitLength <= 8) {
                return BigInteger.valueOf(97); // просте число
            } else if (bitLength <= 16) {
                return BigInteger.valueOf(65521); // просте число
            } else {
                return BigInteger.probablePrime(bitLength, random);
            }
        }
        
        /**
         * зашифрувати повідомлення
         * @param message повідомлення
         * @return зашифроване повідомлення
         */
        public BigInteger encrypt(BigInteger message) {
            return message.modPow(e, n);
        }
        
        /**
         * розшифрувати повідомлення
         * @param encrypted зашифроване повідомлення
         * @return розшифроване повідомлення
         */
        public BigInteger decrypt(BigInteger encrypted) {
            return encrypted.modPow(d, n);
        }
        
        /**
         * отримати відкритий ключ
         * @return відкритий ключ (n, e)
         */
        public BigInteger[] getPublicKey() {
            return new BigInteger[] {n, e};
        }
        
        /**
         * отримати приватний ключ
         * @return приватний ключ (n, d)
         */
        public BigInteger[] getPrivateKey() {
            return new BigInteger[] {n, d};
        }
    }
    
    /**
     * алгоритм Діффі-Хеллмана для обміну ключами
     */
    public static class DiffieHellman {
        private static final BigInteger P = new BigInteger("FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" +
                                                          "29024E088A67CC74020BBEA63B139B22514A08798E3404DD" +
                                                          "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245" +
                                                          "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED" +
                                                          "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3D" +
                                                          "C2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F" +
                                                          "83655D23DCA3AD961C62F356208552BB9ED529077096966D" +
                                                          "670C354E4ABC9804F1746C08CA18217C32905E462E36CE3B" +
                                                          "E39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9" +
                                                          "DE2BCBF6955817183995497CEA956AE515D2261898FA0510" +
                                                          "15728E5A8AACAA68FFFFFFFFFFFFFFFF", 16);
        private static final BigInteger G = BigInteger.valueOf(2);
        
        private final BigInteger privateKey;
        private final BigInteger publicKey;
        
        public DiffieHellman() {
            // генерувати приватний ключ
            privateKey = new BigInteger(P.bitLength() - 1, random);
            
            // обчислити відкритий ключ
            publicKey = G.modPow(privateKey, P);
        }
        
        /**
         * отримати відкритий ключ
         * @return відкритий ключ
         */
        public BigInteger getPublicKey() {
            return publicKey;
        }
        
        /**
         * обчислити спільний секретний ключ
         * @param otherPublicKey відкритий ключ іншої сторони
         * @return спільний секретний ключ
         */
        public BigInteger computeSharedSecret(BigInteger otherPublicKey) {
            return otherPublicKey.modPow(privateKey, P);
        }
    }
    
    /**
     * цифровий підпис RSA
     */
    public static class RSASignature {
        private final RSA rsa;
        
        public RSASignature(int keySize) {
            this.rsa = new RSA(keySize);
        }
        
        /**
         * підписати повідомлення
         * @param message повідомлення
         * @return підпис
         */
        public BigInteger sign(BigInteger message) {
            // в реальній реалізації потрібно застосовувати хешування
            return rsa.decrypt(message); // використовуємо приватний ключ
        }
        
        /**
         * перевірити підпис
         * @param message повідомлення
         * @param signature підпис
         * @return true якщо підпис дійсний
         */
        public boolean verify(BigInteger message, BigInteger signature) {
            BigInteger decrypted = rsa.encrypt(signature); // використовуємо відкритий ключ
            return decrypted.equals(message);
        }
        
        /**
         * отримати відкритий ключ
         * @return відкритий ключ
         */
        public BigInteger[] getPublicKey() {
            return rsa.getPublicKey();
        }
    }
    
    /**
     * генерувати криптографічно безпечне випадкове число
     * @param max максимальне значення
     * @return випадкове число
     */
    public static int secureRandomInt(int max) {
        return random.nextInt(max);
    }
    
    /**
     * генерувати криптографічно безпечне випадкове число в діапазоні
     * @param min мінімальне значення
     * @param max максимальне значення
     * @return випадкове число
     */
    public static int secureRandomInt(int min, int max) {
        return random.nextInt(max - min) + min;
    }
    
    /**
     * генерувати криптографічно безпечний випадковий масив байтів
     * @param length довжина масиву
     * @return масив випадкових байтів
     */
    public static byte[] secureRandomBytes(int length) {
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }
    
    /**
     * шифр XOR
     */
    public static class XORCipher {
        
        /**
         * зашифрувати дані шифром XOR
         * @param data дані для шифрування
         * @param key ключ
         * @return зашифровані дані
         */
        public static byte[] encrypt(byte[] data, byte[] key) {
            byte[] result = new byte[data.length];
            
            for (int i = 0; i < data.length; i++) {
                result[i] = (byte) (data[i] ^ key[i % key.length]);
            }
            
            return result;
        }
        
        /**
         * розшифрувати дані шифром XOR
         * @param data зашифровані дані
         * @param key ключ
         * @return розшифровані дані
         */
        public static byte[] decrypt(byte[] data, byte[] key) {
            // XOR шифр симетричний, тому шифрування і розшифрування однакові
            return encrypt(data, key);
        }
    }
    
    /**
     * обчислити контрольну суму CRC32
     * @param data вхідні дані
     * @return контрольна сума
     */
    public static long crc32(byte[] data) {
        // таблиця CRC32
        long[] crcTable = new long[256];
        for (int i = 0; i < 256; i++) {
            long crc = i;
            for (int j = 0; j < 8; j++) {
                if ((crc & 1) == 1) {
                    crc = (crc >>> 1) ^ 0xEDB88320L;
                } else {
                    crc >>>= 1;
                }
            }
            crcTable[i] = crc;
        }
        
        // обчислити CRC32
        long crc = 0xFFFFFFFFL;
        for (byte b : data) {
            crc = (crc >>> 8) ^ crcTable[(int) ((crc ^ (b & 0xFF)) & 0xFF)];
        }
        
        return crc ^ 0xFFFFFFFFL;
    }
    
    /**
     * генерувати сіль для хешування
     * @param length довжина солі
     * @return сіль
     */
    public static String generateSalt(int length) {
        byte[] salt = new byte[length];
        random.nextBytes(salt);
        return bytesToHex(salt);
    }
    
    /**
     * хешувати пароль з сіллю
     * @param password пароль
     * @param salt сіль
     * @return хеш пароля з сіллю
     */
    public static String hashPassword(String password, String salt) {
        return sha256(password + salt);
    }
}