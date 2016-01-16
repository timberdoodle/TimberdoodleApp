package de.tudarmstadt.timberdoodle;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

import java.security.KeyPair;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;

import de.tudarmstadt.adtn.errorlogger.ErrorLoggingSingleton;
import de.tudarmstadt.timberdoodle.chatlog.ChatLog;
import de.tudarmstadt.timberdoodle.chatlog.IChatLog;
import de.tudarmstadt.timberdoodle.friendcipher.FriendCipher;
import de.tudarmstadt.timberdoodle.friendcipher.IFriendCipher;
import de.tudarmstadt.timberdoodle.friendkeystore.FriendKeyStore;
import de.tudarmstadt.timberdoodle.friendkeystore.IFriendKeyStore;
import de.tudarmstadt.timberdoodle.friendkeystore.IPrivateKeyStore;
import de.tudarmstadt.timberdoodle.friendkeystore.PrivateKeyStore;
import de.tudarmstadt.timberdoodle.messagehandler.IMessageHandler;
import de.tudarmstadt.timberdoodle.messagehandler.MessageHandler;
import de.tudarmstadt.timberdoodle.ui.MessageArrivalNotification;

/**
 * The Timberdoodle service.
 */
public class Service extends android.app.Service implements IService {

    //region Service binding

    /**
     * Listener interface implemented by TimberdoodleServiceConnection. Callback gets called when
     * the service is ready, i.e. all its components are ready to use.
     */
    interface OnServiceReadyListener {
        /**
         * Gets called when the service is ready.
         */
        void onTimberdoodleServiceReady();
    }

    /**
     * A binder to obtain the service object once the service is started.
     */
    class LocalBinder extends Binder {
        /**
         * @return The service object.
         */
        public Service getService() {
            return Service.this;
        }
    }

    // The binder that gets returned in onBind()
    private final LocalBinder binder = new LocalBinder();

    /* Contains listeners waiting for the service to get ready to start or is set to null if the
     * service is already ready. */
    private ArrayList<OnServiceReadyListener> onServiceReadyListeners = new ArrayList<>();

    /**
     * Called by TimberdoodleServiceConnection to register its listener.
     *
     * @param listener The listener to register.
     */
    void addOnServiceReadyListener(OnServiceReadyListener listener) {
        if (onServiceReadyListeners == null) { // Do callback immediately if service is already ready
            listener.onTimberdoodleServiceReady();
        } else { // Store listener so it gets called when the service is ready
            onServiceReadyListeners.add(listener);
        }
    }

    // Notifies listeners that the service is now ready to use
    private void notifyOnServiceReadyListeners() {
        for (OnServiceReadyListener listener : onServiceReadyListeners) {
            listener.onTimberdoodleServiceReady();
        }
        // The listener array is set to null to indicate that the service is ready
        onServiceReadyListeners = null;
    }

