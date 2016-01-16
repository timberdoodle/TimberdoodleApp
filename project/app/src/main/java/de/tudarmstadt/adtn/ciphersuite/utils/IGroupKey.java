package de.tudarmstadt.adtn.ciphersuite.utils;

import javax.crypto.SecretKey;

/**
 * Represents a group key
 */
public interface IGroupKey {

    /**
     * Returns the key that is used for encryption/decryption
     *
     * @return
     */
    SecretKey getCipherKey();

    /**
     * Returns the key that is used to calculate the message authentication code
     *
     * @return
     */
    SecretKey getMACKey();
}
