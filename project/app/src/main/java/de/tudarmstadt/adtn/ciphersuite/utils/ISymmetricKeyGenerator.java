package de.tudarmstadt.adtn.ciphersuite.utils;

import javax.crypto.SecretKey;

/**
 * Interface for key generating classes.
 */
public interface ISymmetricKeyGenerator {

    /**
     * Generates a secret key for symmetric algorithms.
     *
     * @return Returns a symmetric SecretKey
     */
    SecretKey generateKey();

    /**
     * Reads a key from a byte array and creates a SecretKey from it
     * @param key byte array that holds the key(s)
     * @return Returns the SecretKey instance
     */
    SecretKey readKeyFromByteArray(byte[] key);

    /**
     * Returns the key length
     * @return returns the byte count of the key
     */
    int getLength();
}
