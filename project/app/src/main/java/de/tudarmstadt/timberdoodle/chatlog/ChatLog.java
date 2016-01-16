package de.tudarmstadt.timberdoodle.chatlog;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

/**
 * Stores chat messages and provides access to them.
 */
public class ChatLog extends SQLiteOpenHelper implements IChatLog {

    private final static String TABLE_PUBLIC = "public_chat_messages";
    private final static String TABLE_PRIVATE = "private_chat_messages";
    private final static String COLUMN_ID = "id";
    private final static String COLUMN_CONTENT = "content";
    private final static String COLUMN_TIMESTAMP = "timestamp";
    private final static String COLUMN_TYPE = "type";
    private final static String COLUMN_SENDER_OR_RECEIVER = "sender_or_receiver";

    private SQLiteStatement sqlInsertSentPublicMessage, sqlInsertReceivedPublicMessage,
            sqlInsertSentPrivateMessage, sqlInsertReceivedPrivateMessage,
            sqlDeletePublicMessage, sqlDeletePrivateMessage,
            sqlMarkPublicMessageRead, sqlMarkPrivateMessageRead,
            sqlCountUnreadPublicMessages, sqlCountUnreadPrivateMessages;

    private String sqlGetPublicMessage, sqlGetPrivateMessage,
            sqlGetPublicMessages, sqlGetPrivateMessages;

    /**
     * Opens or creates a chat log database.
     *
     * @param context Context to use to open or create the database.
     */
    public ChatLog(Context context) {
        super(context, "chatlog", null, 2);

        SQLiteDatabase db = getWritableDatabase();

        String sqlInsertMessage = "INSERT INTO %s (" +
                COLUMN_CONTENT + ", " + COLUMN_TIMESTAMP + ", " + COLUMN_TYPE + "%s" +
                ") VALUES (?, ?, %%d%s);";

        // Precompile SQL statements for inserting public messages
        String sqlInsertPublicMessage = String.format(sqlInsertMessage, TABLE_PUBLIC, "", "");
        sqlInsertSentPublicMessage = db.compileStatement(String.format(sqlInsertPublicMessage, MESSAGE_TYPE_SENT));
        sqlInsertReceivedPublicMessage = db.compileStatement(String.format(sqlInsertPublicMessage, MESSAGE_TYPE_RECEIVED_UNREAD));

        // Precompile SQL statements for inserting private messages
        String sqlInsertPrivateMessage = String.format(sqlInsertMessage, TABLE_PRIVATE, ", " + COLUMN_SENDER_OR_RECEIVER, ", ?");
        sqlInsertSentPrivateMessage = db.compileStatement(String.format(sqlInsertPrivateMessage, MESSAGE_TYPE_SENT));
        sqlInsertReceivedPrivateMessage = db.compileStatement(String.format(sqlInsertPrivateMessage, MESSAGE_TYPE_RECEIVED_UNREAD));

        // Precompile SQL statements for deleting messages
        String sqlDeleteMessage = "DELETE FROM %s WHERE " + COLUMN_ID + " = ?;";
        sqlDeletePublicMessage = db.compileStatement(String.format(sqlDeleteMessage, TABLE_PUBLIC));
        sqlDeletePrivateMessage = db.compileStatement(String.format(sqlDeleteMessage, TABLE_PRIVATE));

        // Precompile SQL statements for marking received message as read
        String sqlMarkRead = "UPDATE %s SET " + COLUMN_TYPE + "=" + MESSAGE_TYPE_RECEIVED_READ +
                " WHERE " + COLUMN_ID + "=" + "?;";
        sqlMarkPublicMessageRead = db.compileStatement(String.format(sqlMarkRead, TABLE_PUBLIC));
        sqlMarkPrivateMessageRead = db.compileStatement(String.format(sqlMarkRead, TABLE_PRIVATE));

        // Prepare SQL statements for fetching single messages
        String sqlGetMessage = "SELECT " + COLUMN_ID + ", " + COLUMN_CONTENT + ", " + COLUMN_TIMESTAMP + ", " + COLUMN_TYPE + "%s" +
                " FROM %s";
        String sqlGetSingleMessage = sqlGetMessage + " WHERE " + COLUMN_ID + " = ?;";
        sqlGetPublicMessage = String.format(sqlGetSingleMessage, "", TABLE_PUBLIC);
        sqlGetPrivateMessage = String.format(sqlGetSingleMessage, ", " + COLUMN_SENDER_OR_RECEIVER, TABLE_PRIVATE);

        // Prepare SQL statements for fetching all messages
        String sqlGetAllMessages = sqlGetMessage + " ORDER BY " + COLUMN_TIMESTAMP + " DESC, " + COLUMN_ID + " DESC;";
        sqlGetPublicMessages = String.format(sqlGetAllMessages, "", TABLE_PUBLIC);
        sqlGetPrivateMessages = String.format(sqlGetAllMessages, ", " + COLUMN_SENDER_OR_RECEIVER, TABLE_PRIVATE);

        // Prepare SQL statement for counting unread public messages
        String sqlCountUnreadMessages = "SELECT COUNT (*) FROM %s WHERE " +
                COLUMN_TYPE + "=" + MESSAGE_TYPE_RECEIVED_UNREAD + ";";
        sqlCountUnreadPublicMessages = db.compileStatement(String.format(sqlCountUnreadMessages, TABLE_PUBLIC));
        sqlCountUnreadPrivateMessages = db.compileStatement(String.format(sqlCountUnreadMessages, TABLE_PRIVATE));
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlCreateTable = "CREATE TABLE %s (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CONTENT + " STRING NOT NULL, " +
                COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +
                COLUMN_TYPE + " INTEGER NOT NULL%s);";
        db.execSQL(String.format(sqlCreateTable, TABLE_PUBLIC, ""));
        db.execSQL(String.format(sqlCreateTable, TABLE_PRIVATE, ", " + COLUMN_SENDER_OR_RECEIVER + " INTEGER NOT NULL"));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        reset(db);
    }

