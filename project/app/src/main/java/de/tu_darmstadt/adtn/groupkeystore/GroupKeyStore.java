package de.tu_darmstadt.adtn.groupkeystore;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import de.tu_darmstadt.adtn.ciphersuite.IGroupCipher;
import de.tu_darmstadt.adtn.errorlogger.ErrorLoggingSingleton;
import de.tu_darmstadt.adtn.generickeystore.KeyStoreEntry;

/**
 * A key store for group keys (symmetric keys).
 */
public class GroupKeyStore extends de.tu_darmstadt.adtn.generickeystore.KeyStore<SecretKey> implements IGroupKeyStore {

    private final static String ID_ALIAS_SEPARATOR = "_";

    // The next group key ID is stored as pseudo key that uses this constant for alias and algorithm
    private final static String ENTRY_NEXT_GROUP_KEY_ID = "nextgroupkeyid";

    // The password to use for encryption and decryption.
    private final KeyStore.PasswordProtection protection;

    // Context for file access
    private final Context context;

    private final String filename;

    // A collection that contains only the keys.
    private volatile Collection<SecretKey> keys;

    private long nextGroupKeyId = 1;
    private final IGroupCipher cipher;

    /**
     * Loads the group key store from the key store file or creates an empty key store if the file
     * does not exist or it is specified that a new one should be created.
     *
     * @param context     The context to use for file access.
     * @param filename    The filename of the group key store.
     * @param password    The password to use for encryption and decryption.
     * @param createEmpty true to skip loading the existing key store and start with an empty one
     * @throws UnrecoverableKeyException If the specified password is wrong.
     */
    public GroupKeyStore(Context context, IGroupCipher groupCipher, String filename, String password, boolean createEmpty) throws UnrecoverableKeyException {
        if (password.isEmpty()) throw new IllegalArgumentException("password cannot be empty");

        this.context = context;
        this.cipher = groupCipher;
        this.filename = filename;
        protection = new KeyStore.PasswordProtection(password.toCharArray());

        if (createEmpty) {
            createEmptyStore();
            return;
        }

        // Open key store file
        FileInputStream fileStream;
        try {
            fileStream = context.openFileInput(filename);
        } catch (FileNotFoundException e) { // File does not exist? Then just use an empty key store
            createEmptyStore();
            return;
        }

        // Load key store and copy its entries
        ArrayList<KeyStoreEntry<SecretKey>> entries;
        try {
            KeyStore keyStore = loadKeyStore(fileStream);
            entries = new ArrayList<>(keyStore.size());
            for (Enumeration<String> aliases = keyStore.aliases(); aliases.hasMoreElements(); ) {
                String alias = aliases.nextElement();

                // Load next group key ID from pseudo key entry
                if (alias.equals(ENTRY_NEXT_GROUP_KEY_ID)) {
                    Key key = keyStore.getKey(ENTRY_NEXT_GROUP_KEY_ID, protection.getPassword());
                    nextGroupKeyId = ByteBuffer.wrap(key.getEncoded()).getLong();
                    continue;
                }

                // Load real entry
                KeyStoreEntry<SecretKey> entry = loadEntry(keyStore, alias);
                entries.add(entry);
            }
            fileStream.close();
        } catch (CertificateException | IOException | KeyStoreException | NoSuchAlgorithmException e) {
            ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
            log.storeError(ErrorLoggingSingleton.getExceptionStackTraceAsFormattedString(e));
            throw new RuntimeException(e);
        }

        setEntries(entries);
    }

    // Creates an empty group key store
    private void createEmptyStore() {
        setEntries(Collections.<KeyStoreEntry<SecretKey>>emptyList());
    }

