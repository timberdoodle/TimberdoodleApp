package de.tudarmstadt.timberdoodle.chatlog;

/**
 * A chat log entry
 */
public abstract class ChatLogEntry {

    private final long id;
    private final String content;
    private final long timestamp;
    private final int type;

    /**
     * Creates a new chat log entry object.
     *
     * @param id        The ID of the message.
     * @param content   The content of the message.
     * @param timestamp The timestamp fo the message.
     * @param type      The type of the message: Either sent, received and unread or received and read.
     */
    ChatLogEntry(long id, String content, long timestamp, int type) {
        this.id = id;
        this.content = content;
        this.timestamp = timestamp;
        this.type = type;
    }

    /**
     * @return The ID of the message.
     */
    public long getID() {
        return id;
    }

    /**
     * @return The content of the message.
     */
    public String getContent() {
        return content;
    }

    /**
     * @return The timestamp fo the message.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @return The type of the message: Either sent, received and unread or received and read.
     */
    public int getType() {
        return type;
    }
}