package de.tudarmstadt.adtn.packetbuilding;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import javax.crypto.SecretKey;

import de.tudarmstadt.adtn.ciphersuite.IGroupCipher;

/**
 * Converts messages to encrypted packets and vice versa.
 */
public class PacketBuilder implements IPacketBuilder {

    // Header consists only of message length
    private final static int HEADER_SIZE = 2;

    private final int maxMessageSize;
    private final int unencryptedPacketSize;
    private final Random random = new Random();
    private IGroupCipher cipher;
    private int encryptedPacketSize;

    /**
     * Creates a PacketBuilder object.
     *
     * @param maxMessageSize Maximum message size to process.
     */
    public PacketBuilder(int maxMessageSize) {
        if (maxMessageSize < 1) {
            throw new IllegalArgumentException("Invalid maximum message size specified");
        }

        this.maxMessageSize = maxMessageSize;
        unencryptedPacketSize = HEADER_SIZE + maxMessageSize;
    }

    /**
     * @return The size of an unencrypted packet.
     */
    @Override
    public int getUnencryptedPacketSize() {
        return unencryptedPacketSize;
    }

    /**
     * @return The size of an encrypted packet.
     */
    @Override
    public int getEncryptedPacketSize() {
        return encryptedPacketSize;
    }

    /**
     * Sets the cipher the packet builder should use.
     *
     * @param cipher The cipher object to use for encryption and decryption.
     */
    @Override
    public void setCipher(IGroupCipher cipher) {
        this.cipher = cipher;
        encryptedPacketSize = cipher.getCipherTextSize();
    }

    /**
     * Creates a random packet.
     *
     * @return A packet with random content.
     */
    @Override
    public byte[] createRandomPacket() {
        byte[] packet = new byte[encryptedPacketSize];
        random.nextBytes(packet);
        return packet;
    }

    /**
     * Creates a packet for a message and encrypts it with each of the specified keys.
     *
     * @param message The message to be packed.
     * @param keys    List of keys to use for encrypting the packet.
     * @return The packets created for the message.
     */
    @Override
    public byte[][] createPackets(byte[] message, Collection<SecretKey> keys) {
        if (message.length > maxMessageSize) {
            throw new IllegalArgumentException("message is too large");
        }

        byte[] packedMessage = new byte[unencryptedPacketSize];
        // Store message length
        packedMessage[0] = (byte) (message.length & 0xff);
        packedMessage[1] = (byte) (message.length >> 8 & 0xff);
        // Copy message content
        System.arraycopy(message, 0, packedMessage, HEADER_SIZE, message.length);

        return cipher.encrypt(packedMessage, keys);
    }

    /**
     * Tries to decrypt and unpack a packet.
     *
     * @param packet The encrypted packet.
     * @param keys   List of keys to use for trying to decrypt the packet.
     * @return The decrypted message on success or empty byte array otherwise.
     */
    @Override
    public byte[] tryUnpackPacket(byte[] packet, Collection<SecretKey> keys) {
        // Try to decrypt packet
        byte[] packedMessage = cipher.tryDecrypt(packet, keys);

        // Decryption failed with every key?
        if (packedMessage == null) {
            return new byte[0];
        }

        // Remove header and padding
        int length = packedMessage[0] & 0xff | packedMessage[1] << 8 & 0xff00;
        if (length < 1 || length > maxMessageSize) {
            return new byte[0]; // Ignore malformed packets
        }
        return Arrays.copyOfRange(packedMessage, HEADER_SIZE, HEADER_SIZE + length);
    }
}
