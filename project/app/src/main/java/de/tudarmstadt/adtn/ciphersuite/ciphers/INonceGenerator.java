package de.tudarmstadt.adtn.ciphersuite.ciphers;

/**
 * Interface for a nonce generator. An implementing class has to give the developer the opportunity
 * to pass in a length of the nonce or use a fixed length.
 */
public interface INonceGenerator {

    /**
     * Generates a nonce.
     *
     * @return Returns the nonce as byte array.
     */
    byte[] generateNonce();

    /**
     * Returns the length of this nonce
     *
     * @return int
     */
    int getLength();
}
