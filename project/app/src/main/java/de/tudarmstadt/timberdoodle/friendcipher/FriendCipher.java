package de.tudarmstadt.timberdoodle.friendcipher;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Collection;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import de.tudarmstadt.adtn.errorlogger.ErrorLoggingSingleton;

/**
 * Provides methods for encryption and decryption of private messages.
 */
public class FriendCipher implements IFriendCipher {

    private final static String CRYPTO_PROVIDER = "BC";
    private final static int ENCRYPTED_AES_KEY_SIZE = 256;
    private final static int HASH_SIZE = 32;
    private final static int AES_BLOCK_SIZE = 16;
    private final static int SIGNATURE_SIZE = 256;
    private final static int KEY_SIZE = 256;

    private final SecureRandom secureRandom;
    private final KeyGenerator aesKeyGenerator;
    private final MessageDigest sha256;
    private final Signature signing, verification;
    private final Cipher rsaWrapCipher, rsaUnwrapCipher, aes;

    // Was the private key already set?
    private volatile boolean privateKeySet = false;

    /**
     * Creates a new FriendCipher object.
     */
    public FriendCipher() {
        secureRandom = new SecureRandom();

        try {
            aesKeyGenerator = KeyGenerator.getInstance("AES", CRYPTO_PROVIDER);
            sha256 = MessageDigest.getInstance("SHA-256", CRYPTO_PROVIDER);
            final String SIGNATURE_ALGORITHM = "SHA256withRSA";
            signing = Signature.getInstance(SIGNATURE_ALGORITHM, CRYPTO_PROVIDER);
            verification = Signature.getInstance(SIGNATURE_ALGORITHM, CRYPTO_PROVIDER);
            final String RSA_TRANSFORMATION = "RSA/None/OAEPWithSHA1AndMGF1Padding";
            rsaWrapCipher = Cipher.getInstance(RSA_TRANSFORMATION, CRYPTO_PROVIDER);
            rsaUnwrapCipher = Cipher.getInstance(RSA_TRANSFORMATION, CRYPTO_PROVIDER);
            aes = Cipher.getInstance("AES/CBC/PKCS7Padding", CRYPTO_PROVIDER);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
            ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
            log.storeError(ErrorLoggingSingleton.getExceptionStackTraceAsFormattedString(e));
            // Bouncy Castle is included and all algorithms/paddings are supported in Bouncy Castle
            throw new RuntimeException(e);
        }
    }

    /**
     * Encrypts the specified plaintext with the specified public key.
     *
     * @param plaintext The plaintext to encrypt.
     * @param offset    The start offset in plaintext.
     * @param count     The number of bytes of plaintext to process.
     * @param key       The to use for encryption.
     * @return The ciphertext.
     */
    @Override
    public byte[] encrypt(byte[] plaintext, int offset, int count, PublicKey key) {
        // Create hash of plaintext and put hash in front of plaintext
        sha256.update(plaintext, offset, count);
        byte[] hash = sha256.digest();
        byte[] buffer = concatByteArrays(hash, 0, hash.length, plaintext, offset, count);

        // Generate AES key and use it to initialize the AES cipher
        SecretKey aesKey = aesKeyGenerator.generateKey();
        try {
            aes.init(Cipher.ENCRYPT_MODE, aesKey);
        } catch (InvalidKeyException e) {
            ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
            log.storeError(ErrorLoggingSingleton.getExceptionStackTraceAsFormattedString(e));
            throw new RuntimeException(e); // Key generator should never generate invalid keys
        }

        // Encrypt hash and plaintext
        try {
            buffer = aes.doFinal(buffer);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            throw new RuntimeException(e); // Padding and block size are correct
        }

        // Store IV in front of AES ciphertext
        buffer = concatByteArrays(aes.getIV(), 0, AES_BLOCK_SIZE, buffer, 0, buffer.length);

        // Encrypt AES key with RSA
        byte[] wrappedAesKey;
        try {
            rsaWrapCipher.init(Cipher.WRAP_MODE, key);
            wrappedAesKey = rsaWrapCipher.wrap(aesKey);
        } catch (IllegalBlockSizeException e) {
            ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
            log.storeError(ErrorLoggingSingleton.getExceptionStackTraceAsFormattedString(e));
            throw new RuntimeException(e); // Cannot happen
        } catch (InvalidKeyException e) {
            ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
            log.storeError(ErrorLoggingSingleton.getExceptionStackTraceAsFormattedString(e));
            // If thrown, then it's caused by init, since aesKey is guaranteed to be valid
            throw new RuntimeException(e);
        }

        // Put encrypted AES key in front of AES ciphertext
        return concatByteArrays(wrappedAesKey, 0, wrappedAesKey.length, buffer, 0, buffer.length);
    }

