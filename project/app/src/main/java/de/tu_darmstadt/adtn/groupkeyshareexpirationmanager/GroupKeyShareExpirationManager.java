package de.tu_darmstadt.adtn.groupkeyshareexpirationmanager;

import android.content.Context;
import android.content.SharedPreferences;

import org.joda.time.Instant;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps key IDs to key creation timestamps and checks if they are expired for sharing.
 */
public class GroupKeyShareExpirationManager implements IGroupKeyShareExpirationManager {

    // The file the load from and store the group creation timestamps
    private final static String FILENAME = "groupkeyshareexpiration.preferences";

    // The expiration interval after creation of a group key in milliseconds
    private final long expirationInterval;

    // Stores the group ID (decimal string) as key and creation time (long) as value for each entry
    private final SharedPreferences prefs;

    // Contains <group ID, creation timestamp> entries
    private final HashMap<Long, Instant> entries;

    /**
     * Creates a new GroupKeyShareExpirationManager object.
     *
     * @param context            The context to use for accessing the preference file.
     * @param expirationInterval The interval (milliseconds) for a key to expire after creation.
     */
    public GroupKeyShareExpirationManager(Context context, long expirationInterval) {
        this.expirationInterval = expirationInterval;

        // Load creation timestamps from preference file
        prefs = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        Map<String, ?> allPrefs = prefs.getAll();
        entries = new HashMap<>(allPrefs.size());
        for (Map.Entry<String, ?> prefEntry : allPrefs.entrySet()) {
            // Check if stored value is a Long (should always apply, but check anyway)
            Object value = prefEntry.getValue();
            if (!(value instanceof Long)) continue;
            // Add to entries
            addKeyTimestamp(Long.parseLong(prefEntry.getKey()), new Instant((long) value));
        }
    }

    /**
     * Persistently stores the entries, to they can be loaded again.
     */
    @Override
    public void store() {
        // Clear contents of SharedPreferences instance
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();

        // Transfer entries to SharedPreferences instance
        for (Map.Entry<Long, Instant> entry : entries.entrySet()) {
            // Only store entries that are not expired yet
            if (!isExpired(entry.getValue())) {
                editor.putLong(Long.toString(entry.getKey()), entry.getValue().getMillis());
            }
        }

        editor.apply();
    }

    /**
     * Clears all entries.
     */
    @Override
    public void reset() {
        entries.clear();
    }

    /**
     * Maps a key ID to the current timestamp.
     *
     * @param id The key ID to map to the current timestamp.
     * @return The timestamp used for this key.
     */
    @Override
    public Instant addKeyTimestamp(long id) {
        Instant creationTimestamp = Instant.now();
        addKeyTimestamp(id, creationTimestamp);
        return creationTimestamp;
    }

    /**
     * Maps the specified key ID to the specified creation timestamp if it is not already expired.
     *
     * @param id                The key ID to add a timestamp for.
     * @param creationTimestamp The creation timestamp of the key.
     */
    @Override
    public void addKeyTimestamp(long id, Instant creationTimestamp) {
        // Store only if not expired
        if (!isExpired(creationTimestamp)) {
            entries.put(id, creationTimestamp);
            store();
        }
    }

    /**
     * Gets the creation timestamp of the key with the specified ID.
     *
     * @param id The ID of the key.
     * @return The creation timestamp on success or null if the timestamp already expired.
     */
    @Override
    public Instant getTimestamp(long id) {
        Instant creationTimestamp = entries.get(id);
        // Only return creation timestamp if not expired. Otherwise return null.
        return creationTimestamp == null || isExpired(creationTimestamp) ? null : creationTimestamp;
    }

    /**
     * Checks if a key with the specified creation timestamp is already expired for sharing.
     *
     * @param creationTimestamp The key creation timestamp to check.
     * @return true if the timestamp already expired or false otherwise.
     */
    @Override
    public boolean isExpired(Instant creationTimestamp) {
        return creationTimestamp.plus(expirationInterval).isBeforeNow();
    }
}
