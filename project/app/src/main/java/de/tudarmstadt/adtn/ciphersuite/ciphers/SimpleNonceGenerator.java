package de.tudarmstadt.adtn.ciphersuite.ciphers;

import java.util.Random;

/**
 * A simple nonce generator that uses a pseudo random number generator to generate a nonce.
 * The length of the nonce has to be defined as a parameter in the constructor and cannot be changed
 * afterwards. Take into account that the length is defined in bytes not in bits.
 */
public class SimpleNonceGenerator implements INonceGenerator {

    private final int length;

    public SimpleNonceGenerator(int length) {
        this.length = length;
    }


    /**
     * Returns the length of this nonce
     *
     * @return int
     */
    @Override
    public int getLength() {
        return length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] generateNonce() {
        byte[] nonce = new byte[length];
        Random rand = new Random();
        rand.nextBytes(nonce);
        return nonce;
    }
}