    /**
     * Tries to decrypt the specified ciphertext using the private key specified in setPrivateKey.
     *
     * @param ciphertext The ciphertext to decrypt.
     * @return The plaintext on success or null otherwise.
     */
    @Override
    public byte[] tryDecrypt(byte[] ciphertext) {
        // If the private key has not been set yet decryption is not possible
        if (!privateKeySet) return null;

        try {
            // Decrypt AES key
            SecretKey aesKey;
            byte[] buffer = Arrays.copyOf(ciphertext, ENCRYPTED_AES_KEY_SIZE);
            aesKey = (SecretKey) rsaUnwrapCipher.unwrap(buffer, "AES", Cipher.SECRET_KEY);

            // Decrypt AES-encrypted hash and plaintext
            aes.init(Cipher.DECRYPT_MODE, aesKey,
                    new IvParameterSpec(ciphertext, ENCRYPTED_AES_KEY_SIZE, AES_BLOCK_SIZE));
            buffer = aes.doFinal(ciphertext, ENCRYPTED_AES_KEY_SIZE + AES_BLOCK_SIZE,
                    ciphertext.length - ENCRYPTED_AES_KEY_SIZE - AES_BLOCK_SIZE);

            // Calculate and compare hash of plaintext
            sha256.update(buffer, HASH_SIZE, buffer.length - HASH_SIZE);
            if (!Arrays.equals(sha256.digest(), Arrays.copyOf(buffer, HASH_SIZE))) {
                throw new RuntimeException("Hash check failed");
            }

            return Arrays.copyOfRange(buffer, HASH_SIZE, buffer.length);
        } catch (Exception e) {
            // Ignore packet if it contains garbage
            return null;
        }
    }

    /**
     * @return The number of bytes a public key that was converted to a byte array consists of.
     */
    @Override
    public int getEncodedPublicKeySize() {
        return 294; // X.509 encoded 2048 bit RSA key
    }

    /**
     * @return The number of bytes a signature consists of.
     */
    @Override
    public int getNumBytesInSignature() {
        return SIGNATURE_SIZE;
    }