    /* Loads and returns the entry that uses the specified alias from the specified key store using
     * the password stored in the protection attribute. */
    private KeyStoreEntry<SecretKey> loadEntry(KeyStore keyStore, String keyStoreAlias)
            throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        // Split key store alias in ID and group alias
        String[] parts = keyStoreAlias.split(ID_ALIAS_SEPARATOR, 2);
        long id = Long.parseLong(parts[0]);
        String groupAlias = parts[1];

        // Obtain key and create entry for it
        SecretKey key = (SecretKey) keyStore.getKey(keyStoreAlias, protection.getPassword());

        return new KeyStoreEntry<>(id, groupAlias, key);
    }

    /**
     * Persistently stores the entries currently in the store so they can be retrieved when the
     * store is loaded again.
     */
    @Override
    public synchronized void save() {
        try {
            // Create empty key store file
            FileOutputStream fileStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            KeyStore keyStore;
            try {
                keyStore = loadKeyStore(null);
            } catch (UnrecoverableKeyException e) {
                ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
                log.storeError(ErrorLoggingSingleton.getExceptionStackTraceAsFormattedString(e));
                throw new RuntimeException(e); // Password cannot be wrong in newly created store
            }

            // Store entries in key store file
            for (KeyStoreEntry<SecretKey> entry : getEntries()) {
                // Create key store alias by concatenating ID and group alias and put in key store file
                storeEntry(keyStore, entry.getId() + ID_ALIAS_SEPARATOR + entry.getAlias(), entry.getKey());
            }

            // Store next group key ID in pseudo key entry
            byte[] nextGroupKeyIdBytes = ByteBuffer.allocate(8).putLong(nextGroupKeyId).array();
            storeEntry(keyStore, ENTRY_NEXT_GROUP_KEY_ID, new SecretKeySpec(nextGroupKeyIdBytes, ENTRY_NEXT_GROUP_KEY_ID));

            // Save and close key store file
            keyStore.store(fileStream, protection.getPassword());
            fileStream.close();
        } catch (CertificateException | IOException | KeyStoreException | NoSuchAlgorithmException e) {
            ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
            log.storeError(ErrorLoggingSingleton.getExceptionStackTraceAsFormattedString(e));
            throw new RuntimeException(e);
        }
    }

    /* Stores the specified entry in the specified key store using the password from the protection
     * attribute. */
    private void storeEntry(KeyStore keyStore, String alias, SecretKey key) throws KeyStoreException {
        KeyStore.SecretKeyEntry keyStoreEntry = new KeyStore.SecretKeyEntry(key);
        keyStore.setEntry(alias, keyStoreEntry, protection);
    }

    /* Loads and returns a key store using the specified input stream and the password stored in the
     * protection attribute. */
    private KeyStore loadKeyStore(InputStream stream)
            throws CertificateException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try {
            keyStore.load(stream, protection.getPassword());
        } catch (IOException e) {
            throw new UnrecoverableKeyException();
        }
        return keyStore;
    }

    /**
     * Gets called whenever an ID is needed, i.e. when a new entry is added.
     *
     * @return An unused ID.
     */
    @Override
    protected long getFreeId() {
        return nextGroupKeyId++;
    }

    /**
     * Assigns a new unmodifiable collection to unmodifiableEntries containing the current entries.
     * Gets called each time the entries change and is thread-safe.
     */
    @Override
    protected void onChanged() {
        super.onChanged();

        // Iterate through all entries and copy only the keys
        Collection<KeyStoreEntry<SecretKey>> entries = getEntries();
        SecretKey[] keys = new SecretKey[entries.size()];
        int i = 0;
        for (KeyStoreEntry<SecretKey> entry : entries) {
            keys[i++] = cipher.byteArrayToSecretKey(entry.getKey().getEncoded());
        }

        this.keys = Collections.unmodifiableList(Arrays.asList(keys));

        // Key store won't change often, so just save on every change
        save();
    }

    /**
     * @return An unmodifiable snapshot of the keys currently in the store.
     */
    @Override
    public Collection<SecretKey> getKeys() {
        return keys;
    }
}
