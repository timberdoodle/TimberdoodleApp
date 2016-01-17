package de.tudarmstadt.adtn.messagestore;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores unencrypted incoming and outgoing messages. All methods are thread-safe.
 */
public class MessageStore extends SQLiteOpenHelper implements IMessageStore {

    private static final String TABLE_NAME = "messages";
    private static final String COLUMN_FINGERPRINT = "fingerprint";
    private static final String COLUMN_MESSAGE = "message";
    private static final String COLUMN_TIMES_SENT = "times_sent";
    private static final String COLUMN_TIMES_RECEIVED = "times_received";
    private static final String COLUMN_FIRST_TIME_SENT = "first_time_sent";
    private static final String COLUMN_FIRST_TIME_RECEIVED = "first_time_received";
    private static final String COLUMN_LAST_TIME_RECEIVED = "last_time_received";
    private static final String COLUMN_LAST_TIME_SENT = "last_time_sent";
    private static final String INT_NOT_NULL = " INTEGER NOT NULL DEFAULT 0, ";

    private SQLiteStatement sqlInsertMessage; // [1] fingerprint; [2] message
    private SQLiteStatement sqlInsertReceivedMessage; // [1] fingerprint; [2] message; [3] time
    private SQLiteStatement sqlUpdateReceiveStats; // [1] fingerprint; [2] time
    private SQLiteStatement sqlUpdateSendStats; // [1] fingerprint; [2] time

    private final Object md5Lock = new Object();
    private MessageDigest md5;