    /**
     * Creates a PublicKey object from the raw key bytes obtained through publicKeyToByteArray.
     *
     * @param key The key bytes.
     * @return A PublicKey that encapsulates the raw key or null if the key's format is invalid.
     */
    @Override
    public PublicKey byteArrayToPublicKey(byte[] key) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA", CRYPTO_PROVIDER);
            return keyFactory.generatePublic(new X509EncodedKeySpec(key));
        } catch (InvalidKeySpecException e) {
            return null; // Key in byte array uses invalid format
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
            log.storeError(ErrorLoggingSingleton.getExceptionStackTraceAsFormattedString(e));
            // Bouncy Castle is included and all algorithms/paddings are supported in Bouncy Castle
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a byte array from a public key that can be converted back using byteArrayToPublicKey.
     *
     * @param publicKey The public key.
     * @return A byte array representing the key.
     */
    @Override
    public byte[] publicKeyToByteArray(PublicKey publicKey) {
        return publicKey.getEncoded();
    }

    /**
     * Checks which of the specified public keys can be used to verify the specified data using
     * the specified signature.
     *
     * @param data            The signed data.
     * @param dataOffset      Start offset of the signed data.
     * @param dataCount       Length of the signed data
     * @param signature       Contains the signature.
     * @param signatureOffset Start offset of the signature in buffer.
     * @param publicKeys      The public keys to use verification.
     * @return The public key that belongs to the signature or null if none of the keys worked.
     */
    @Override
    public PublicKey checkSignature(byte[] data, int dataOffset, int dataCount,
                                    byte[] signature, int signatureOffset,
                                    Collection<PublicKey> publicKeys) {
        for (PublicKey key : publicKeys) {
            try {
                verification.initVerify(key);
                verification.update(data, dataOffset, dataCount);
                if (verification.verify(signature, signatureOffset, SIGNATURE_SIZE)) return key;
            } catch (InvalidKeyException e) {
                ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
                log.storeError(ErrorLoggingSingleton.getExceptionStackTraceAsFormattedString(e));
                // Just ignore invalid keys
            } catch (SignatureException e) {
                ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
                log.storeError(ErrorLoggingSingleton.getExceptionStackTraceAsFormattedString(e));
                throw new RuntimeException(e);
            }
        }

        return null; // Could not find a public key for this signature
    }

    /**
     * Creates a signature of the specified data using the private key specified by setPrivateKey.
     *
     * @param buffer The data to sign.
     * @param offset The start offset in buffer.
     * @param count  The number of bytes in buffer to sign.
     * @return The signature.
     */
    @Override
    public byte[] sign(byte[] buffer, int offset, int count) {
        try {
            signing.update(buffer, offset, count);
            return signing.sign();
        } catch (SignatureException e) {
            throw new RuntimeException(e); // Cannot happen
        }
    }

    /**
     * Set the private key to use for decryption and signing.
     *
     * @param privateKey The private key to use for decryption and signing.
     */
    @Override
    public void setPrivateKey(PrivateKey privateKey) {
        try {
            rsaUnwrapCipher.init(Cipher.UNWRAP_MODE, privateKey);
            signing.initSign(privateKey);
        } catch (InvalidKeyException e) {
            ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
            log.storeError(ErrorLoggingSingleton.getExceptionStackTraceAsFormattedString(e));
            throw new RuntimeException(e);
        }

        privateKeySet = true;
    }

    /**
     * Calculates the size of the ciphertext for the specified plaintext size.
     *
     * @param plaintextSize The plaintext size in bytes.
     * @return The size of the ciphertext in bytes.
     */
    @Override
    public int getCiphertextSize(int plaintextSize) {
        // Encrypted AES key + Hash + IV + encrypted plaintext with padding
        return ENCRYPTED_AES_KEY_SIZE + HASH_SIZE + AES_BLOCK_SIZE +
                (plaintextSize / AES_BLOCK_SIZE + 1) * AES_BLOCK_SIZE;
    }

    /**
     * Calculates the maximum plaintext size for the specified ciphertext size.
     *
     * @param ciphertextSize The size of the ciphertext in bytes.
     * @return The maximum plaintext size in bytes. If not even zero bytes of plaintext would fit in
     * the specified ciphertext size, -1 is returned.
     */
    @Override
    public int getMaxPlaintextSize(int ciphertextSize) {
        // AES ciphertext size = Encrypted AES key + Hash + IV
        int aesCiphertextSize = ciphertextSize - ENCRYPTED_AES_KEY_SIZE - HASH_SIZE - AES_BLOCK_SIZE;
        // Not even one block of AES ciphertext would fit? Then not even zero bytes can be encrypted
        if (aesCiphertextSize < AES_BLOCK_SIZE) return -1;
        // Multiple of block size - 1 due to padding
        return aesCiphertextSize - aesCiphertextSize % AES_BLOCK_SIZE - 1;
    }

    /**
     * Generates a private key and its associated public key.
     *
     * @return The key pair containing the private and the public key.
     */
    @Override
    public KeyPair generateKeyPair() {
        KeyPairGenerator keyGenerator = null;
        try {
            keyGenerator = KeyPairGenerator.getInstance("RSA", CRYPTO_PROVIDER);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
            log.storeError(ErrorLoggingSingleton.getExceptionStackTraceAsFormattedString(e));
            // Bouncy Castle is included and all algorithms/paddings are supported in Bouncy Castle
            throw new RuntimeException(e);
        }
        keyGenerator.initialize(KEY_SIZE * 8, secureRandom);
        return keyGenerator.generateKeyPair();
    }

    /**
     * @return true if a valid private key was set by setPrivateKey or false if not done yet.
     */
    @Override
    public boolean isPrivateKeySet() {
        return privateKeySet;
    }

    private byte[] concatByteArrays(byte[] a, int offsetA, int countA,
                                    byte[] b, int offsetB, int countB) {
        byte[] result = new byte[countA + countB];
        System.arraycopy(a, offsetA, result, 0, countA);
        System.arraycopy(b, offsetB, result, countA, countB);
        return result;
    }
}
