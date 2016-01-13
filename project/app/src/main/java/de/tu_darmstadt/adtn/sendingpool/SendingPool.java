package de.tu_darmstadt.adtn.sendingpool;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

import javax.crypto.SecretKey;

import de.tu_darmstadt.adtn.ISocket;
import de.tu_darmstadt.adtn.groupkeystore.IGroupKeyStore;
import de.tu_darmstadt.adtn.messagestore.IMessageStore;
import de.tu_darmstadt.adtn.messagestore.Message;
import de.tu_darmstadt.adtn.packetbuilding.IPacketBuilder;
import de.tu_darmstadt.adtn.preferences.IPreferences;

/**
 * Wraps messages from the message store in packets and stores them.
 * The packets are then broadcasted to network in batches.
 */
public class SendingPool implements ISendingPool {

    private volatile int sendInterval;
    private volatile int refillThreshold;
    private volatile int batchSize;
    private LinkedList<SendingPoolEntry> entries = new LinkedList<>();
    private Thread thread;
    private Random random = new Random();
    private IPreferences preferences;
    private IPreferences.OnCommitListener preferencesListener = new de.tu_darmstadt.adtn.genericpreferences.Preferences.OnCommitListener() {
        @Override
        public void onCommit() {
            loadPreferences();
        }
    };
    private ISocket socket;
    private IMessageStore messageStore;
    private IPacketBuilder packetBuilder;
    private IGroupKeyStore groupKeyStore;

    /**
     * Creates the sending pool object.
     *
     * @param preferences   A preferences object to configure the sending pool.
     * @param socket        The socket to use for sending the packets.
     * @param messageStore  The message store to fetch the messages from.
     * @param packetBuilder The packet builder to create packets for a message.
     * @param groupKeyStore The key store containing the keys to encrypt the packets.
     */
    public SendingPool(IPreferences preferences, ISocket socket, IMessageStore messageStore,
                       IPacketBuilder packetBuilder, IGroupKeyStore groupKeyStore) {
        // Store references
        this.preferences = preferences;
        this.socket = socket;
        this.messageStore = messageStore;
        this.packetBuilder = packetBuilder;
        this.groupKeyStore = groupKeyStore;

        // Register preferences listener and load current preferences
        preferences.addOnCommitListenerListener(preferencesListener);
        loadPreferences();

        // Start worker thread
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        long millis = System.currentTimeMillis();
                        refill(); // Fetch messages from store
                        if (!sendBatch()) break; // Send the messages

                        // Wait between sending of two batches
                        long wait = sendInterval * 1000 - (System.currentTimeMillis() - millis);
                        if (wait > 0) {
                            Thread.sleep(wait);
                        }
                    } catch (InterruptedException e) {
                        break; // Cancel if interrupted by shutdown()
                    }
                }
            }
        });
        thread.start();
    }

    private void loadPreferences() {
        sendInterval = preferences.getSendingPoolSendInterval();
        refillThreshold = preferences.getSendingPoolRefillThreshold();
        batchSize = preferences.getSendingPoolBatchSize();
    }

    /**
     * Cancels message processing. Note that this could block if message store or network are
     * blocking.
     */
    @Override
    public void close() {
        thread.interrupt();

        // Wait until worker thread stopped. Postpone any interruptions of current thread.
        boolean currentThreadWasInterrupted = false;
        while (true) {
            try {
                thread.join();
                break;
            } catch (InterruptedException e) {
                currentThreadWasInterrupted = true;
            }
        }
        if (currentThreadWasInterrupted) Thread.currentThread().interrupt();

        // Do not leak the preferences listener object
        preferences.removeOnCommitListener(preferencesListener);
    }

    // Wraps messages from the message store in packets and adds them to the sending pool
    private void refill() {
        // No need to refill?
        if (entries.size() >= refillThreshold) return;

        // Calculate how many messages are needed to reach threshold
        Collection<SecretKey> keys = groupKeyStore.getKeys();
        int numKeys = keys.size();
        if (numKeys == 0) return; // Cannot create any packets without keys
        int numMessages = (refillThreshold - entries.size() + numKeys - 1) / numKeys;

        // Wrap messages in packets so they are ready to send and store them in pool
        for (Message message : messageStore.getNextMessagesToSend(numMessages)) {
            for (byte[] packet : packetBuilder.createPackets(message.getContent(), keys)) {
                entries.add(new SendingPoolEntry(packet, message.getID()));
            }
        }
    }

    /* Sends a batch containing packets for the messages that are currently in the pool.
     * Random packets will be interspersed until batch size is reached.
     * Returns true on success or false if sending failed. */
    private boolean sendBatch() {
        SendingPoolEntry[] batch = new SendingPoolEntry[batchSize];

        // Move as much pool entries to batch as possible
        int i;
        for (i = 0; i < batch.length && !entries.isEmpty(); ++i) {
            batch[i] = entries.remove(random.nextInt(entries.size()));
        }

        // Fill batch with random data packets if there are no more entries in pool
        if (i < batch.length) {
            while (i < batch.length) {
                batch[i] = new SendingPoolEntry(packetBuilder.createRandomPacket(), null);
                ++i;
            }

            // Shuffle batch so random data packets do not necessarily appear at the end
            Collections.shuffle(Arrays.asList(batch));
        }

        // Finally send the packets stored in batch
        for (SendingPoolEntry entry : batch) {
            // Try to send the packet


            // Update statistics for message if this is not a dummy packet
            if (entry.getMessageID() != null) messageStore.sentMessage(entry.getMessageID());
        }

        return true;
    }
}
