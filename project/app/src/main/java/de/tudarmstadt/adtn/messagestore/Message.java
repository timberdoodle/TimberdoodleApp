package de.tudarmstadt.adtn.messagestore;

/**
 * Represents a message that was fetched from the message store.
 */
public class Message {

    private byte[] id;
    private byte[] content;

    /**
     * Creates a new Message object.
     *
     * @param id      The ID of the message.
     * @param content The content of the message.
     */
    public Message(byte[] id, byte[] content) {
        this.id = id;
        this.content = content;
    }

    /**
     * @return The ID of the message.
     */
    public byte[] getID() {
        return id;
    }

    /**
     * @return The content of the message.
     */
    public byte[] getContent() {
        return content;
    }
}
