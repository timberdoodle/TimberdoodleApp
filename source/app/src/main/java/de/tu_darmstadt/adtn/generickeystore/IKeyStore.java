package de.tu_darmstadt.adtn.generickeystore;

import java.security.Key;
import java.util.Collection;

/**
 * A key store that stores objects inherited from java.security.Key.
 *
 * @param <K> The type of keys the store contains.
 */
public interface IKeyStore<K extends Key> {

    /**
     * Empties the store.
     */
    void clear();

    /**
     * Stores a group key in the key store and saves its creation timestamp.
     *
     * @param alias The alias of the key.
     * @param key   The key.
     * @return A KeyStoreEntry for the newly stored key where alias and key are not null on success.
     * Alias set to null and ID set to the existing entry's ID if the alias is already in use.
     * Key set to null and ID set to the existing entry's ID if the key is already in use.
     */
    KeyStoreEntry<K> addEntry(String alias, K key);

    /**
     * Deletes keys from the store.
     *
     * @param ids The IDs of the keys to delete.
     */
    void deleteEntries(Collection<Long> ids);

    /**
     * Changes the alias of key.
     *
     * @param id       The ID of the entry to rename.
     * @param newAlias The new alias for the key.
     * @return The specified ID on success, the ID of the existing entry if the alias is already in
     * use, or 0 if the specified ID is invalid.
     */
    long renameEntry(long id, String newAlias);

    /**
     * Obtains an entry from the store.
     *
     * @param id The ID of the key.
     * @return The key entry if it was found or null otherwise.
     */
    KeyStoreEntry<K> getEntry(long id);

    /**
     * Returns an unmodifiable copy of all entries currently in the store. Since this is only a
     * snapshot, modifications to the store after the call do not affect the returned entries.
     *
     * @return All entries currently in the store.
     */
    Collection<KeyStoreEntry<K>> getEntries();
}
