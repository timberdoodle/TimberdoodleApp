package de.tudarmstadt.timberdoodle.ui;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;

import de.tudarmstadt.timberdoodle.R;
import de.tudarmstadt.timberdoodle.chatlog.IChatLog;
import de.tudarmstadt.timberdoodle.chatlog.PublicChatLogEntry;

/**
 * Holds the elements of the public message list.
 */
public class PublicMessagesAdapter extends BaseAdapter {

    private final DateTimeFormatter dateOnlyFormatter = DateTimeFormat.shortDate();
    private final DateTimeFormatter timeOnlyFormatter = DateTimeFormat.shortTime();
    private final String YESTERDAY;
    private final LayoutInflater layoutInflater;
    private ArrayList<PublicMessageListEntry> entries;
    private IChatLog chatLog;

    /**
     * Creates a new PublicMessageListAdapter object.
     *
     * @param context A context for obtaining a layout infalter.
     * @param chatLog A chat log object to get the messages from.
     */
    public PublicMessagesAdapter(Context context, IChatLog chatLog) {
        super();
        YESTERDAY = context.getString(R.string.yesterday);
        this.chatLog = chatLog;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        entries = new ArrayList<>(0);
    }

    /**
     * Clears the adapter and fills it with the entries from the chat log.
     */
    public void loadFromLog() {
        // Clear current entry list and request all log entries
        entries.clear();
        Cursor cursor = chatLog.getPublicMessages();

        // Fill with entries from log
        entries.ensureCapacity(cursor.getCount());
        while (cursor.moveToNext()) {
            long id = cursor.getLong(IChatLog.CURSORINDEX_ID);
            String shortMessage = getShortMessage(cursor.getString(IChatLog.CURSORINDEX_CONTENT));
            long timestamp = cursor.getLong(IChatLog.CURSORINDEX_TIMESTAMP);
            int type = cursor.getInt(IChatLog.CURSORINDEX_TYPE);
            entries.add(new PublicMessageListEntry(id, shortMessage, timestamp, type));
        }
        notifyDataSetChanged();
    }

    /**
     * Adds a single message to the log.
     *
     * @param id The ID of the message to add.
     * @return The position of the newly loaded entry or its old position if it was already known
     */
    public int addItem(long id) {
        // Get chat log entry and convert it to message list entry
        PublicChatLogEntry logEntry = chatLog.getPublicMessage(id);
        String shortMessage = getShortMessage(logEntry.getContent());
        PublicMessageListEntry newEntry = new PublicMessageListEntry(logEntry.getID(), shortMessage, logEntry.getTimestamp(), logEntry.getType());

        // Insert list entry
        int searchResult = Collections.binarySearch(entries, newEntry);
        if (searchResult >= 0) return searchResult; // Already inserted?
        int insertAt = -searchResult - 1;
        entries.add(insertAt, newEntry);
        notifyDataSetChanged();
        return insertAt;
    }

    public void removeAtPosition(int position) {
        entries.remove(position);
        notifyDataSetChanged();
    }

    public void removeById(long id) {
        PublicMessageListEntry matchingEntry = null;
        for (PublicMessageListEntry entry : entries) {
            if (entry.getID() == id) {
                matchingEntry = entry;
                break;
            }
        }
        entries.remove(matchingEntry);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return entries.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return entries.get(position).getID();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Store references to message and timestamp TextView in the tag field of the row view
        View view = convertView;
        ViewHolder holder;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.public_screen_text, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        // Populate row with short message and timestamp string
        PublicMessageListEntry entry = entries.get(position);
        holder.getMessage().setText((entry.getType() == IChatLog.MESSAGE_TYPE_SENT ? "S: " : "R: ") + entry.getShortMessage());
        // The timestamp will be converted to a string once it is first displayed
        String timestampString = entry.getTimestampString();
        if (timestampString == null) {
            timestampString = createTimestampString(entry.getTimestamp());
            entry.setTimestampString(timestampString);
        }
        holder.getDate().setText(timestampString);

        return view;
    }

    // Returns the short form of the message that will be shown in the list
    private String getShortMessage(String wholeMessage) {
        return wholeMessage;
    }

    // Creates a timestamp string from the specified message timestamp
    private String createTimestampString(long timestamp) {
        LocalDate timestampDate = new LocalDate(timestamp);
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // Show only date if message was written before yesterday
        if (timestampDate.isBefore(yesterday)) {
            return dateOnlyFormatter.print(timestampDate);
        }

        // Show "yesterday" if message was written yesterday and is older than 24 hours
        if (timestampDate.isEqual(yesterday)) {
            LocalDateTime yesterdayDateTime = LocalDateTime.now().minusDays(1).withSecondOfMinute(0).withMillisOfSecond(0);
            LocalDateTime timestampDateTime = new LocalDateTime(timestamp).withSecondOfMinute(0).withMillisOfSecond(0);
            if (timestampDateTime.isAfter(yesterdayDateTime)) return YESTERDAY;
        }

        // Just show hour and minute if message age is less than 24 hours
        return timeOnlyFormatter.print(timestamp);
    }

    /**
     * Stores references to the message and the date TextView
     */
    private static class ViewHolder {
        private TextView message, date;

        public ViewHolder(View parentView) {
            message = (TextView) parentView.findViewById(R.id.message);
            date = (TextView) parentView.findViewById(R.id.date);
        }

        /**
         * @return The message TextView of the list entry.
         */
        public TextView getMessage() {
            return message;
        }

        /**
         * @return The date TextView of the list entry.
         */
        public TextView getDate() {
            return date;
        }
    }
}
