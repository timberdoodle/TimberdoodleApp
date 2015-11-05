package de.tu_darmstadt.timberdoodle.chatlog;

import android.database.Cursor;

/**
 * Stores chat messages and provides access to them.
 */
public interface IChatLog {

    int CURSORINDEX_ID = 0, CURSORINDEX_CONTENT = 1, CURSORINDEX_TIMESTAMP = 2, CURSORINDEX_TYPE = 3, CURSORINDEX_SENDER_OR_RECEIVER = 4;

    long INVALID_MESSAGE_ID = 0;

    int MESSAGE_TYPE_SENT = 0, MESSAGE_TYPE_RECEIVED_UNREAD = 1, MESSAGE_TYPE_RECEIVED_READ = 2;

    /**
     * Recreates the database and clears existing entries.
     */
    void reset();

    /**
     * Close any open database object.
     */
    void close();

    /**
     * Puts a sent public message in the log
     *
     * @param content The message.
     * @return The ID of the newly added message.
     */
    long addSentPublicMessage(String content);

    /**
     * Puts a sent private message in the log.
     *
     * @param content  The message content.
     * @param receiver The friend ID of the receiver.
     * @return The ID of the newly added message.
     */
    long addSentPrivateMessage(String content, long receiver);

    /**
     * Puts a received public message in the log.
     *
     * @param content The message.
     * @return The ID of the newly added message.
     */
    long addReceivedPublicMessage(String content);

    /**
     * Puts a received private message in the log.
     *
     * @param content The message content.
     * @param sender  The friend ID of the sender or 0 if unknown.
     * @return The ID of the newly added message.
     */
    long addReceivedPrivateMessage(String content, long sender);

    /**
     * Deletes a public message from the log.
     *
     * @param id The ID of the message to delete.
     */
    void deletePublicMessage(long id);

    /**
     * Deletes a private message from the log.
     *
     * @param id The ID of the message to delete.
     */
    void deletePrivateMessage(long id);

    /**
     * Gets a public message entry.
     *
     * @param id The ID of the message to obtain.
     * @return An entry representing the public message or null if a message with the specified ID
     * was not found.
     */
    PublicChatLogEntry getPublicMessage(long id);

    /**
     * Gets a private message entry.
     *
     * @param id The ID of the message to obtain.
     * @return An entry representing the private message or null if a message with the specified ID
     * was not found.
     */
    PrivateChatLogEntry getPrivateMessage(long id);

    /**
     * @return A cursor containing the public messages of the log.
     */
    Cursor getPublicMessages();

    /**
     * @return A cursor containing the private messages of the log.
     */
    Cursor getPrivateMessages();

    /**
     * Marks a public message as read.
     *
     * @param id The ID of the public message to mark as read.
     */
    void setPublicMessageRead(long id);

    /**
     * Marks a private message as read.
     *
     * @param id The ID of the private message to mark as read.
     */
    void setPrivateMessageRead(long id);

    /**
     * @return The number of unread public messages.
     */
    int getNumUnreadPublicMessages();

    /**
     * @return The number of unread private messages.
     */
    int getNumUnreadPrivateMessages();
}
