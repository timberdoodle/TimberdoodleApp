package de.tudarmstadt.timberdoodle.friendcipher;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collection;

/**
 * Provides methods for encryption and decryption of private messages.
 */
public interface IFriendCipher {

    /**
     * Encrypts the specified plaintext with the specified public key.
     *
     * @param plaintext The plaintext to encrypt.
     * @param offset    The start offset in plaintext.
     * @param count     The number of bytes of plaintext to process.
     * @param key       The to use for encryption.
     * @return The ciphertext.
     */
    byte[] encrypt(byte[] plaintext, int offset, int count, PublicKey key);

    /**
     * Tries to decrypt the specified ciphertext using the private key specified in setPrivateKey.
     *
     * @param ciphertext The ciphertext to decrypt.
     * @return The plaintext on success or null otherwise.
     */
    byte[] tryDecrypt(byte[] ciphertext);

    /**
     * @return The number of bytes a public key that was converted to a byte array consists of.
     */
    int getEncodedPublicKeySize();

    /**
     * @return The number of bytes a signature consists of.
     */
    int getNumBytesInSignature();

    /**
     * Creates a PublicKey object from the raw key bytes obtained through publicKeyToByteArray.
     *
     * @param key The key bytes.
     * @return A PublicKey that encapsulates the raw key or null if the key's format is invalid.
     */
    PublicKey byteArrayToPublicKey(byte[] key);

    /**
     * Creates a byte array from a public key that can be converted back using byteArrayToPublicKey.
     *
     * @param publicKey The public key.
     * @return A byte array representing the key.
     */
    byte[] publicKeyToByteArray(PublicKey publicKey);

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
    PublicKey checkSignature(byte[] data, int dataOffset, int dataCount,
                             byte[] signature, int signatureOffset,
                             Collection<PublicKey> publicKeys);

    /**
     * Creates a signature of the specified data using the private key specified by setPrivateKey.
     *
     * @param buffer The data to sign.
     * @param offset The start offset in buffer.
     * @param count  The number of bytes in buffer to sign.
     * @return The signature.
     */
    byte[] sign(byte[] buffer, int offset, int count);

    /**
     * Set the private key to use for decryption and signing.
     *
     * @param privateKey The private key to use for decryption and signing.
     */
    void setPrivateKey(PrivateKey privateKey);

    /**
     * Calculates the size of the ciphertext for the specified plaintext size.
     *
     * @param plaintextSize The plaintext size in bytes.
     * @return The size of the ciphertext in bytes.
     */
    int getCiphertextSize(int plaintextSize);

    /**
     * Calculates the maximum plaintext size for the specified ciphertext size.
     *
     * @param ciphertextSize The size of the ciphertext in bytes.
     * @return The maximum plaintext size in bytes. If not even zero bytes of plaintext would fit in
     * the specified ciphertext size, -1 is returned.
     */
    int getMaxPlaintextSize(int ciphertextSize);

    /**
     * Generates a private key and its associated public key.
     *
     * @return The key pair containing the private and the public key.
     */
    KeyPair generateKeyPair();

    /**
     * @return true if a valid private key was set by setPrivateKey or false if not done yet.
     */
    boolean isPrivateKeySet();
}