    /**
     * Recreates the database and clears existing entries.
     */
    @Override
    public void reset() {
        reset(getWritableDatabase());
    }

    private void reset(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PUBLIC + ";");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRIVATE + ";");
        onCreate(db);
    }

    /**
     * Puts a sent public message in the log
     *
     * @param content The message.
     * @return The ID of the newly added message.
     */
    @Override
    public long addSentPublicMessage(String content) {
        return insertMessage(sqlInsertSentPublicMessage, content, false, 0);
    }

    /**
     * Puts a sent private message in the log.
     *
     * @param content  The message content.
     * @param receiver The friend ID of the receiver.
     * @return The ID of the newly added message.
     */
    @Override
    public long addSentPrivateMessage(String content, long receiver) {
        return insertMessage(sqlInsertSentPrivateMessage, content, true, receiver);
    }

    /**
     * Puts a received public message in the log.
     *
     * @param content The message.
     * @return The ID of the newly added message.
     */
    @Override
    public long addReceivedPublicMessage(String content) {
        return insertMessage(sqlInsertReceivedPublicMessage, content, false, 0);
    }

    /**
     * Puts a received private message in the log.
     *
     * @param content The message content.
     * @param sender  The friend ID of the sender or 0 if unknown.
     * @return The ID of the newly added message.
     */
    @Override
    public long addReceivedPrivateMessage(String content, long sender) {
        return insertMessage(sqlInsertReceivedPrivateMessage, content, true, sender);
    }

    // Inserts a sent or received message using the specified SQLÖ statement.
    private long insertMessage(SQLiteStatement statement, String content, boolean isPrivate, long senderOrReceiver) {
        // Bind content
        statement.bindString(1, content);
        // Bind timestamp
        statement.bindLong(2, System.currentTimeMillis());
        // Bind sender or receiver
        if (isPrivate) statement.bindLong(3, senderOrReceiver);

        long id = statement.executeInsert();
        statement.clearBindings();
        return id;
    }

    /**
     * Deletes a public message from the log.
     *
     * @param id The ID of the message to delete.
     */
    @Override
    public void deletePublicMessage(long id) {
        deleteMessage(sqlDeletePublicMessage, id);
    }

    /**
     * Deletes a private message from the log.
     *
     * @param id The ID of the message to delete.
     */
    @Override
    public void deletePrivateMessage(long id) {
        deleteMessage(sqlDeletePrivateMessage, id);
    }

    private void deleteMessage(SQLiteStatement statement, long id) {
        statement.bindLong(1, id);
        statement.executeUpdateDelete();
        statement.clearBindings();
    }

    /**
     * Gets a public message entry.
     *
     * @param id The ID of the message to obtain.
     * @return An entry representing the public message or null if a message with the specified ID
     * was not found.
     */
    @Override
    public PublicChatLogEntry getPublicMessage(long id) {
        Cursor cursor = getReadableDatabase().rawQuery(sqlGetPublicMessage, new String[]{Long.toString(id)});

        PublicChatLogEntry entry = null;
        if (cursor.moveToFirst()) {
            entry = new PublicChatLogEntry(cursor.getLong(0), cursor.getString(1), cursor.getLong(2), cursor.getInt(3));
        }
        cursor.close();

        return entry;
    }

    /**
     * Gets a private message entry.
     *
     * @param id The ID of the message to obtain.
     * @return An entry representing the private message or null if a message with the specified ID
     * was not found.
     */
    @Override
    public PrivateChatLogEntry getPrivateMessage(long id) {
        Cursor cursor = getReadableDatabase().rawQuery(sqlGetPrivateMessage, new String[]{Long.toString(id)});

        PrivateChatLogEntry entry = null;
        if (cursor.moveToFirst()) {
            entry = new PrivateChatLogEntry(cursor.getLong(0), cursor.getString(1), cursor.getLong(2), cursor.getInt(3), cursor.getLong(4));
        }
        cursor.close();

        return entry;
    }

    /**
     * @return A cursor containing the public messages of the log.
     */
    @Override
    public Cursor getPublicMessages() {
        return getReadableDatabase().rawQuery(sqlGetPublicMessages, null);
    }

    /**
     * @return A cursor containing the private messages of the log.
     */
    @Override
    public Cursor getPrivateMessages() {
        return getReadableDatabase().rawQuery(sqlGetPrivateMessages, null);
    }

    /**
     * Marks a public message as read.
     *
     * @param id The ID of the public message to mark as read.
     */
    @Override
    public void setPublicMessageRead(long id) {
        sqlMarkPublicMessageRead.bindLong(1, id);
        sqlMarkPublicMessageRead.executeUpdateDelete();
        sqlMarkPublicMessageRead.clearBindings();
    }

    /**
     * Marks a private message as read.
     *
     * @param id The ID of the private message to mark as read.
     */
    @Override
    public void setPrivateMessageRead(long id) {
        sqlMarkPrivateMessageRead.bindLong(1, id);
        sqlMarkPrivateMessageRead.executeUpdateDelete();
        sqlMarkPrivateMessageRead.clearBindings();
    }

    /**
     * @return The number of unread public messages.
     */
    @Override
    public int getNumUnreadPublicMessages() {
        return (int) sqlCountUnreadPublicMessages.simpleQueryForLong();
    }

    /**
     * @return The number of unread private messages.
     */
    @Override
    public int getNumUnreadPrivateMessages() {
        return (int) sqlCountUnreadPrivateMessages.simpleQueryForLong();
    }
}