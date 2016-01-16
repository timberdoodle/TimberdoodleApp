package de.tudarmstadt.timberdoodle.ui;

/**
 * Model for ConversationOverviewAdapter
 */
public class ConversationMessages {
    private long timeStamp;
    private int type;
    private String content;
    private long ID;

    public ConversationMessages(int type, String content, long timeStamp, long ID) {
        this.timeStamp = timeStamp;
        this.type = type;
        this.content = content;
        this.ID = ID;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public int getType() {
        return type;
    }

    public String getContent() {
        return content;
    }


    public long getID() {
        return ID;
    }
}
