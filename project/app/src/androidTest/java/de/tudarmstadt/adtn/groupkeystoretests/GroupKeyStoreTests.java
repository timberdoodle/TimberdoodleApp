package de.tudarmstadt.adtn.groupkeystoretests;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.MediumTest;

import java.security.UnrecoverableKeyException;
import java.util.Collection;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import de.tudarmstadt.adtn.ciphersuite.IGroupCipher;
import de.tudarmstadt.adtn.errorlogger.ErrorLoggingSingleton;
import de.tudarmstadt.adtn.generickeystore.KeyStoreEntry;
import de.tudarmstadt.adtn.groupkeystore.GroupKeyStore;
import de.tudarmstadt.adtn.groupkeystore.IGroupKeyStore;
import de.tudarmstadt.adtn.mocks.GroupCipherMock;

public class GroupKeyStoreTests extends AndroidTestCase {

    private final static String PASSWORD = "Test Password";
    private final String KEY_ALGORITHM = "Test key algorithm";
    private final IGroupCipher helper = new GroupCipherMock();
    private final String GROUP_KEY_STORE_FILENAME = "network_group_keys";

    private Context context;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
        log.setContext(new RenamingDelegatingContext(getContext(), "test."));
        context = new RenamingDelegatingContext(getContext(), "test.");
    }

    private IGroupKeyStore createEmptyGroupKeyStore() throws UnrecoverableKeyException {
        return new GroupKeyStore(context, helper, GROUP_KEY_STORE_FILENAME, PASSWORD, true);
    }

    private IGroupKeyStore loadGroupKeyStore() throws UnrecoverableKeyException {
        return new GroupKeyStore(context, helper, GROUP_KEY_STORE_FILENAME, PASSWORD, false);
    }

    @MediumTest
    public void testNonExistingFile() throws UnrecoverableKeyException {
        // Make sure the key store file does not exist
        context.deleteFile(GROUP_KEY_STORE_FILENAME);

        // Try to load store from non-existing file (should create empty store)
        IGroupKeyStore uut = loadGroupKeyStore();
        assertTrue(uut.getEntries().isEmpty());

        // Store key and save
        final String testAlias = "Test key alias";
        final byte[] testKey = new byte[]{1, 2, 3, 4};
        KeyStoreEntry<SecretKey> entry = uut.addEntry(testAlias, new SecretKeySpec(testKey, KEY_ALGORITHM));
        uut.save();

        // Load store again and check if key is present
        Collection<KeyStoreEntry<SecretKey>> entries = uut.getEntries();
        assertEquals(1, entries.size());
        checkKeyStoreEntriesEqual(entry, entries.iterator().next());
    }

    @MediumTest
    public void testEmptyStoreGetKeys() throws UnrecoverableKeyException {
        // Create empty store and verify it is empty
        IGroupKeyStore uut = createEmptyGroupKeyStore();
        assertTrue(uut.getEntries().isEmpty());

        // Save the empty store, load it again, and check if it is still empty
        uut.save();
        uut = loadGroupKeyStore();
        assertTrue(uut.getEntries().isEmpty());
    }

    private boolean checkKeyStoreEntriesEqual(KeyStoreEntry<SecretKey> a, KeyStoreEntry<SecretKey> b) {
        return a.getId() == b.getId() && a.getAlias().equals(b.getAlias()) && a.getKey().equals(b.getKey());
    }

    @MediumTest
    public void testStoreAndLoadKeys() throws UnrecoverableKeyException {
        final SecretKey
                testKey1 = new SecretKeySpec(new byte[]{9, 0, 0, 1}, KEY_ALGORITHM),
                testKey2 = new SecretKeySpec(new byte[]{7, 3, 3, 1}, KEY_ALGORITHM);

        final String testKeyAlias1 = "Test alias 1", testKeyAlias2 = "Test alias 2";

        // Create empty store and put two keys in it
        IGroupKeyStore uut = createEmptyGroupKeyStore();
        KeyStoreEntry<SecretKey> entry1 = uut.addEntry(testKeyAlias1, testKey1);
        KeyStoreEntry<SecretKey> entry2 = uut.addEntry(testKeyAlias2, testKey2);

        // Entries must have different IDs
        assertTrue(entry1.getId() != entry2.getId());

        // Check if entry data is correct
        assertEquals(testKeyAlias1, entry1.getAlias());
        assertEquals(testKeyAlias2, entry2.getAlias());
        assertEquals(testKey1, entry1.getKey());
        assertEquals(testKey2, entry2.getKey());

        // Store and load again
        uut.save();
        uut = loadGroupKeyStore();

        // Get entries from store and check if exactly two entries are contained
        Collection<KeyStoreEntry<SecretKey>> loadedEntries = uut.getEntries();
        assertEquals(2, loadedEntries.size());

        // Check if the loaded entries are equal to the stored ones
        boolean entry1Present = false, entry2Present = false;
        for (KeyStoreEntry<SecretKey> entry : loadedEntries) {
            if (entry.getId() == entry1.getId()) {
                assertTrue(checkKeyStoreEntriesEqual(entry, entry1));
                entry1Present = true;
            } else if (entry.getId() == entry2.getId()) {
                assertTrue(checkKeyStoreEntriesEqual(entry, entry2));
                entry2Present = true;
            }
        }

        // Verify that both entries are present
        assertTrue(entry1Present);
        assertTrue(entry2Present);
    }

    @MediumTest
    public void testWrongPassword() throws Exception {
        // Create empty key store, add key and save
        IGroupKeyStore uut = null;
        uut = new GroupKeyStore(context, helper, GROUP_KEY_STORE_FILENAME, "Correct test password", true);
        uut.addEntry("Test alias", new SecretKeySpec(new byte[1], "Test key algorithm"));
        uut.save();

        // Load key store with wrong password and verify the password exception gets thrown
        boolean unrecoverableKeyExceptionThrown = false;
        try {
            uut = new GroupKeyStore(context, helper, GROUP_KEY_STORE_FILENAME, "Wrong test password", false);
        } catch (UnrecoverableKeyException e) {
            unrecoverableKeyExceptionThrown = true;
        }
        assertTrue(unrecoverableKeyExceptionThrown);
    }
}
