package de.tu_darmstadt.timberdoodle.chatlog;

/**
 * A private chat log entry.
 */
public class PrivateChatLogEntry extends ChatLogEntry {

    private final long senderOrReceiver;

    /**
     * Creates a new chat log entry object for a private message.
     *
     * @param id               The ID of the message.
     * @param content          The content of the message.
     * @param timestamp        The timestamp fo the message.
     * @param type             The type of the message: Either sent, received and unread or received and read.
     * @param senderOrReceiver The friend ID of the sender of an incoming message if he is known or
     *                         the receiver's ID of an outgoing message. 0 if the sender is unknown.
     */
    PrivateChatLogEntry(long id, String content, long timestamp, int type, long senderOrReceiver) {
        super(id, content, timestamp, type);
        this.senderOrReceiver = senderOrReceiver;
    }

    /**
     * @return The friend ID of the sender of an incoming message if he is known or the receiver's
     * ID of an outgoing message. 0 if the sender is unknown.
     */
    public long getSenderOrReceiver() {
        return senderOrReceiver;
    }
}
