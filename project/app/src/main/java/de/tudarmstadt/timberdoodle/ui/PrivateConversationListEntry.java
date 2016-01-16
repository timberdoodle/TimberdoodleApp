package de.tudarmstadt.timberdoodle.ui;

public class PrivateConversationListEntry {

    private final String content;
    private final long timestamp;
    private final String sender;
    private final long id;

    public PrivateConversationListEntry(String content, long timestamp, String sender, long id) {
        this.content = content;
        this.timestamp = timestamp;
        this.sender = sender;
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }


    public long getId() {
        return id;
    }

    /**
     * Is used for privateMessageFragment to sort out duplicates
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof PrivateConversationListEntry
                && sender.equals(((PrivateConversationListEntry) other).getSender())) {
            return true;
        }
        return false;
    }

}
