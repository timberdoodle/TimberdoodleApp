package de.tudarmstadt.adtn.generickeystore;

import java.security.Key;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * A key store that stores objects inherited from java.security.Key.
 *
 * @param <K> The type of keys the store contains.
 */
public abstract class KeyStore<K extends Key> implements IKeyStore<K> {

    private final Object entriesLock = new Object(); // Lock to use when accessing entries
    private HashMap<Long, KeyStoreEntry<K>> entries; // The (modifiable) entries
    // Contains entries.values() and is unmodifiable and thread-safe
    private volatile Collection<KeyStoreEntry<K>> unmodifiableEntries;

    /**
     * Empties the store.
     */
    @Override
    public void clear() {
        synchronized (entriesLock) {
            entries.clear();
            onChanged();
        }
    }

    /**
     * Stores a group key in the key store and saves its creation timestamp.
     *
     * @param alias The alias of the key.
     * @param key   The key.
     * @return A KeyStoreEntry for the newly stored key where alias and key are not null on success.
     * Alias set to null and ID set to the existing entry's ID if the alias is already in use.
     * Key set to null and ID set to the existing entry's ID if the key is already in use.
     */
    @Override
    public KeyStoreEntry<K> addEntry(String alias, K key) {
        synchronized (entriesLock) {
            // Check if alias is already in use
            KeyStoreEntry<K> entry = getEntry(alias);
            if (entry != null) {
                return new KeyStoreEntry<>(entry.getId(), null, entry.getKey());
            }

            // Check if key is already known
            entry = getEntry(key);
            if (entry != null) {
                return new KeyStoreEntry<>(entry.getId(), entry.getAlias(), (K) null);
            }

            // Add entry
            entry = new KeyStoreEntry<>(getFreeId(), alias, key);
            entries.put(entry.getId(), entry);
            onChanged();
            return entry;
        }
    }

    /**
     * Deletes keys from the store.
     *
     * @param ids The IDs of the keys to delete.
     */
    @Override
    public void deleteEntries(Collection<Long> ids) {
        synchronized (entriesLock) {
            entries.keySet().removeAll(ids);
            onChanged();
        }
    }

    /**
     * Changes the alias of key.
     *
     * @param id       The ID of the entry to rename.
     * @param newAlias The new alias for the key.
     * @return The specified ID on success, the ID of the existing entry if the alias is already in
     * use, or 0 if the specified ID is invalid.
     */
    @Override
    public long renameEntry(long id, String newAlias) {
        synchronized (entriesLock) {
            // Cancel if new alias is already in use
            KeyStoreEntry<K> entry = getEntry(newAlias);
            if (entry != null) {
                return entry.getId();
            }

            // Cancel if ID is unknown
            entry = getEntry(id);
            if (entry == null) {
                return 0;
            }

            // Replace the entry with a new one using the new alias
            entry = new KeyStoreEntry<>(id, newAlias, entry.getKey());
            entries.put(id, entry);
            onChanged();
            return id;
        }
    }

    /**
     * Assigns a new unmodifiable collection to unmodifiableEntries containing the current entries.
     * Gets called each time the entries change and is thread-safe.
     */
    protected void onChanged() {
        unmodifiableEntries = Collections.unmodifiableList(new ArrayList<>(entries.values()));
    }

    // Assigns a new unmodifiable collection to unmodifiableEntries containing the current entries

    /**
     * Obtains an entry from the store.
     *
     * @param id The ID of the key.
     * @return The key entry if it was found or null otherwise.
     */
    @Override
    public KeyStoreEntry<K> getEntry(long id) {
        return entries.get(id);
    }

    // Get entry by alias
    private KeyStoreEntry<K> getEntry(String alias) {
        for (KeyStoreEntry<K> entry : unmodifiableEntries) {
            if (entry.getAlias().equals(alias)) {
                return entry;
            }
        }
        return null;
    }

    // Get entry by key
    private KeyStoreEntry<K> getEntry(K key) {
        for (KeyStoreEntry<K> entry : unmodifiableEntries) {
            if (entry.getKey().equals(key)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Returns an unmodifiable copy of all entries currently in the store. Since this is only a
     * snapshot, modifications to the store after the call do not affect the returned entries.
     *
     * @return All entries currently in the store.
     */
    @Override
    public Collection<KeyStoreEntry<K>> getEntries() {
        return unmodifiableEntries;
    }

    /**
     * Use from inherited class to assign the loaded entries.
     *
     * @param entries The loaded entries.
     */
    protected void setEntries(Collection<KeyStoreEntry<K>> entries) {
        this.entries = new HashMap<>(entries.size());
        for (KeyStoreEntry<K> entry : entries) {
            this.entries.put(entry.getId(), entry);
        }

        onChanged();
    }

    /**
     * Gets called whenever an ID is needed, i.e. when a new entry is added.
     *
     * @return An unused ID.
     */
    protected abstract long getFreeId();
}
