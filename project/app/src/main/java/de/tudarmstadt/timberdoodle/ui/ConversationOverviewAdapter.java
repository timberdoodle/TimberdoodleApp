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

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import de.tudarmstadt.adtn.generickeystore.KeyStoreEntry;
import de.tudarmstadt.timberdoodle.R;
import de.tudarmstadt.timberdoodle.chatlog.IChatLog;

public class ConversationOverviewAdapter extends BaseAdapter {
    private final String YESTERDAY;
    private final DateTimeFormatter dateOnlyFormatter = DateTimeFormat.shortDate();
    private final DateTimeFormatter timeOnlyFormatter = DateTimeFormat.shortTime();
    private Context context;
    private Collection<KeyStoreEntry<PublicKey>> nameEntries;
    private ArrayList<PrivateConversationListEntry> privateMessageEntries = new ArrayList<>();

    public ConversationOverviewAdapter(Context context) {
        this.context = context;
        YESTERDAY = context.getString(R.string.yesterday);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.private_row_item, parent, false);

        TextView contact = (TextView) itemView.findViewById(R.id.conversationContact);
        TextView time = (TextView) itemView.findViewById(R.id.messageTime);
        TextView message = (TextView) itemView.findViewById(R.id.conversationMessage);

        contact.setText(privateMessageEntries.get(position).getSender());
        time.setText(createTimestampString(privateMessageEntries.get(position).getTimestamp()));
        message.setText(privateMessageEntries.get(position).getContent());

        return itemView;
    }

    @Override
    public int getCount() {
        return privateMessageEntries.size();
    }

    @Override
    public Object getItem(int position) {
        return privateMessageEntries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Converts a cursor from a chatlog to List entries
     *
     * @param privateMessages
     * @param entries
     */
    public void convertChatlogToEntries(Cursor privateMessages, Collection<KeyStoreEntry<PublicKey>> entries) {
        this.nameEntries = entries;
        privateMessageEntries.clear();
        privateMessages.moveToFirst();

        // Takes all received messaged from the cursor and converts them into PrivateConversationListEntries
        ArrayList<PrivateConversationListEntry> chatLogEntries = new ArrayList<>();
        while (!privateMessages.isAfterLast()) {
            int type = privateMessages.getInt(IChatLog.CURSORINDEX_TYPE);
//            if (type != IChatLog.MESSAGE_TYPE_RECEIVED_READ && type != IChatLog.MESSAGE_TYPE_RECEIVED_UNREAD) {
//                privateMessages.moveToNext();
//                continue;
//            }

            String content = privateMessages.getString(IChatLog.CURSORINDEX_CONTENT);
            long timestamp = privateMessages.getLong(IChatLog.CURSORINDEX_TIMESTAMP);
            long userID = privateMessages.getLong(IChatLog.CURSORINDEX_SENDER_OR_RECEIVER);
            String sender = nameConverter(userID);
            chatLogEntries.add(new PrivateConversationListEntry(content, timestamp, sender, userID));
            privateMessages.moveToNext();
        }


        int position;
        PrivateConversationListEntry entry;

        // Filters out all duplicate names & helds only onto the newest message
        for (PrivateConversationListEntry p : chatLogEntries) {
            if ((position = privateMessageEntries.indexOf(p)) >= 0) {

                entry = privateMessageEntries.get(position);
                if (p.getTimestamp() < entry.getTimestamp())
                    continue;

                privateMessageEntries.remove(position);

            }

            privateMessageEntries.add(p);
        }

        // Sorts them from newest to oldest
        Collections.sort(privateMessageEntries, new Comparator<PrivateConversationListEntry>() {
            @Override
            public int compare(PrivateConversationListEntry lhs, PrivateConversationListEntry rhs) {
                return (int) (lhs.getTimestamp() - rhs.getTimestamp());
            }
        });

        // Notifies the Adapter that the array changed
        notifyDataSetChanged();
    }

    /**
     * Converts an id to names
     *
     * @param id sender id
     * @return sender name
     */
    private String nameConverter(long id) {
        for (KeyStoreEntry<PublicKey> entry : nameEntries) {
            if (entry.getId() == id)
                return entry.getAlias();
        }
        return context.getString(R.string.anonymous);
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
}