    // Service connection for the aDTN service
    private final ServiceConnection adtnServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // Set up reference to aDTN service and create message handler
            adtnService = ((de.tudarmstadt.adtn.Service.LocalBinder) service).getService();
            messageHandler = new MessageHandler(Service.this, adtnService, chatLog, friendCipher, friendKeyStore);
            // Now that the aDTN service is running, the Timberdoodle service is ready
            notifyOnServiceReadyListeners();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            throw new RuntimeException("Timberdoodle service disconnected unexpectedly");
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // Keep running in background
        startService(new Intent(this, Service.class));
        return binder;
    }

    //endregion

    private de.tudarmstadt.adtn.IService adtnService;

    // Encryption
    private IFriendCipher friendCipher;
    private IFriendKeyStore friendKeyStore;

    // Private key store
    private final String PRIVATE_KEY_STORE_FILENAME = "own_key_pair_store";
    private final Object privateKeyStoreLock = new Object();
    private IPrivateKeyStore privateKeyStore;

    private IChatLog chatLog;
    private IMessageHandler messageHandler;
    private MessageArrivalNotification messageArrivalNotification;

    @Override
    public void onCreate() {
        super.onCreate();

        // Bind to aDTN service
        if (!bindService(new Intent(this, de.tudarmstadt.adtn.Service.class), adtnServiceConnection, BIND_AUTO_CREATE)) {
            throw new RuntimeException("Could not bind to aDTN service");
        }

        // Create friend cipher, friend key store and key store for own public/private key pair
        friendCipher = new FriendCipher();
        friendKeyStore = new FriendKeyStore(this, friendCipher);

        // Create chat log and message arrival notification
        chatLog = new ChatLog(this);
        messageArrivalNotification = new MessageArrivalNotification(this, chatLog);
    }

    @Override
    public void onDestroy() {
        messageHandler.close();
        chatLog.close();
        if (getPrivateKeyStore() != null) privateKeyStore.save();
        friendKeyStore.save();

        // Unbind from aDTN service
        unbindService(adtnServiceConnection);

        super.onDestroy();
    }

    /**
     * @return The aDTN service object.
     */
    @Override
    public de.tudarmstadt.adtn.IService getAdtnService() {
        return adtnService;
    }

    /**
     * @return The FriendCipher object.
     */
    @Override
    public IFriendCipher getFriendCipher() {
        return friendCipher;
    }

    /**
     * @return The friend key store.
     */
    @Override
    public IFriendKeyStore getFriendKeyStore() {
        return friendKeyStore;
    }

    /**
     * @return The key store that stores the own public/private key pair.
     */
    @Override
    public IPrivateKeyStore getPrivateKeyStore() {
        synchronized (privateKeyStoreLock) {
            return privateKeyStore;
        }
    }

    /**
     * @return The chat log database.
     */
    @Override
    public IChatLog getChatLog() {
        return chatLog;
    }

    /**
     * @return The message handler object.
     */
    @Override
    public IMessageHandler getMessageHandler() {
        return messageHandler;
    }

    /**
     * Tell the service to reset the key store and use password as it's
     * new password. Warning, this will delete the old key store and create
     * a completely new one. Only call this, to create a key store for the
     * first time or if the user has forgotten his password.
     *
     * @param password password for the new key store
     */
    @Override
    public void createPrivateKeyStore(String password) {
        try {
            synchronized (privateKeyStoreLock) {
                // Do nothing if already loaded
                if (privateKeyStore != null) return;

                privateKeyStore = new PrivateKeyStore(this, PRIVATE_KEY_STORE_FILENAME, password, true);
            }
        } catch (UnrecoverableKeyException e) {
            ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
            log.storeError(ErrorLoggingSingleton.getExceptionStackTraceAsFormattedString(e));
            throw new RuntimeException(e); // Cannot happen when when an empty store is used
        }
    }

    /**
     * Tell the service to open the key store with the given password and to load the key pair if
     * present.
     *
     * @param password The password for the key store
     * @return Returns true if the password was correct, false if not.
     */
    @Override
    public boolean openPrivateKeyStore(String password) {
        try {
            synchronized (privateKeyStoreLock) {
                // Do nothing if already loaded
                if (privateKeyStore != null) return true;

                privateKeyStore = new PrivateKeyStore(this, PRIVATE_KEY_STORE_FILENAME, password, false);

                // Configure friend cipher with private key
                KeyPair keyPair = privateKeyStore.getKeyPair();
                if (keyPair != null) friendCipher.setPrivateKey(keyPair.getPrivate());
            }
        } catch (UnrecoverableKeyException e) {
            return false;
        }

        return true;
    }

    /**
     * Check if keystore exists
     *
     * @return returns true if it exists, false if not.
     */
    @Override
    public boolean privateKeyStoreExists() {
        return getPrivateKeyStore() != null || getFileStreamPath(PRIVATE_KEY_STORE_FILENAME).exists();
    }
}
