package de.tudarmstadt.adtn;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import java.security.UnrecoverableKeyException;
import java.util.Arrays;

import de.tudarmstadt.adtn.ciphersuite.GroupCipherSuite;
import de.tudarmstadt.adtn.ciphersuite.IGroupCipher;
import de.tudarmstadt.adtn.errorlogger.ErrorLoggingSingleton;
import de.tudarmstadt.adtn.groupkeyshareexpirationmanager.GroupKeyShareExpirationManager;
import de.tudarmstadt.adtn.groupkeyshareexpirationmanager.IGroupKeyShareExpirationManager;
import de.tudarmstadt.adtn.groupkeystore.GroupKeyStore;
import de.tudarmstadt.adtn.groupkeystore.IGroupKeyStore;
import de.tudarmstadt.adtn.messagestore.IMessageStore;
import de.tudarmstadt.adtn.messagestore.MessageStore;
import de.tudarmstadt.adtn.packetbuilding.IPacketBuilder;
import de.tudarmstadt.adtn.packetbuilding.PacketBuilder;
import de.tudarmstadt.adtn.preferences.IAdtnPreferences;
import de.tudarmstadt.adtn.preferences.AdtnPreferences;
import de.tudarmstadt.adtn.sendingpool.ISendingPool;
import de.tudarmstadt.adtn.ui.NetworkingStatusNotification;
import de.tudarmstadt.timberdoodle.R;

/**
 * The aDTN service.
 */
public class Service extends android.app.Service implements IService {

    private static final String GROUP_KEY_STORE_FILENAME = "network_group_keys";
    private static final long GROUP_KEY_SHARE_EXPIRATION_INTERVAL = (long) 5 * 60000; // 5 minutes

    private final LocalBinder binder = new LocalBinder();
    private final Object groupKeyStoreLock = new Object();
    private final Object networkingStartStopLock = new Object();

    private IAdtnPreferences preferences;
    private IMessageStore messageStore;
    private IPacketBuilder packetBuilder;
    private ISendingPool sendingPool;
    private Thread receiveThread;
    private volatile boolean stopReceiving;
    private IGroupCipher groupCipher;
    private IGroupKeyStore groupKeyStore;
    private IGroupKeyShareExpirationManager expirationManager;
    private volatile NetworkingStatus networkingStatus;
    private NetworkingStatusNotification statusNotification;
    private LocalBroadcastManager broadcastManager;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Set context for the error logger.
        ErrorLoggingSingleton.getInstance().setContext(getApplicationContext());

        // Set up networking status
        statusNotification = new NetworkingStatusNotification(this);
        setNetworkingStatus(false, null);

        preferences = new AdtnPreferences(this);

        // Initialize group cipher, packet builder and broadcast manager
        packetBuilder = new PacketBuilder(ProtocolConstants.MAX_MESSAGE_SIZE);
        groupCipher = new GroupCipherSuite(packetBuilder.getUnencryptedPacketSize());
        packetBuilder.setCipher(groupCipher);
        broadcastManager = LocalBroadcastManager.getInstance(this);

        /* Initialize message store even without networking enabled, so messages to send will be
         * collected and get sent as soon as networking is enabled.
         */
        messageStore = new MessageStore(this);

