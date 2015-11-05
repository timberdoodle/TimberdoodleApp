package de.tu_darmstadt.adtn.groupkeystore;

import java.util.Collection;

import javax.crypto.SecretKey;

import de.tu_darmstadt.adtn.generickeystore.IKeyStore;

/**
 * A key store for group keys (symmetric keys).
 */
public interface IGroupKeyStore extends IKeyStore<SecretKey> {

    int MAX_LENGTH_GROUP_NAME = 15;

    /**
     * Persistently stores the entries currently in the store so they can be retrieved when the
     * store is loaded again.
     */
    void save();

    /**
     * @return An unmodifiable snapshot of the keys currently in the store.
     */
    Collection<SecretKey> getKeys();
}
