package de.tudarmstadt.adtn.groupkeyshareexpirationmanager;

import org.joda.time.Instant;

/**
 * Maps key IDs to key creation timestamps and checks if they are expired for sharing.
 */
public interface IGroupKeyShareExpirationManager {

    /**
     * Persistently stores the entries, to they can be loaded again.
     */
    void store();

    /**
     * Clears all entries.
     */
    void reset();

    /**
     * Maps a key ID to the current timestamp.
     *
     * @param id The key ID to map to the current timestamp.
     * @return The timestamp used for this key.
     */
    Instant addKeyTimestamp(long id);

    /**
     * Maps the specified key ID to the specified creation timestamp if it is not already expired.
     *
     * @param id                The key ID to add a timestamp for.
     * @param creationTimestamp The creation timestamp of the key.
     */
    void addKeyTimestamp(long id, Instant creationTimestamp);

    /**
     * Gets the creation timestamp of the key with the specified ID.
     *
     * @param id The ID of the key.
     * @return The creation timestamp on success or null if the timestamp already expired.
     */
    Instant getTimestamp(long id);

    /**
     * Checks if a key with the specified creation timestamp is already expired for sharing.
     *
     * @param creationTimestamp The key creation timestamp to check.
     * @return true if the timestamp already expired or false otherwise.
     */
    boolean isExpired(Instant creationTimestamp);
}
