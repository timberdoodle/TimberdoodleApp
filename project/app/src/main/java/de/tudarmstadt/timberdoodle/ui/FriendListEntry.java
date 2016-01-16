package de.tudarmstadt.timberdoodle.ui;

import java.security.PublicKey;

import de.tudarmstadt.adtn.generickeystore.KeyStoreEntry;

/**
 * Model for the ActionButton Dialog List @ Private Message Fragment
 */
public class FriendListEntry {

    private final long id;
    private final String name;

    public FriendListEntry(KeyStoreEntry<PublicKey> pke) {
        this.id = pke.getId();
        this.name = pke.getAlias();
    }

    @Override
    public String toString() {
        return name;
    }

    public long getId() {
        return id;
    }
}
