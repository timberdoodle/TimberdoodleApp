package de.tudarmstadt.adtn.groupciphersuitetests.mockobjects;

import de.tudarmstadt.adtn.ciphersuite.ciphers.INonceGenerator;
import de.tudarmstadt.adtn.groupciphersuitetests.utils.CipherTestVectors;

/**
 * Mocks the NonceGenerator to load custom nonces.
 */
public class NonceGenMock implements INonceGenerator {

    private byte[][] nonceList;
    private int length;
    private int i = -1;

    public NonceGenMock() {
        nonceList = CipherTestVectors.nonces;
        length = nonceList.length;
    }

    /**
     * Generates a nonce.
     *
     * @return Returns the nonce as byte array.
     */
    @Override
    public byte[] generateNonce() {
        if (i < length) i++;
        else i = 0;
        return nonceList[i];
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
}
