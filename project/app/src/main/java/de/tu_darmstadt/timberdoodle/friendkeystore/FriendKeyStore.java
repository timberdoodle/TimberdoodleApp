package de.tu_darmstadt.timberdoodle.friendkeystore;

import android.content.Context;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import de.tu_darmstadt.adtn.errorlogger.ErrorLoggingSingleton;
import de.tu_darmstadt.adtn.generickeystore.KeyStore;
import de.tu_darmstadt.adtn.generickeystore.KeyStoreEntry;
import de.tu_darmstadt.timberdoodle.friendcipher.IFriendCipher;

/**
 * A key store for friend keys (public keys).
 */
public class FriendKeyStore extends KeyStore<PublicKey> implements IFriendKeyStore {

    private final static Charset CHARSET = Charset.forName("UTF-8");

    private final static String FILENAME = "friend_keys";
    private final static long MAGIC_NUMBER = 0x63A7C72CFEF708BAL;
    private final static int FILE_VERSION = 1;
    private final Context context; // Context for file access
    private final IFriendCipher cipher;
    private long nextFriendKeyId = 1;

    /**
     * Creates a new public friend key store.
     *
     * @param context The context to use for file access.
     * @param cipher  The friend cipher object.
     */
    public FriendKeyStore(Context context, IFriendCipher cipher) {
        this.context = context;
        this.cipher = cipher;

        // Open key store file
        FileInputStream fileInputStream;
        try {
            fileInputStream = context.openFileInput(FILENAME);
        } catch (FileNotFoundException e) { // File does not exist? Then use empty store.
            setEntries(Collections.<KeyStoreEntry<PublicKey>>emptyList());
            return;
        }

        // Read the file
        ArrayList<KeyStoreEntry<PublicKey>> entries;
        DataInputStream reader = new DataInputStream(fileInputStream);
        try {
            // Read header: Magic number, file version and next key ID
            if (reader.readLong() != MAGIC_NUMBER) throw new IOException("Wrong magic number");
            if (reader.readInt() != FILE_VERSION) throw new IOException("Wrong file version");
            nextFriendKeyId = reader.readLong();

            // Read entries
            int numEntries = reader.readInt();
            entries = new ArrayList<>(numEntries);
            byte[] key = new byte[cipher.getEncodedPublicKeySize()];
            for (int i = 0; i < numEntries; ++i) {
                // Read ID
                long id = reader.readLong();
                // Read alias
                byte[] aliasBytes = new byte[reader.readInt()];
                reader.readFully(aliasBytes);
                String alias = new String(aliasBytes, CHARSET);
                // Read key
                reader.readFully(key);
                // Add to entries
                entries.add(new KeyStoreEntry<PublicKey>(id, alias, cipher.byteArrayToPublicKey(key)));
            }

            reader.close();
        } catch (IOException e) {
            ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
            log.storeError(ErrorLoggingSingleton.getExceptionStackTraceAsFormattedString(e));
            throw new RuntimeException(e);
        }

        setEntries(entries);
    }

    /**
     * Saves the key store entries.
     */
    @Override
    public void save() {
        // Create/overwrite key store file
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
            log.storeError(ErrorLoggingSingleton.getExceptionStackTraceAsFormattedString(e));
            throw new RuntimeException(e); // Should never happen
        }

        // Write header: Magic number, file version and next key ID
        DataOutputStream writer = new DataOutputStream(fileOutputStream);
        try {
            writer.writeLong(MAGIC_NUMBER);
            writer.writeInt(FILE_VERSION);
            writer.writeLong(nextFriendKeyId);

            // Write entries
            Collection<KeyStoreEntry<PublicKey>> entries = getEntries();
            writer.writeInt(entries.size()); // Write number of entries
            for (KeyStoreEntry<PublicKey> entry : entries) {
                // Write ID
                writer.writeLong(entry.getId());
                // Write alias
                byte[] aliasBytes = entry.getAlias().getBytes(CHARSET);
                writer.writeInt(aliasBytes.length);
                writer.write(aliasBytes);
                // Write key
                writer.write(cipher.publicKeyToByteArray(entry.getKey()));
            }

            writer.close();
        } catch (IOException e) {
            ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
            log.storeError(ErrorLoggingSingleton.getExceptionStackTraceAsFormattedString(e));
            throw new RuntimeException(e);
        }
    }

    /**
     * Assigns a new unmodifiable collection to unmodifiableEntries containing the current entries.
     * Gets called each time the entries change and is thread-safe.
     */
    @Override
    protected void onChanged() {
        super.onChanged();

        save();
    }

    /**
     * Gets called whenever an ID is needed, i.e. when a new entry is added.
     *
     * @return An unused ID.
     */
    @Override
    protected long getFreeId() {
        return nextFriendKeyId++;
    }
}