        // Create group key share expiration manager
        expirationManager = new GroupKeyShareExpirationManager(this, GROUP_KEY_SHARE_EXPIRATION_INTERVAL);
    }

    @Override
    public void onDestroy() {
        // Do cleanup in reverse order
        stopNetworking(null, false);
        expirationManager.store();
        IGroupKeyStore keyStore = getGroupKeyStore();
        if (keyStore != null) {
            keyStore.save();
        }
        messageStore.close();

        super.onDestroy();
    }

    /**
     * Starts receiving messages and processing the sending pool.
     */
    @Override
    public void startNetworking() {
        synchronized (networkingStartStopLock) {
            // Do nothing if already started
            if (networkingStatus.getStatus() == NetworkingStatus.STATUS_ENABLED)  {
                return;
            }

            // Check if group key store is present
            if (getGroupKeyStore() == null) {
                setNetworkingStatus(false, getString(R.string.group_key_store_not_accessible));
                return;
            }

            // Keep running in background
            startService(new Intent(this, Service.class));


            // Create socket, message store and sending pool
            // TODO

            // Start receiving
            stopReceiving = false;
            receiveThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    receiveMessages();
                }
            });
            receiveThread.start();

            setNetworkingStatus(true, null);
        }
    }

    /**
     * Stops receiving messages and processing the sending pool.
     */
    @Override
    public void stopNetworking() {
        stopNetworking(null, true);
    }

    /**
     * @return The current networking status.
     */
    @Override
    public NetworkingStatus getNetworkingStatus() {
        return networkingStatus;
    }

    /**
     * Sets the current networking status info.
     *
     * @param enabled      true if the service is enabled or false if disabled. Ignored if
     *                     errorMessage is not null.
     * @param errorMessage If not null, the state is set to "error" with the specified message.
     */
    private void setNetworkingStatus(boolean enabled, String errorMessage) {
        if (errorMessage == null) {
            networkingStatus = new NetworkingStatus(enabled);
        } else {
            networkingStatus = new NetworkingStatus(errorMessage);
        }

        statusNotification.setStatus(networkingStatus);
    }

    /**
     * Stops networking.
     *
     * @param errorMessage The error message to set or null. If null, the networking state will be
     *                     set to "disabled". Otherwise it will be set to "error".
     * @param async        If true, the method will not block until networking stopped. Otherwise the
     *                     method blocks until networking is stopped.
     */
    private void stopNetworking(final String errorMessage, boolean async) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (networkingStartStopLock) {
                    if (networkingStatus.getStatus() != NetworkingStatus.STATUS_ENABLED) {
                        return;
                    }
                    sendingPool.close();
                    stopReceiving = true;
                    joinReceiveThread();

                    // TODO: Stop ad-hoc auto-connect

                    stopSelf();
                    setNetworkingStatus(false, errorMessage);
                }
            }
        };

        // Run either synchronous or asynchronous
        if (async) {
            new Thread(runnable).start();
        } else {
            runnable.run(); //NOSONAR
        }
    }

    /**
     * Puts a message in the sending pool so it will be sent when networking is available.
     *
     * @param header  The message header.
     * @param content The message content.
     */
    @Override
    public void sendMessage(byte header, byte[] content) {
        if (content.length > ProtocolConstants.MAX_MESSAGE_CONTENT_SIZE) {
            throw new IllegalArgumentException("Content size exceeds maximum allowed size");
        }

        // Merge header and content and put them in the message store
        byte[] message = new byte[ProtocolConstants.MESSAGE_HEADER_SIZE + content.length];
        message[0] = header;
        System.arraycopy(content, 0, message, 1, content.length);
        messageStore.addMessage(message);
    }

    /* Thread function to continuously receive messages, put them in the message store and send
     * a broadcast intent to inform about the message arrival */
    private void receiveMessages() {
        byte[] receiveBuffer = new byte[packetBuilder.getEncryptedPacketSize()];

        while (true) {
            // TODO: Received encrypted packet

            // Try to decrypt
            byte[] unpacked = packetBuilder.tryUnpackPacket(receiveBuffer, groupKeyStore.getSecretKeys());
            // If decryption successful and message not yet in store
            if (unpacked.length > 0 && !(messageStore.receivedMessage(unpacked))) {
                // Notify of message arrival via broadcast intent
                Intent intent = new Intent(ACTION_HANDLE_RECEIVED_MESSAGE);
                intent.putExtra(INTENT_ARG_HEADER, unpacked[0]);
                intent.putExtra(INTENT_ARG_CONTENT, Arrays.copyOfRange(unpacked, 1, unpacked.length));
                broadcastManager.sendBroadcast(intent);
            }
        }
    }

    // Blocks until the receive thread stopped
    private void joinReceiveThread() {
        boolean currentThreadWasInterrupted = false;
        while (true) {
            try {
                receiveThread.join();
                break;
            } catch (InterruptedException e) {
                currentThreadWasInterrupted = true;
            }
        }
        if (currentThreadWasInterrupted) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * @return The service preferences.
     */
    @Override
    public IAdtnPreferences getPreferences() {
        return preferences;
    }

    /**
     * @return The group cipher object.
     */
    @Override
    public IGroupCipher getGroupCipher() {
        return groupCipher;
    }

    /**
     * @return The group key store.
     */
    @Override
    public IGroupKeyStore getGroupKeyStore() {
        synchronized (groupKeyStoreLock) {
            return groupKeyStore;
        }
    }

    /**
     * @return The group key share expiration manager.
     */
    @Override
    public IGroupKeyShareExpirationManager getExpirationManager() {
        return expirationManager;
    }

    /**
     * Tries to open the key store with the given password.
     * @param password The password for the key store.
     *
     * @return true if the password was correct or false otherwise.
     */
    @Override
    public boolean openGroupKeyStore(String password) {
        try {
            synchronized (groupKeyStoreLock) {
                // Do nothing if already loaded
                if (groupKeyStore != null) {
                    return true;
                }

                groupKeyStore = new GroupKeyStore(this, groupCipher, GROUP_KEY_STORE_FILENAME, password, false);
                return true;
            }
        } catch (UnrecoverableKeyException e) {
            return false;
        }
    }

    /**
     * Tell the service to reset the key store and use the specified password as its new password.
     * This will delete the old key store and create an empty new one. Only call this to create a
     * key store for the first time or if the user forgot the password.
     *
     * @param password The password for the new key store.
     */
    @Override
    public void createGroupKeyStore(String password) {
        try {
            synchronized (groupKeyStoreLock) {
                // Do nothing if already loaded
                if (groupKeyStore != null) {
                    return;
                }

                groupKeyStore = new GroupKeyStore(this, groupCipher, GROUP_KEY_STORE_FILENAME, password, true);
            }
        } catch (UnrecoverableKeyException e) {
            ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
            log.storeError(ErrorLoggingSingleton.getExceptionStackTraceAsFormattedString(e));
            throw new RuntimeException(e);
        }
    }

    /**
     * @return true if the key store exists or false otherwise.
     */
    @Override
    public boolean groupKeyStoreExists() {
        return getGroupKeyStore() != null || getFileStreamPath(GROUP_KEY_STORE_FILENAME).exists();
    }

    /**
     * A binder to obtain the service object once the service is started.
     */
    public class LocalBinder extends Binder {
        /**
         * @return The service object.
         */
        public Service getService() {
            return Service.this;
        }
    }
}
