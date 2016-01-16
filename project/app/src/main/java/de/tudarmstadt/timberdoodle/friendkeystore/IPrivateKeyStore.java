package de.tudarmstadt.timberdoodle.friendkeystore;

import java.security.KeyPair;

/**
 * Stores the own public/private key pair.
 */
public interface IPrivateKeyStore {

    /**
     * Persistently stores the key pair.
     */
    void save();

    /**
     * @return The own friend cipher key pair.
     */
    KeyPair getKeyPair();

    /**
     * Sets the own friend cipher key pair.
     *
     * @param keyPair The key to use as own private key.
     */
    void setKeyPair(KeyPair keyPair);
}
