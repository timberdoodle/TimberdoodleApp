package de.tu_darmstadt.adtn.ciphersuite.ciphers;

import javax.crypto.SecretKey;

/**
 * A Cipherclass that encrypts a plaintext with a given key and an initialisation vector (nonce).
 * The specific algorithm is defined by a IPublicMessageCipherBuilder instance or on instantiation
 * of a class that implements this interface. Although it is recommended to use a builder (instance of IPublicMessageCipherBuilder)
 * in order to keep the classes consistent with a specific algorithm.
 * Side note: This class can be used with most symmetric block ciphers and stream ciphers that use
 * a nonce/ an initialisation vector.
 */
public interface IPublicMessageCipher {

    /**
     * Encrypts or decrypts the text and writes the result into the buffer starting at index Offset.
     * The mode (encryption or decryption) should be set in the implementing classes' constructor.
     *
     * @param ivBytes    Initialisation vector / nonce.
     * @param key        Key.
     * @param text       Plain text/cipher text that is encrypted / decrypted (depending on the previously set mode).
     * @param textOffset Start index for the operation.
     * @param outBuffer  Buffer into which the result is written.
     * @param outOffset  Start index of the result.
     */
    void doFinalOptimized(byte[] ivBytes, SecretKey key, byte[] text, int textOffset, byte[] outBuffer, int outOffset);

    /**
     * Returns the nonce length for this specific cipher.
     *
     * @return returns teh legnth in bytes.
     */
    int getNonceLength();
}