    /**
     * Creates the message store.
     *
     * @param context Context to use to open or create the database.
     */
    public MessageStore(Context context) {
        super(context, "network_message_store", null, 1);

        // Initialize MD5
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e); // Cannot happen since MD5 is always available
        }

        // Precompile SQLite queries

        SQLiteDatabase db = getWritableDatabase();

        sqlInsertMessage = db.compileStatement("INSERT OR IGNORE INTO " + TABLE_NAME + " " +
                "(" + COLUMN_FINGERPRINT + ", " + COLUMN_MESSAGE + ") VALUES (?1, ?2);");

        sqlInsertReceivedMessage = db.compileStatement("INSERT INTO " + TABLE_NAME + " " +
                "(" + COLUMN_FINGERPRINT + ", " + COLUMN_MESSAGE + ", " + COLUMN_TIMES_RECEIVED + ", " + COLUMN_FIRST_TIME_RECEIVED + ", " + COLUMN_LAST_TIME_RECEIVED + ") " +
                "VALUES (?1, ?2, 1, ?3, ?3);");

        sqlUpdateReceiveStats = db.compileStatement("UPDATE " + TABLE_NAME + " SET " +
                COLUMN_FIRST_TIME_RECEIVED + " = CASE WHEN " + COLUMN_TIMES_RECEIVED + " = 0 THEN ?2 ELSE " + COLUMN_FIRST_TIME_RECEIVED + " END, " +
                COLUMN_LAST_TIME_RECEIVED + " = ?2, " +
                COLUMN_TIMES_RECEIVED + " = " + COLUMN_TIMES_RECEIVED + " + 1 " +
                "WHERE " + COLUMN_FINGERPRINT + " = ?1;");

        sqlUpdateSendStats = db.compileStatement("UPDATE " + TABLE_NAME + " SET " +
                COLUMN_FIRST_TIME_SENT + " = CASE WHEN " + COLUMN_TIMES_SENT + " = 0 THEN ?2 ELSE " + COLUMN_TIMES_SENT + " END, " +
                COLUMN_LAST_TIME_SENT + " = ?2, " +
                COLUMN_TIMES_SENT + " = " + COLUMN_TIMES_SENT + " + 1 " +
                "WHERE " + COLUMN_FINGERPRINT + " = ?1;");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_FINGERPRINT + " BLOB PRIMARY KEY NOT NULL, " +
                COLUMN_MESSAGE + " BLOB NOT NULL, " +
                COLUMN_TIMES_SENT + INT_NOT_NULL +
                COLUMN_TIMES_RECEIVED + INT_NOT_NULL +
                COLUMN_FIRST_TIME_SENT + INT_NOT_NULL +
                COLUMN_LAST_TIME_SENT + INT_NOT_NULL +
                COLUMN_FIRST_TIME_RECEIVED + INT_NOT_NULL +
                COLUMN_LAST_TIME_RECEIVED + " INTEGER NOT NULL DEFAULT 0);");
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

    // Recreates the database and clears existing entries.
    private void reset(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME + ";");
        onCreate(db);
    }

    /**
     * Puts an outgoing message in the store.
     *
     * @param message The message to add.
     */
    @Override
    public void addMessage(byte[] message) {
        sqlInsertMessage.bindBlob(1, calculateFingerprint(message));
        sqlInsertMessage.bindBlob(2, message);
        sqlInsertMessage.executeInsert();
        sqlInsertMessage.clearBindings();
    }

    /**
     * Puts a received message in the store or updates its statistics if it is already contained.
     *
     * @param message The received message to add or update the statistics for.
     * @return true if the message was already in the store or false if it is new.
     */
    @Override
    public boolean receivedMessage(byte[] message) {
        byte[] fingerprint = calculateFingerprint(message);
        long time = System.currentTimeMillis();

        // Try to update stats of existing message
        sqlUpdateReceiveStats.bindBlob(1, fingerprint);
        sqlUpdateReceiveStats.bindLong(2, time);
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        boolean isNew = sqlUpdateReceiveStats.executeUpdateDelete() == 0;

        // Insert message if it does not exist yet
        if (isNew) {
            sqlInsertReceivedMessage.bindBlob(1, fingerprint);
            sqlInsertReceivedMessage.bindBlob(2, message);
            sqlInsertReceivedMessage.bindLong(3, time);
            sqlInsertReceivedMessage.executeInsert();
        }

        // End transaction and clean up
        db.setTransactionSuccessful();
        db.endTransaction();
        sqlUpdateReceiveStats.clearBindings();
        if (isNew) {
            sqlInsertReceivedMessage.clearBindings();
        }

        return !isNew;
    }

    /**
     * Informs the store that a message was just sent and updates the statistics accordingly.
     *
     * @param messageID The ID of the message whose statistics should be updated.
     */
    @Override
    public void sentMessage(byte[] messageID) {
        sqlUpdateSendStats.bindBlob(1, messageID);
        sqlUpdateSendStats.bindLong(2, System.currentTimeMillis());
        sqlUpdateSendStats.executeUpdateDelete();
        sqlUpdateSendStats.clearBindings();
    }

    /**
     * Copies the messages from the store that should be sent next. Note this will *not* alter
     * the statistics. Call {@link #receivedMessage(byte[])} if a message actually gets sent.
     *
     * @param count Maximum number of messages to obtain.
     * @return The fetched messages.
     */
    @Override
    public Message[] getNextMessagesToSend(int count) {
        // Request fingerprint + messages from database
        Cursor cursor = getReadableDatabase().rawQuery("SELECT " +
                        COLUMN_FINGERPRINT + ", " + COLUMN_MESSAGE +
                        " FROM " + TABLE_NAME + " ORDER BY " + COLUMN_TIMES_SENT + " LIMIT ?;",
                new String[]{Integer.toString(count)});

        // Copy result to array and return it
        List<Message> messages = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            messages.add(new Message(cursor.getBlob(0), cursor.getBlob(1)));
        }
        cursor.close();
        return messages.toArray(new Message[0]);  // TODO: change return type to List<Message>
    }


    // Uses the md5 object in a thread-safe way to calculate a fingerprint.
    private byte[] calculateFingerprint(byte[] input) {
        synchronized (md5Lock) {
            return md5.digest(input);
        }
    }
}
