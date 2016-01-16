package de.tudarmstadt.timberdoodle.chatlogtests;

import android.database.Cursor;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.MediumTest;

import de.tudarmstadt.timberdoodle.chatlog.ChatLog;
import de.tudarmstadt.timberdoodle.chatlog.ChatLogEntry;
import de.tudarmstadt.timberdoodle.chatlog.IChatLog;
import de.tudarmstadt.timberdoodle.chatlog.PrivateChatLogEntry;

public class ChatLogTests extends AndroidTestCase {

    private IChatLog uut;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        resetAndCloseDb();
        uut = new ChatLog(new RenamingDelegatingContext(getContext(), "test."));
        uut.reset();
    }

    @Override
    protected void tearDown() throws Exception {
        resetAndCloseDb();

        super.tearDown();
    }

    private void resetAndCloseDb() {
        if (uut == null) return;

        uut.reset();
        uut.close();
        uut = null;
    }

    // Compares the entry's content and type and checks if its timestamp lies between the specified minimum and maximum value
    private void compareMessageEntry(ChatLogEntry entry, String content, long timestampMin, long timestampMax, int type) {
        assertEquals(content, entry.getContent());
        assertTrue(timestampMin <= entry.getTimestamp() && entry.getTimestamp() <= timestampMax);
        assertEquals(type, entry.getType());
    }

    // Same comparison check as for public messages, but also checks "sender or receiver" value
    private void compareMessageEntry(PrivateChatLogEntry entry, String content, long timestampMin, long timestampMax, int type, long senderOrReceiver) {
        compareMessageEntry(entry, content, timestampMin, timestampMax, type);
        assertEquals(senderOrReceiver, entry.getSenderOrReceiver());
    }

    @MediumTest
    public void testAddSentPublicMessage() {
        final String TEST_CONTENT = "A public test message sent by the local user";

        // Add message to db and save timestamp before and after call
        long timeBeforeCall = System.currentTimeMillis();
        long id = uut.addSentPublicMessage(TEST_CONTENT);
        long timeAfterCall = System.currentTimeMillis();

        // Fetch from db and compare
        compareMessageEntry(uut.getPublicMessage(id), TEST_CONTENT, timeBeforeCall, timeAfterCall, IChatLog.MESSAGE_TYPE_SENT);
    }

    @MediumTest
    public void testAddSentPrivateMessage() {
        final String TEST_CONTENT = "A private test message sent by the local user";
        final long TEST_RECEIVER = 123;

        // Add message to db and save timestamp before and after call
        long timeBeforeCall = System.currentTimeMillis();
        long id = uut.addSentPrivateMessage(TEST_CONTENT, TEST_RECEIVER);
        long timeAfterCall = System.currentTimeMillis();

        // Fetch from db and compare
        compareMessageEntry(uut.getPrivateMessage(id), TEST_CONTENT, timeBeforeCall, timeAfterCall, IChatLog.MESSAGE_TYPE_SENT, TEST_RECEIVER);
    }

    @MediumTest
    public void testAddReceivedPublicMessageAndMarkRead() {
        final String TEST_CONTENT = "A public test message sent by a remote user";

        // Add message to db and save timestamp before and after call
        long timeBeforeCall = System.currentTimeMillis();
        long id = uut.addReceivedPublicMessage(TEST_CONTENT);
        long timeAfterCall = System.currentTimeMillis();

        // Fetch from db and compare
        compareMessageEntry(uut.getPublicMessage(id), TEST_CONTENT, timeBeforeCall, timeAfterCall, IChatLog.MESSAGE_TYPE_RECEIVED_UNREAD);

        // Mark message as read and fetch to verify
        uut.setPublicMessageRead(id);
        assertEquals(IChatLog.MESSAGE_TYPE_RECEIVED_READ, uut.getPublicMessage(id).getType());
    }

    private void testAddReceivedPrivateMessageAndMarkRead(long sender) {
        final String TEST_CONTENT = "A private test message sent by a remote user";

        // Add message to db and save timestamp before and after call
        long timeBeforeCall = System.currentTimeMillis();
        long id = uut.addReceivedPrivateMessage(TEST_CONTENT, sender);
        long timeAfterCall = System.currentTimeMillis();

        // Fetch from db and compare
        compareMessageEntry(uut.getPrivateMessage(id), TEST_CONTENT, timeBeforeCall, timeAfterCall, IChatLog.MESSAGE_TYPE_RECEIVED_UNREAD, sender);

        // Mark message as read and fetch to verify
        uut.setPrivateMessageRead(id);
        assertEquals(IChatLog.MESSAGE_TYPE_RECEIVED_READ, uut.getPrivateMessage(id).getType());
    }

    @MediumTest
    public void testAddReceivedPrivateMessageFromKnownSenderAndMarkRead() {
        testAddReceivedPrivateMessageAndMarkRead(456);
    }

    @MediumTest
    public void testAddReceivedPrivateMessageFromUnknownSenderAndMarkRead() {
        testAddReceivedPrivateMessageAndMarkRead(0);
    }

    @MediumTest
    public void testDeletePublicMessage() {
        // Add message and verify that it is present in db
        long id = uut.addSentPublicMessage("Test message");
        assertNotNull(uut.getPublicMessage(id));

        // Delete message and verify that it is gone
        uut.deletePublicMessage(id);
        assertNull(uut.getPublicMessage(id));
    }

    @MediumTest
    public void testDeletePrivateMessage() {
        // Add message and verify that it is present in db
        long id = uut.addSentPrivateMessage("Test message", 891);
        assertNotNull(uut.getPrivateMessage(id));

        // Delete message and verify that it is gone
        uut.deletePrivateMessage(id);
        assertNull(uut.getPrivateMessage(id));
    }

    @MediumTest
    public void testGetMessages() {
        final int NUM_MESSAGES = 100;
        final String TEST_PUBLIC_CONTENT = "Test public content ";
        final String TEST_PRIVATE_CONTENT = "Test private content ";
        final long TEST_RECEIVER_START_ID = 1;

        long[] publicIDs = new long[NUM_MESSAGES], privateIDs = new long[NUM_MESSAGES];

        // Add public and private test messages
        for (int i = 0; i < NUM_MESSAGES; ++i) {
            publicIDs[i] = uut.addSentPublicMessage(TEST_PUBLIC_CONTENT + i);
            privateIDs[i] = uut.addSentPrivateMessage(TEST_PRIVATE_CONTENT + i, TEST_RECEIVER_START_ID + i);
        }

        // Verify that the messages are returned in reverse order (from latest to oldest)

        // Public messages
        Cursor publicMessagesCursor = uut.getPublicMessages();
        for (int i = NUM_MESSAGES - 1; i >= 0; --i) {
            assertTrue(publicMessagesCursor.moveToNext());

            // Compare ID and content of public messages
            assertEquals(publicIDs[i], publicMessagesCursor.getLong(IChatLog.CURSORINDEX_ID));
            assertEquals(TEST_PUBLIC_CONTENT + i, publicMessagesCursor.getString(IChatLog.CURSORINDEX_CONTENT));
        }
        publicMessagesCursor.close();

        // Private messages
        Cursor privateMessagesCursor = uut.getPrivateMessages();
        for (int i = NUM_MESSAGES - 1; i >= 0; --i) {
            assertTrue(privateMessagesCursor.moveToNext());

            // Compare ID, content and receiver of private messages
            assertEquals(privateIDs[i], privateMessagesCursor.getLong(IChatLog.CURSORINDEX_ID));
            assertEquals(TEST_PRIVATE_CONTENT + i, privateMessagesCursor.getString(IChatLog.CURSORINDEX_CONTENT));
            assertEquals(TEST_RECEIVER_START_ID + i, privateMessagesCursor.getLong(IChatLog.CURSORINDEX_SENDER_OR_RECEIVER));
        }
        privateMessagesCursor.close();
    }
}
