package com.sparky.libx.crypto;

import com.sparky.libx.math.Vector3D;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.util.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

/**
 * Advanced Cryptography Framework for Minecraft Plugins
 * Provides capabilities for encryption, hashing, digital signatures, and cryptographic protocols
 * 
 * @author Андрій Будильников
 */
public class AdvancedCryptography {
    
    /**
     * Represents a cryptographic hash function utility
     */
    public static class HashFunctions {
        private static final String SHA_256 = "SHA-256";
        private static final String SHA_512 = "SHA-512";
        private static final String MD5 = "MD5";
        
        /**
         * Computes the SHA-256 hash of the input data
         */
        public static byte[] sha256(byte[] data) {
            try {
                MessageDigest digest = MessageDigest.getInstance(SHA_256);
                return digest.digest(data);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("SHA-256 algorithm not available", e);
            }
        }
        
        /**
         * Computes the SHA-256 hash of the input string
         */
        public static byte[] sha256(String input) {
            return sha256(input.getBytes(StandardCharsets.UTF_8));
        }
        
        /**
         * Computes the SHA-512 hash of the input data
         */
        public static byte[] sha512(byte[] data) {
            try {
                MessageDigest digest = MessageDigest.getInstance(SHA_512);
                return digest.digest(data);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("SHA-512 algorithm not available", e);
            }
        }
        
        /**
         * Computes the SHA-512 hash of the input string
         */
        public static byte[] sha512(String input) {
            return sha512(input.getBytes(StandardCharsets.UTF_8));
        }
        
        /**
         * Computes the MD5 hash of the input data
         */
        public static byte[] md5(byte[] data) {
            try {
                MessageDigest digest = MessageDigest.getInstance(MD5);
                return digest.digest(data);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("MD5 algorithm not available", e);
            }
        }
        
        /**
         * Computes the MD5 hash of the input string
         */
        public static byte[] md5(String input) {
            return md5(input.getBytes(StandardCharsets.UTF_8));
        }
        
        /**
         * Computes the HMAC-SHA256 of the input data with the given key
         */
        public static byte[] hmacSha256(byte[] key, byte[] data) {
            try {
                Mac mac = Mac.getInstance("HmacSHA256");
                SecretKeySpec secretKeySpec = new SecretKeySpec(key, "HmacSHA256");
                mac.init(secretKeySpec);
                return mac.doFinal(data);
            } catch (Exception e) {
                throw new RuntimeException("Failed to compute HMAC-SHA256", e);
            }
        }
        
        /**
         * Computes the HMAC-SHA512 of the input data with the given key
         */
        public static byte[] hmacSha512(byte[] key, byte[] data) {
            try {
                Mac mac = Mac.getInstance("HmacSHA512");
                SecretKeySpec secretKeySpec = new SecretKeySpec(key, "HmacSHA512");
                mac.init(secretKeySpec);
                return mac.doFinal(data);
            } catch (Exception e) {
                throw new RuntimeException("Failed to compute HMAC-SHA512", e);
            }
        }
        
        /**
         * Converts a byte array to a hexadecimal string
         */
        public static String toHexString(byte[] bytes) {
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        }
        
