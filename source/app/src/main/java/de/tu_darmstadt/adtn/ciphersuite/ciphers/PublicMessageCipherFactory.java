package de.tu_darmstadt.adtn.ciphersuite.ciphers;

import java.security.GeneralSecurityException;
import java.security.Provider;

import javax.crypto.Cipher;

/**
 * Factory to create an IPublicMessageCipher instance. This factory uses Spongy Castle as service provider
 */
public class PublicMessageCipherFactory {

    /**
     * Instantiates and returns an IPublicMessageCipher instance.
     *
     * @param algorithm Determines the algorithm that is used (e.g. "AES", "ChaCha" or "Twofish")
     * @param mode      Determines if the result is set to encryption or decryption.
     * @return Returns a IPublicMessageCipher. IPublicMessageCipher can only handle symmetric algorithms.
     * @throws Exception
     */
    public static IPublicMessageCipher getPublicMessageCipherInstance(String algorithm, Ciphermode mode, int nonceLength) throws GeneralSecurityException {
        Provider provider = new org.spongycastle.jce.provider.BouncyCastleProvider();
        if (mode == Ciphermode.ENCRYPT)
            return new PublicMessageCipherImpl(Cipher.getInstance(algorithm, provider), Cipher.ENCRYPT_MODE, nonceLength);
        else
            return new PublicMessageCipherImpl(Cipher.getInstance(algorithm, provider), Cipher.DECRYPT_MODE, nonceLength);
    }

    /**
     * Instantiates and returns an INonceGenerator instance
     *
     * @param nonceLength Length of the nonce.
     * @return INonceGenerator.
     */
    public static INonceGenerator getNonceGenerator(int nonceLength) {
        return new SimpleNonceGenerator(nonceLength);
    }
}
