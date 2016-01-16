package de.tudarmstadt.adtn.messagestoretests;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.MediumTest;

import java.util.Arrays;

import de.tudarmstadt.adtn.errorlogger.ErrorLoggingSingleton;
import de.tudarmstadt.adtn.messagestore.IMessageStore;
import de.tudarmstadt.adtn.messagestore.Message;
import de.tudarmstadt.adtn.messagestore.MessageStore;

public class MessageStoreTests extends AndroidTestCase {

    private IMessageStore uut;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
        log.setContext(new RenamingDelegatingContext(getContext(), "test."));
        uut = new MessageStore(new RenamingDelegatingContext(getContext(), "test."));
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

    @MediumTest
    public void testAddAndSentMessage() {
        final byte[] testMessage = new byte[]{(byte) 't', (byte) 'e', (byte) 's', (byte) 't'};

        // Add test message to message store
        uut.addMessage(testMessage);

        // Try to fetch two messages. Since only one message was added, only one message should be returned
        Message[] fetched = uut.getNextMessagesToSend(2);

        // Since only one message was added, it should be the only message returned although two were requested
        assertEquals(1, fetched.length);

        // Check if the fetched entry is the same as the one that was stored
        assertTrue(Arrays.equals(testMessage, fetched[0].getContent()));

        // Finally mark the message as sent
        uut.sentMessage(fetched[0].getID());
    }

    @MediumTest
    public void testReceivedMessage() {
        final byte[] testMessage1 = new byte[]{(byte) 'f', (byte) 'o', (byte) 'o'};
        final byte[] testMessage2 = new byte[]{(byte) 'b', (byte) 'a', (byte) 'r'};

        // Add message. receivedMessage should return false to indicate it is new.
        assertFalse(uut.receivedMessage(testMessage1));

        // Try to fetch two messages. Since only one message was added, only one message should be returned
        Message[] fetched = uut.getNextMessagesToSend(2);

        // Since only one message was added, it should be the only message returned although two were requested
        assertEquals(1, fetched.length);

        // Check if the fetched entry is the same as the one that was stored
        assertTrue(Arrays.equals(testMessage1, fetched[0].getContent()));

        // Try to add the same message again. receivedMessage should return true since it is already known
        assertTrue(uut.receivedMessage(testMessage1));

        // Add another message that is different from the first
        assertFalse(uut.receivedMessage(testMessage2));

        // getNextMessagesToSend should return the two messages now
        fetched = uut.getNextMessagesToSend(3);
        assertEquals(2, fetched.length);

        // Check if both messages were returned
        assertTrue(Arrays.equals(testMessage1, fetched[0].getContent()) && Arrays.equals(testMessage2, fetched[1].getContent()) ||
                Arrays.equals(testMessage1, fetched[1].getContent()) && Arrays.equals(testMessage2, fetched[0].getContent()));
    }

    @MediumTest
    public void testGetNextMessagesToSend() {
        final byte[] testMessage1 = new byte[]{(byte) 'f', (byte) 'o', (byte) 'o'};
        final byte[] testMessage2 = new byte[]{(byte) 'b', (byte) 'a', (byte) 'r'};

        // Add testMessage1 and fetch it to get its ID
        uut.addMessage(testMessage1);
        Message[] fetched = uut.getNextMessagesToSend(1);
        assertEquals(1, fetched.length);
        assertTrue(Arrays.equals(testMessage1, fetched[0].getContent()));
        byte[] testMessage1ID = fetched[0].getID();

        // Mark testMessage1 as sent
        uut.sentMessage(testMessage1ID);

        // Now add testMessage2
        uut.addMessage(testMessage2);

        // Since testMessage2 was sent less often than testMessage1, getNextMessagesToSend(1) should return testMessage2
        fetched = uut.getNextMessagesToSend(1);
        assertEquals(1, fetched.length);
        assertTrue(Arrays.equals(testMessage2, fetched[0].getContent()));
        byte[] testMessage2ID = fetched[0].getID();

        // The IDs of the two messages must be different
        assertFalse(Arrays.equals(testMessage1ID, testMessage2ID));

        // Now mark testMessage2 as sent twice
        uut.sentMessage(testMessage2ID);
        uut.sentMessage(testMessage2ID);

        // getNextMessagesToSent(1) should return testMessage1, since it was sent less often than testMessage2
        fetched = uut.getNextMessagesToSend(1);
        assertEquals(1, fetched.length);
        assertTrue(Arrays.equals(testMessage1, fetched[0].getContent()));
        assertTrue(Arrays.equals(testMessage1ID, fetched[0].getID()));
    }
}