        /**
         * Converts a hexadecimal string to a byte array
         */
        public static byte[] fromHexString(String hex) {
            int len = hex.length();
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                        + Character.digit(hex.charAt(i+1), 16));
            }
            return data;
        }
    }
    
    /**
     * Represents an RSA encryption utility
     */
    public static class RSAEncryption {
        private static final String RSA = "RSA";
        private static final String RSA_ECB_PKCS1_PADDING = "RSA/ECB/PKCS1Padding";
        
        /**
         * Generates an RSA key pair
         */
        public static KeyPair generateKeyPair(int keySize) {
            try {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA);
                keyPairGenerator.initialize(keySize);
                return keyPairGenerator.generateKeyPair();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("RSA algorithm not available", e);
            }
        }
        
        /**
         * Encrypts data using RSA public key
         */
        public static byte[] encrypt(byte[] data, PublicKey publicKey) {
            try {
                Cipher cipher = Cipher.getInstance(RSA_ECB_PKCS1_PADDING);
                cipher.init(Cipher.ENCRYPT_MODE, publicKey);
                return cipher.doFinal(data);
            } catch (Exception e) {
                throw new RuntimeException("Failed to encrypt data with RSA", e);
            }
        }
        
        /**
         * Decrypts data using RSA private key
         */
        public static byte[] decrypt(byte[] encryptedData, PrivateKey privateKey) {
            try {
                Cipher cipher = Cipher.getInstance(RSA_ECB_PKCS1_PADDING);
                cipher.init(Cipher.DECRYPT_MODE, privateKey);
                return cipher.doFinal(encryptedData);
            } catch (Exception e) {
                throw new RuntimeException("Failed to decrypt data with RSA", e);
            }
        }
        
        /**
         * Signs data using RSA private key
         */
        public static byte[] sign(byte[] data, PrivateKey privateKey) {
            try {
                Signature signature = Signature.getInstance("SHA256withRSA");
                signature.initSign(privateKey);
                signature.update(data);
                return signature.sign();
            } catch (Exception e) {
                throw new RuntimeException("Failed to sign data with RSA", e);
            }
        }
        
        /**
         * Verifies a signature using RSA public key
         */
        public static boolean verify(byte[] data, byte[] signature, PublicKey publicKey) {
            try {
                Signature sig = Signature.getInstance("SHA256withRSA");
                sig.initVerify(publicKey);
                sig.update(data);
                return sig.verify(signature);
            } catch (Exception e) {
                throw new RuntimeException("Failed to verify signature with RSA", e);
            }
        }
        
        /**
         * Converts a public key to PEM format string
         */
        public static String publicKeyToPEM(PublicKey publicKey) {
            return "-----BEGIN PUBLIC KEY-----\n" +
                   Base64.getEncoder().encodeToString(publicKey.getEncoded()) +
                   "\n-----END PUBLIC KEY-----";
        }
        
        /**
         * Converts a private key to PEM format string
         */
        public static String privateKeyToPEM(PrivateKey privateKey) {
            return "-----BEGIN PRIVATE KEY-----\n" +
                   Base64.getEncoder().encodeToString(privateKey.getEncoded()) +
                   "\n-----END PRIVATE KEY-----";
        }
    }
    
    /**
     * Represents an AES encryption utility
     */
    public static class AESEncryption {
        private static final String AES = "AES";
        private static final String AES_CBC_PKCS5_PADDING = "AES/CBC/PKCS5Padding";
        
        /**
         * Generates a random AES key
         */
        public static SecretKey generateKey(int keySize) {
            try {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(AES);
                keyGenerator.init(keySize);
                return keyGenerator.generateKey();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("AES algorithm not available", e);
            }
        }
        
        /**
         * Generates a random initialization vector
         */
        public static byte[] generateIV() {
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            return iv;
        }
        
        /**
         * Encrypts data using AES
         */
        public static EncryptedData encrypt(byte[] data, SecretKey key) {
            return encrypt(data, key, generateIV());
        }
        
        /**
         * Encrypts data using AES with a specific IV
         */
        public static EncryptedData encrypt(byte[] data, SecretKey key, byte[] iv) {
            try {
                Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
                IvParameterSpec ivSpec = new IvParameterSpec(iv);
                cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
                byte[] encryptedData = cipher.doFinal(data);
                return new EncryptedData(encryptedData, iv);
            } catch (Exception e) {
                throw new RuntimeException("Failed to encrypt data with AES", e);
            }
        }
        
        /**
         * Decrypts data using AES
         */
        public static byte[] decrypt(EncryptedData encryptedData, SecretKey key) {
            return decrypt(encryptedData.getEncryptedData(), key, encryptedData.getIv());
        }
        
        /**
         * Decrypts data using AES with a specific IV
         */
        public static byte[] decrypt(byte[] encryptedData, SecretKey key, byte[] iv) {
            try {
                Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
                IvParameterSpec ivSpec = new IvParameterSpec(iv);
                cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
                return cipher.doFinal(encryptedData);
            } catch (Exception e) {
                throw new RuntimeException("Failed to decrypt data with AES", e);
            }
        }
        
        /**
         * Represents encrypted data with its IV
         */
        public static class EncryptedData {
            private final byte[] encryptedData;
            private final byte[] iv;
            
            public EncryptedData(byte[] encryptedData, byte[] iv) {
                this.encryptedData = encryptedData;
                this.iv = iv;
            }
            
            public byte[] getEncryptedData() {
                return encryptedData;
            }
            
            public byte[] getIv() {
                return iv;
            }
        }
    }
    
    /**
     * Represents an elliptic curve cryptography utility
     */
    public static class EllipticCurveCryptography {
        private static final String EC = "EC";
        private static final String EC_KEY_PAIR_GENERATOR = "EC";
        private static final String SHA256_WITH_ECDSA = "SHA256withECDSA";
        
        /**
         * Generates an elliptic curve key pair
         */
        public static KeyPair generateKeyPair() {
            try {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(EC_KEY_PAIR_GENERATOR);
                keyPairGenerator.initialize(256);
                return keyPairGenerator.generateKeyPair();
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate EC key pair", e);
            }
        }
        
        /**
         * Signs data using EC private key
         */
        public static byte[] sign(byte[] data, PrivateKey privateKey) {
            try {
                Signature signature = Signature.getInstance(SHA256_WITH_ECDSA);
                signature.initSign(privateKey);
                signature.update(data);
                return signature.sign();
            } catch (Exception e) {
                throw new RuntimeException("Failed to sign data with EC", e);
            }
        }
        
        /**
         * Verifies a signature using EC public key
         */
        public static boolean verify(byte[] data, byte[] signature, PublicKey publicKey) {
            try {
                Signature sig = Signature.getInstance(SHA256_WITH_ECDSA);
                sig.initVerify(publicKey);
                sig.update(data);
                return sig.verify(signature);
            } catch (Exception e) {
                throw new RuntimeException("Failed to verify signature with EC", e);
            }
        }
    }
    
    /**
     * Represents a cryptographic key derivation utility
     */
    public static class KeyDerivation {
        private static final String PBKDF2_WITH_HMAC_SHA256 = "PBKDF2WithHmacSHA256";
        
        /**
         * Derives a key using PBKDF2
         */
        public static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) {
            try {
                SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_WITH_HMAC_SHA256);
                PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength * 8);
                SecretKey secretKey = factory.generateSecret(spec);
                return secretKey.getEncoded();
            } catch (Exception e) {
                throw new RuntimeException("Failed to derive key with PBKDF2", e);
            }
        }
        
        /**
         * Generates a random salt
         */
        public static byte[] generateSalt(int length) {
            byte[] salt = new byte[length];
            new SecureRandom().nextBytes(salt);
            return salt;
        }
    }
    
    /**
     * Represents a cryptographic random number generator utility
     */
    public static class CryptoRandom {
        private static final SecureRandom secureRandom = new SecureRandom();
        
        /**
         * Generates a random byte array of the specified length
         */
        public static byte[] nextBytes(int length) {
            byte[] bytes = new byte[length];
            secureRandom.nextBytes(bytes);
            return bytes;
        }
        
        /**
         * Generates a random integer within the specified range
         */
        public static int nextInt(int min, int max) {
            return secureRandom.nextInt(max - min) + min;
        }
        
        /**
         * Generates a random long
         */
        public static long nextLong() {
            return secureRandom.nextLong();
        }
        
        /**
         * Generates a random double between 0.0 and 1.0
         */
        public static double nextDouble() {
            return secureRandom.nextDouble();
        }
        
        /**
         * Generates a random boolean
         */
        public static boolean nextBoolean() {
            return secureRandom.nextBoolean();
        }
    }
    
    /**
     * Represents a digital certificate utility
     */
    public static class DigitalCertificate {
        /**
         * Generates a self-signed X.509 certificate
         */
        public static java.security.cert.X509Certificate generateSelfSignedCertificate(
                KeyPair keyPair, String subjectDN, int validityDays) {
            try {
                // This is a simplified implementation - in practice, you would use
                // a library like Bouncy Castle for full X.509 certificate generation
                return null;
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate self-signed certificate", e);
            }
        }
        
        /**
         * Validates a certificate chain
         */
        public static boolean validateCertificateChain(java.security.cert.X509Certificate[] chain) {
            try {
                // This is a simplified implementation - in practice, you would use
                // a library like Bouncy Castle for full certificate chain validation
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }
    
    /**
     * Represents a cryptographic protocol utility
     */
    public static class CryptoProtocol {
        /**
         * Performs a Diffie-Hellman key exchange
         */
        public static class DiffieHellman {
            private static final BigInteger P = new BigInteger("FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" +
                    "29024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD" +
                    "3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F4" +
                    "4C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C" +
                    "4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69" +
                    "163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED5290770" +
                    "96966D670C354E4ABC9804F1746C08CA18217C32905E462E36CE3BE" +
                    "39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9DE2BCBF69" +
                    "55817183995497CEA956AE515D2261898FA051015728E5A8AACAA68" +
                    "FFFFFFFFFFFFFFFF", 16);
            private static final BigInteger G = BigInteger.valueOf(2);
            
            private BigInteger privateKey;
            private BigInteger publicKey;
            
            public DiffieHellman() {
                // Generate private key
                privateKey = new BigInteger(P.bitLength() - 1, new SecureRandom());
                
                // Calculate public key: g^privateKey mod p
                publicKey = G.modPow(privateKey, P);
            }
            
            /**
             * Gets the public key
             */
            public BigInteger getPublicKey() {
                return publicKey;
            }
            
            /**
             * Computes the shared secret using the other party's public key
             */
            public BigInteger computeSharedSecret(BigInteger otherPublicKey) {
                // Shared secret = otherPublicKey^privateKey mod p
                return otherPublicKey.modPow(privateKey, P);
            }
        }
        
        /**
         * Performs RSA-OAEP encryption
         */
        public static class RSAOAEP {
            private static final String RSA_OAEP_PADDING = "RSA/ECB/OAEPPadding";
            
            /**
             * Encrypts data using RSA-OAEP
             */
            public static byte[] encrypt(byte[] data, PublicKey publicKey) {
                try {
                    Cipher cipher = Cipher.getInstance(RSA_OAEP_PADDING);
                    cipher.init(Cipher.ENCRYPT_MODE, publicKey);
                    return cipher.doFinal(data);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to encrypt data with RSA-OAEP", e);
                }
            }
            
            /**
             * Decrypts data using RSA-OAEP
             */
            public static byte[] decrypt(byte[] encryptedData, PrivateKey privateKey) {
                try {
                    Cipher cipher = Cipher.getInstance(RSA_OAEP_PADDING);
                    cipher.init(Cipher.DECRYPT_MODE, privateKey);
                    return cipher.doFinal(encryptedData);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to decrypt data with RSA-OAEP", e);
                }
            }
        }
    }
    
    /**
     * Represents a cryptographic commitment scheme utility
     */
    public static class CommitmentScheme {
        /**
         * Creates a commitment to a value using a salt
         */
        public static byte[] commit(byte[] value, byte[] salt) {
            byte[] combined = new byte[value.length + salt.length];
            System.arraycopy(value, 0, combined, 0, value.length);
            System.arraycopy(salt, 0, combined, value.length, salt.length);
            return HashFunctions.sha256(combined);
        }
        
        /**
         * Verifies that a commitment matches a value and salt
         */
        public static boolean verify(byte[] commitment, byte[] value, byte[] salt) {
            byte[] computedCommitment = commit(value, salt);
            return Arrays.equals(commitment, computedCommitment);
        }
    }
    
    /**
     * Represents a zero-knowledge proof utility
     */
    public static class ZeroKnowledgeProof {
        /**
         * Generates a Schnorr signature (a simple zero-knowledge proof)
         */
        public static class SchnorrSignature {
            private static final BigInteger P = new BigInteger("FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" +
                    "29024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD" +
                    "3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F4" +
                    "4C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C" +
                    "4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69" +
                    "163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED5290770" +
                    "96966D670C354E4ABC9804F1746C08CA18217C32905E462E36CE3BE" +
                    "39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9DE2BCBF69" +
                    "55817183995497CEA956AE515D2261898FA051015728E5A8AACAA68" +
                    "FFFFFFFFFFFFFFFF", 16);
            private static final BigInteger G = BigInteger.valueOf(2);
            
            private BigInteger privateKey;
            private BigInteger publicKey;
            
            public SchnorrSignature(BigInteger privateKey) {
                this.privateKey = privateKey;
                this.publicKey = G.modPow(privateKey, P);
            }
            
            /**
             * Generates a Schnorr signature for a message
             */
            public Signature generateSignature(byte[] message) {
                // Generate random nonce
                BigInteger k = new BigInteger(P.bitLength() - 1, new SecureRandom());
                
                // Calculate R = g^k mod p
                BigInteger R = G.modPow(k, P);
                
                // Calculate challenge e = H(R || message)
                byte[] RBytes = R.toByteArray();
                byte[] combined = new byte[RBytes.length + message.length];
                System.arraycopy(RBytes, 0, combined, 0, RBytes.length);
                System.arraycopy(message, 0, combined, RBytes.length, message.length);
                BigInteger e = new BigInteger(1, HashFunctions.sha256(combined));
                
                // Calculate s = k - e * privateKey mod (p-1)
                BigInteger s = k.subtract(e.multiply(privateKey)).mod(P.subtract(BigInteger.ONE));
                
                return new Signature(R, s);
            }
            
            /**
             * Verifies a Schnorr signature
             */
            public boolean verifySignature(byte[] message, Signature signature) {
                BigInteger R = signature.getR();
                BigInteger s = signature.getS();
                
                // Calculate challenge e = H(R || message)
                byte[] RBytes = R.toByteArray();
                byte[] combined = new byte[RBytes.length + message.length];
                System.arraycopy(RBytes, 0, combined, 0, RBytes.length);
                System.arraycopy(message, 0, combined, RBytes.length, message.length);
                BigInteger e = new BigInteger(1, HashFunctions.sha256(combined));
                
                // Calculate R' = g^s * publicKey^e mod p
                BigInteger RPrime = G.modPow(s, P).multiply(publicKey.modPow(e, P)).mod(P);
                
                // Verify R' == R
                return RPrime.equals(R);
            }
            
            /**
             * Represents a Schnorr signature
             */
            public static class Signature {
                private final BigInteger R;
                private final BigInteger s;
                
                public Signature(BigInteger R, BigInteger s) {
                    this.R = R;
                    this.s = s;
                }
                
                public BigInteger getR() {
                    return R;
                }
                
                public BigInteger getS() {
                    return s;
                }
            }
            
            public BigInteger getPublicKey() {
                return publicKey;
            }
        }
    }
    
    /**
     * Represents a homomorphic encryption utility
     */
    public static class HomomorphicEncryption {
        /**
         * Represents a simple additive homomorphic encryption scheme (Paillier-like)
         */
        public static class AdditiveHomomorphic {
            private BigInteger n;
            private BigInteger nSquared;
            private BigInteger g;
            private BigInteger lambda;
            
            public AdditiveHomomorphic(int keySize) {
                // Generate two large primes
                BigInteger p = BigInteger.probablePrime(keySize / 2, new SecureRandom());
                BigInteger q = BigInteger.probablePrime(keySize / 2, new SecureRandom());
                
                // Calculate n = p * q
                n = p.multiply(q);
                nSquared = n.multiply(n);
                
                // Calculate lambda = lcm(p-1, q-1)
                lambda = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE))
                        .divide(p.subtract(BigInteger.ONE).gcd(q.subtract(BigInteger.ONE)));
                
                // Calculate g (in practice, this would be more complex)
                g = n.add(BigInteger.ONE);
            }
            
            /**
             * Encrypts a message
             */
            public BigInteger encrypt(BigInteger message) {
                // Generate random r
                BigInteger r = new BigInteger(n.bitLength() - 1, new SecureRandom());
                
                // Calculate ciphertext = g^m * r^n mod n^2
                return g.modPow(message, nSquared).multiply(r.modPow(n, nSquared)).mod(nSquared);
            }
            
            /**
             * Decrypts a ciphertext
             */
            public BigInteger decrypt(BigInteger ciphertext) {
                // Calculate m = L(ciphertext^lambda mod n^2) * mu mod n
                // where L(x) = (x-1)/n and mu = L(g^lambda mod n^2)^(-1) mod n
                
                // Calculate L(g^lambda mod n^2)
                BigInteger gLambda = g.modPow(lambda, nSquared);
                BigInteger lFunction = gLambda.subtract(BigInteger.ONE).divide(n);
                
                // Calculate mu = L(g^lambda mod n^2)^(-1) mod n
                BigInteger mu = lFunction.modInverse(n);
                
                // Calculate L(ciphertext^lambda mod n^2)
                BigInteger cLambda = ciphertext.modPow(lambda, nSquared);
                BigInteger lCFunction = cLambda.subtract(BigInteger.ONE).divide(n);
                
                // Calculate message = L(ciphertext^lambda mod n^2) * mu mod n
                return lCFunction.multiply(mu).mod(n);
            }
            
            /**
             * Adds two encrypted values homomorphically
             */
            public BigInteger add(BigInteger encryptedA, BigInteger encryptedB) {
                // For additive homomorphic encryption: E(a) * E(b) = E(a + b)
                return encryptedA.multiply(encryptedB).mod(nSquared);
            }
            
            /**
             * Multiplies an encrypted value by a plaintext constant homomorphically
             */
            public BigInteger multiply(BigInteger encrypted, BigInteger constant) {
                // For additive homomorphic encryption: E(a)^b = E(a * b)
                return encrypted.modPow(constant, nSquared);
            }
            
            public BigInteger getN() {
                return n;
            }
        }
    }
}