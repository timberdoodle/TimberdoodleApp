package de.tudarmstadt.adtn.generickeystore;

import java.security.Key;

/**
 * An entry of the key store.
 *
 * @param <K> The type of key this entry contains.
 */
public class KeyStoreEntry<K extends Key> {

    private final long id;
    private final String alias;
    private final K key;

    /**
     * Creates a new KeyStoreEntry object.
     *
     * @param id    The ID of the entry.
     * @param alias The alias of the entry.
     * @param key   The key.
     */
    public KeyStoreEntry(long id, String alias, K key) {
        this.id = id;
        this.alias = alias;
        this.key = key;
    }

    /**
     * @return The ID of the entry.
     */
    public long getId() {
        return id;
    }

    /**
     * @return The alias of the entry.
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @return The key.
     */
    public K getKey() {
        return key;
    }
}
