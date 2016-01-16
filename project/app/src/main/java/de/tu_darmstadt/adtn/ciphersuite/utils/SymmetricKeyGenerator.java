package de.tu_darmstadt.adtn.ciphersuite.utils;

import java.security.Provider;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import de.tu_darmstadt.adtn.errorlogger.ErrorLoggingSingleton;

/**
 * Generates a symmetric key
 */
public class SymmetricKeyGenerator implements ISymmetricKeyGenerator{

    private final Provider serviceProvider;
    private final String algorithm;
    private final int keyLength;

    public SymmetricKeyGenerator(Provider provider, String algorithm, int keyLength) {
        this.serviceProvider = provider;
        this.algorithm = algorithm;
        this.keyLength = keyLength;
    }


    /**
     * {@inheritDoc}
     */
    public SecretKey generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm, serviceProvider);
            keyGenerator.init(keyLength);
            return keyGenerator.generateKey();
        } catch (Exception e) {
            //Error logging
            ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
            log.storeError(ErrorLoggingSingleton.getExceptionStackTraceAsFormattedString(e));
            throw new RuntimeException();
        }
    }

    /**
     * Reads a key from a byte array and creates a SecretKey from it
     *
     * @param key byte array that holds the key(s)
     * @return Returns the SecretKey instance
     */
    @Override
    public SecretKey readKeyFromByteArray(byte[] key) {
        return new SecretKeySpec(key, algorithm);
    }

    /**
     * Returns the key length
     *
     * @return returns the byte count of the key
     */
    @Override
    public int getLength() {
        return keyLength/8;
    }
}
