package de.tu_darmstadt.timberdoodle;

import de.tu_darmstadt.timberdoodle.chatlog.IChatLog;
import de.tu_darmstadt.timberdoodle.friendcipher.IFriendCipher;
import de.tu_darmstadt.timberdoodle.friendkeystore.IFriendKeyStore;
import de.tu_darmstadt.timberdoodle.friendkeystore.IPrivateKeyStore;
import de.tu_darmstadt.timberdoodle.messagehandler.IMessageHandler;

/**
 * The Timberdoodle service.
 */
public interface IService {

    /**
     * @return The aDTN service object.
     */
    de.tu_darmstadt.adtn.IService getAdtnService();

    /**
     * @return The FriendCipher object.
     */
    IFriendCipher getFriendCipher();

    /**
     * @return The friend key store.
     */
    IFriendKeyStore getFriendKeyStore();

    /**
     * @return The key store that stores the own public/private key pair.
     */
    IPrivateKeyStore getPrivateKeyStore();

    /**
     * @return The chat log database.
     */
    IChatLog getChatLog();

    /**
     * @return The message handler object.
     */
    IMessageHandler getMessageHandler();

    /**
     * Tries to open the private key store with the given password.
     *
     * @param password The password for the key store.
     * @return true if the password was correct or false otherwise.
     */
    boolean openPrivateKeyStore(String password);

    /**
     * Tell the service to reset the key store and use the specified password as its new password.
     * This will delete the old key store and create an empty new one. Only call this to create a
     * key store for the first time or if the user forgot the password.
     *
     * @param password The password for the new key store.
     */
    void createPrivateKeyStore(String password);

    /**
     * @return true if the key store exists or false otherwise.
     */
    boolean privateKeyStoreExists();
}

