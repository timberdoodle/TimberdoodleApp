package de.tu_darmstadt.timberdoodle.friendkeystoretests;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.MediumTest;

import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.util.Collection;

import de.tu_darmstadt.adtn.generickeystore.KeyStoreEntry;
import de.tu_darmstadt.timberdoodle.friendcipher.IFriendCipher;
import de.tu_darmstadt.timberdoodle.friendkeystore.FriendKeyStore;
import de.tu_darmstadt.timberdoodle.friendkeystore.IFriendKeyStore;

public class FriendKeyStoreTests extends AndroidTestCase {

    private Context context;
    private IFriendCipher friendCipher;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        context = new RenamingDelegatingContext(getContext(), "test.");
        friendCipher = new FriendCipherMock(4);
    }

    private IFriendKeyStore loadGroupKeyStore() {
        return new FriendKeyStore(context, friendCipher);
    }

    @MediumTest
    public void testEmptyStoreGetKeys() {
        // Load store, clear and verify it is empty
        IFriendKeyStore uut = loadGroupKeyStore();
        uut.clear();
        assertTrue(uut.getEntries().isEmpty());

        // Save the empty store, load it again, and check if it is still empty
        uut.save();
        uut = loadGroupKeyStore();
        assertTrue(uut.getEntries().isEmpty());
    }

    private boolean checkKeyStoreEntriesEqual(KeyStoreEntry<PublicKey> a, KeyStoreEntry<PublicKey> b) {
        return a.getId() == b.getId() && a.getAlias().equals(b.getAlias()) && a.getKey().equals(b.getKey());
    }

    @MediumTest
    public void testStoreAndLoadKeys() throws UnrecoverableKeyException {
        final PublicKey
                testKey1 = friendCipher.byteArrayToPublicKey(new byte[]{9, 0, 0, 1}),
                testKey2 = friendCipher.byteArrayToPublicKey(new byte[]{7, 3, 3, 1});

        final String testKeyAlias1 = "Test alias 1", testKeyAlias2 = "Test alias 2";

        // Load store, clear it and put two keys in it
        IFriendKeyStore uut = loadGroupKeyStore();
        uut.clear();
        KeyStoreEntry<PublicKey> entry1 = uut.addEntry(testKeyAlias1, testKey1);
        KeyStoreEntry<PublicKey> entry2 = uut.addEntry(testKeyAlias2, testKey2);

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
        Collection<KeyStoreEntry<PublicKey>> loadedEntries = uut.getEntries();
        assertEquals(2, loadedEntries.size());

        // Check if the loaded entries are equal to the stored ones
        boolean entry1Present = false, entry2Present = false;
        for (KeyStoreEntry<PublicKey> entry : loadedEntries) {
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
}
