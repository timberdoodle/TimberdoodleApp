package de.tu_darmstadt.timberdoodle.friendkeystore;

import java.security.PublicKey;

import de.tu_darmstadt.adtn.generickeystore.IKeyStore;

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
