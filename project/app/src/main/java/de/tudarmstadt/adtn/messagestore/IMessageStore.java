package de.tudarmstadt.adtn.messagestore;

/**
 * Stores unencrypted incoming and outgoing messages. All methods are thread-safe.
 */
public interface IMessageStore {

    /**
     * Recreates the database and clears existing entries.
     */
    void reset();

    /**
     * Close any open database object.
     */
    void close();

    /**
     * Puts an outgoing message in the store.
     *
     * @param message The message to add.
     */
    void addMessage(byte[] message);

    /**
     * Puts a received message in the store or updates its statistics if it is already contained.
     *
     * @param message The received message to add or update the statistics for.
     * @return true if the message was already in the store or false if it is new.
     */
    boolean receivedMessage(byte[] message);

    /**
     * Informs the store that a message was just sent and updates the statistics accordingly.
     *
     * @param messageID The ID of the message whose statistics should be updated.
     */
    void sentMessage(byte[] messageID);

    /**
     * Copies the messages from the store that should be sent next. Note this will *not* alter
     * the statistics. Call {@link #receivedMessage(byte[])} if a message actually gets sent.
     *
     * @param count Maximum number of messages to obtain.
     * @return The fetched messages.
     */
    Message[] getNextMessagesToSend(int count);
}
