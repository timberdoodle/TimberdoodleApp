package de.tudarmstadt.timberdoodle.friendkeystore;

import java.security.PublicKey;

import de.tudarmstadt.adtn.generickeystore.IKeyStore;

/**
 * A key store for friend keys (public keys).
 */
public interface IFriendKeyStore extends IKeyStore<PublicKey> {

    int MAX_LENGTH_FRIEND_NAME = 15;

    /**
     * Saves the key store entries.
     */
    void save();
}
