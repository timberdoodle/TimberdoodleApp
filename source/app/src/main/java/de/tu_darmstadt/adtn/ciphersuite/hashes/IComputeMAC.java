package de.tu_darmstadt.adtn.ciphersuite.hashes;

import javax.crypto.SecretKey;

/**
 * Interface for MAC creation.
 */
public interface IComputeMAC {

    /**
     * Computes a message authentication code of a given text (as byte array) with a given
     * key and a nonce (iv).
     *
     * @param key  Key that is used to create MAC.
     * @param iv   Nonce that is used to create MAC.
     * @param text Text that is used to create MAC.
     */
    void computeMAC(byte[] iv, SecretKey key, byte[] text, int textOffset, byte[] buffer, int offset);

    /**
     * Returns the length of the MAC.
     *
     * @return Length is measured in bytes. Therefore the returned int is the byte count.
     */
    int length();
}
