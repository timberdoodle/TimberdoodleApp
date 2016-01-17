package de.tudarmstadt.adtn.packetbuilding;

import java.util.Collection;

import javax.crypto.SecretKey;

import de.tudarmstadt.adtn.ciphersuite.IGroupCipher;

/**
 * Converts messages to encrypted packets and vice versa.
 */
public interface IPacketBuilder {

    /**
     * @return The size of an unencrypted packet.
     */
    int getUnencryptedPacketSize();

    /**
     * @return The size of an encrypted packet.
     */
    int getEncryptedPacketSize();

    /**
     * Sets the cipher the packet builder should use.
     *
     * @param cipher The cipher object to use for encryption and decryption.
     */
    void setCipher(IGroupCipher cipher);

    /**
     * Creates a random packet.
     *
     * @return A packet with random content.
     */
    byte[] createRandomPacket();

    /**
     * Creates a packet for a message and encrypts it with each of the specified keys.
     *
     * @param message The message to be packed.
     * @param keys    List of keys to use for encrypting the packet.
     * @return The packets created for the message.
     */
    byte[][] createPackets(byte[] message, Collection<SecretKey> keys);

    /**
     * Tries to decrypt and unpack a packet.
     *
     * @param packet The encrypted packet.
     * @param keys   List of keys to use for trying to decrypt the packet.
     * @return The decrypted message on success or empty byte array otherwise.
     */
    byte[] tryUnpackPacket(byte[] packet, Collection<SecretKey> keys);
}
