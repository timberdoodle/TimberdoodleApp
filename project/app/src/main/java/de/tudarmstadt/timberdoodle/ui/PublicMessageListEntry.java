package de.tudarmstadt.timberdoodle.ui;

import android.support.annotation.NonNull;

public class PublicMessageListEntry implements Comparable<PublicMessageListEntry> {

    private long id;
    private String shortMessage;
    private long timestamp;
    private String timestampString;
    private int type;

    public PublicMessageListEntry(long id, String shortMessage, long timestamp, int type) {
        this.id = id;
        this.shortMessage = shortMessage;
        this.timestamp = timestamp;
        this.type = type;
    }

    public long getID() {
        return id;
    }

    public String getShortMessage() {
        return shortMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTimestampString() {
        return timestampString;
    }

    public void setTimestampString(String timestampString) {
        this.timestampString = timestampString;
    }

    public int getType() {
        return type;
    }

    @Override
    public int compareTo(@NonNull PublicMessageListEntry another) {
        return Long.signum(timestamp - another.timestamp);
    }
}
