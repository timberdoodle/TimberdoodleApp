package de.tu_darmstadt.adtn;

import de.tu_darmstadt.adtn.ciphersuite.IGroupCipher;
import de.tu_darmstadt.adtn.groupkeyshareexpirationmanager.IGroupKeyShareExpirationManager;
import de.tu_darmstadt.adtn.groupkeystore.IGroupKeyStore;
import de.tu_darmstadt.adtn.preferences.IPreferences;

/**
 * The aDTN service.
 */
public interface IService {

    String ACTION_HANDLE_RECEIVED_MESSAGE = "de.tu_darmstadt.adtn.IService.ACTION_HANDLE_RECEIVED_MESSAGE";
    String INTENT_ARG_HEADER = "header";
    String INTENT_ARG_CONTENT = "content";

    /**
     * Starts receiving messages and processing the sending pool.
     */
    void startNetworking();

    /**
     * Stops receiving messages and processing the sending pool.
     */
    void stopNetworking();

    /**
     * @return The current networking status.
     */
    NetworkingStatus getNetworkingStatus();

    /**
     * Puts a message in the sending pool so it will be sent when networking is available.
     *
     * @param header  The message header.
     * @param content The message content.
     */
    void sendMessage(byte header, byte[] content);

    /**
     * @return The service preferences.
     */
    IPreferences getPreferences();

    /**
     * @return The group cipher object.
     */
    IGroupCipher getGroupCipher();

    /**
     * @return The group key store.
     */
    IGroupKeyStore getGroupKeyStore();

    /**
     * @return The group key share expiration manager.
     */
    IGroupKeyShareExpirationManager getExpirationManager();

    /**
     * Tries to open the key store with the given password.
     * @param password The password for the key store.
     *
     * @return true if the password was correct or false otherwise.
     */
    boolean openGroupKeyStore(String password);

    /**
     * Tell the service to reset the key store and use the specified password as its new password.
     * This will delete the old key store and create an empty new one. Only call this to create a
     * key store for the first time or if the user forgot the password.
     *
     * @param password The password for the new key store.
     */
    void createGroupKeyStore(String password);

    /**
     * @return true if the key store exists or false otherwise.
     */
    boolean groupKeyStoreExists();
}
