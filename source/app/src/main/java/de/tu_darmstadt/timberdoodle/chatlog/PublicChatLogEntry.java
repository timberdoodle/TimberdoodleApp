package de.tu_darmstadt.timberdoodle.chatlog;

/**
 * A public chat log entry.
 */
public class PublicChatLogEntry extends ChatLogEntry {

    /**
     * Creates a new chat log entry object for a public message.
     *
     * @param id        The ID of the message.
     * @param content   The content of the message.
     * @param timestamp The timestamp fo the message.
     * @param type      The type of the message: Either sent, received and unread or received and read.
     */
    PublicChatLogEntry(long id, String content, long timestamp, int type) {
        super(id, content, timestamp, type);
    }
}
