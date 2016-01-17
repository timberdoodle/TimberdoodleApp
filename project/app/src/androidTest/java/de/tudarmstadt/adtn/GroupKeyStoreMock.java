package de.tudarmstadt.adtn;

import java.util.Collection;

import javax.crypto.SecretKey;

import de.tudarmstadt.adtn.generickeystore.KeyStoreEntry;
import de.tudarmstadt.adtn.groupkeystore.IGroupKeyStore;
import static de.tudarmstadt.adtn.groupciphersuitetests.utils.CipherTestVectors.*;

/**
 * Mocks a GroupKeyStore object
 */
public class GroupKeyStoreMock implements IGroupKeyStore{
    /**
     * Persistently stores the entries currently in the store so they can be retrieved when the
     * store is loaded again.
     */
    @Override
    public void save() {

    }

    /**
     * @return An unmodifiable snapshot of the keys currently in the store.
     */
    @Override
    public Collection<SecretKey> getSecretKeys() {
        return groupKeyList;
    }

    /**
     * Empties the store.
     */
    @Override
    public void clear() {

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
    public KeyStoreEntry<SecretKey> addEntry(String alias, SecretKey key) {
        return null;
    }

    /**
     * Deletes keys from the store.
     *
     * @param ids The IDs of the keys to delete.
     */
    @Override
    public void deleteEntries(Collection<Long> ids) {

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
        return 0;
    }

    /**
     * Obtains an entry from the store.
     *
     * @param id The ID of the key.
     * @return The key entry if it was found or null otherwise.
     */
    @Override
    public KeyStoreEntry<SecretKey> getEntry(long id) {
        return null;
    }

    /**
     * Returns an unmodifiable copy of all entries currently in the store. Since this is only a
     * snapshot, modifications to the store after the call do not affect the returned entries.
     *
     * @return All entries currently in the store.
     */
    @Override
    public Collection<KeyStoreEntry<SecretKey>> getEntries() {
        return null;
    }
}
