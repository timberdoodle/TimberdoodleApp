package de.tu_darmstadt.timberdoodle.ui;

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

import de.tu_darmstadt.adtn.generickeystore.KeyStoreEntry;
import de.tu_darmstadt.timberdoodle.R;
import de.tu_darmstadt.timberdoodle.chatlog.IChatLog;

/**
 * ConversationAdapter for the Conversation list of Conversation Fragment
 */
public class ConversationAdapter extends BaseAdapter {
    private final String YESTERDAY;
    private final Context context;
    private final DateTimeFormatter dateOnlyFormatter = DateTimeFormat.shortDate();
    private final DateTimeFormatter timeOnlyFormatter = DateTimeFormat.shortTime();
    private ArrayList<ConversationMessages> conversationMessages = new ArrayList<>();
    private Collection<KeyStoreEntry<PublicKey>> entries;

    public ConversationAdapter(Context context) {
        this.context = context;
        YESTERDAY = context.getString(R.string.yesterday);
    }

    @Override
    public int getCount() {
        return conversationMessages.size();
    }

    @Override
    public Object getItem(int position) {
        return conversationMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.conversation_row_item, parent, false);

        TextView time = (TextView) itemView.findViewById(R.id.timestampOfConversation);
        TextView message;

        if (conversationMessages.get(position).getType() == IChatLog.MESSAGE_TYPE_SENT) {
            message = (TextView) itemView.findViewById(R.id.rightContentOfConversation);
        } else {
            message = (TextView) itemView.findViewById(R.id.leftContentOfConversation);
        }

        time.setText(createTimestampString(conversationMessages.get(position).getTimeStamp()));
        message.setText(conversationMessages.get(position).getContent());

        return itemView;
    }

    /**
     * Updates and gets all relevant messages from a cursor, id, entries
     *
     * @param cursor
     * @param id
     * @param entries
     */
    public void getRelevantMessages(Cursor cursor, long id, Collection<KeyStoreEntry<PublicKey>> entries) {
        this.entries = entries;
        conversationMessages.clear();
        cursor.moveToFirst();

        // if the id is inside the keystore -> start getting only these messages
        if (containsID(id)) {
            while(!cursor.isAfterLast()) {
                long userID = cursor.getLong(IChatLog.CURSORINDEX_SENDER_OR_RECEIVER);
                if (userID != id) {
                    cursor.moveToNext();
                    continue;
                }
                long msgID = cursor.getLong(IChatLog.CURSORINDEX_ID);
                int type = cursor.getInt(IChatLog.CURSORINDEX_TYPE);
                String content = cursor.getString(IChatLog.CURSORINDEX_CONTENT);
                long timestamp = cursor.getLong(IChatLog.CURSORINDEX_TIMESTAMP);
                conversationMessages.add(new ConversationMessages(type, content, timestamp, msgID));
                cursor.moveToNext();
            }
            // if the id is not inside -> get all messages which don't have any sender
        } else {
            while(!cursor.isAfterLast()) {
                long userID = cursor.getLong(IChatLog.CURSORINDEX_SENDER_OR_RECEIVER);
                if (containsID(userID)) {
                    cursor.moveToNext();
                    continue;
                }

                long msgID = cursor.getLong(IChatLog.CURSORINDEX_ID);
                int type = cursor.getInt(IChatLog.CURSORINDEX_TYPE);
                String content = cursor.getString(IChatLog.CURSORINDEX_CONTENT);
                long timestamp = cursor.getLong(IChatLog.CURSORINDEX_TIMESTAMP);
                conversationMessages.add(new ConversationMessages(type, content, timestamp, msgID));
                cursor.moveToNext();
            }

        }
        Collections.sort(conversationMessages, new Comparator<ConversationMessages>() {
            @Override
            public int compare(ConversationMessages lhs, ConversationMessages rhs) {
                return (int) (lhs.getTimeStamp() - rhs.getTimeStamp());
            }
        });

        Collections.reverse(conversationMessages);
        notifyDataSetChanged();
    }

    /**
     * Checks if for a given id if the Collections contains it or not
     *
     * @param id id to check
     * @return true or false
     */
    private boolean containsID(long id) {
        for (KeyStoreEntry<PublicKey> pke : entries) {
            if (pke.getId() == id) return true;
        }
        return false;
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
