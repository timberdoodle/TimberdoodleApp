package de.tu_darmstadt.timberdoodle.messagehandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import java.security.PublicKey;
import java.util.Collection;
import java.util.HashMap;

import de.tu_darmstadt.adtn.IService;
import de.tu_darmstadt.adtn.ProtocolConstants;
import de.tu_darmstadt.adtn.generickeystore.KeyStoreEntry;
import de.tu_darmstadt.timberdoodle.chatlog.IChatLog;
import de.tu_darmstadt.timberdoodle.friendcipher.IFriendCipher;
import de.tu_darmstadt.timberdoodle.friendkeystore.IFriendKeyStore;

/**
 * Encrypts/decrypts private messages and passes messages between UI and aDTN service.
 */
public class MessageHandler implements IMessageHandler {

    private final static int MESSAGE_TYPE_CHAT = 0;

    private IService adtnService;
    private IChatLog chatLog;
    private IFriendCipher friendCipher;
    private IFriendKeyStore friendKeyStore;
    private LocalBroadcastManager broadcastManager;
    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get message header and content from the intent's extra data
            Bundle extras = intent.getExtras();
            Byte header = (Byte) extras.get(IService.INTENT_ARG_HEADER);
            assert header != null;
            byte[] message = (byte[]) extras.get(IService.INTENT_ARG_CONTENT);
            assert message != null;

            // Handle message type
            byte type = (byte) (header & 3);
            if (type == MESSAGE_TYPE_CHAT) {
                if ((header & 4) == 0) { // Public message
                    handleReceivedPublicChatMessage(message);
                } else { // Private message
                    handleReceivedPrivateChatMessage(message);
                }
            }
        }
    };

    /**
     * Creates a new MessageHandler object.
     *
     * @param context      The context to use for sending broadcasts.
     * @param adtnService  The aDTN service object to use for sending messages.
     * @param chatLog      The chat log object to store sent and received messages in.
     * @param friendCipher The friend cipher object to encrypt and decrypt private messages.
     */
    public MessageHandler(Context context, IService adtnService, IChatLog chatLog, IFriendCipher friendCipher, IFriendKeyStore friendKeyStore) {
        this.adtnService = adtnService;
        this.chatLog = chatLog;
        this.friendCipher = friendCipher;
        this.friendKeyStore = friendKeyStore;
        broadcastManager = LocalBroadcastManager.getInstance(context);
        broadcastManager.registerReceiver(messageReceiver, new IntentFilter(IService.ACTION_HANDLE_RECEIVED_MESSAGE));
    }

    /**
     * Stores a public chat message in the chat log and passes it to the aDTN service.
     *
     * @param message The message to send.
     * @return The ID of the message in the chat log.
     */
    @Override
    public long sendPublicChatMessage(String message) {
        sendChatMessage((byte) 0, encodeText(message));
        return chatLog.addSentPublicMessage(message);
    }

    /**
     * Stores a private chat message in the chat log and passes it to the aDTN service.
     *
     * @param message The message to send.
     * @param keyId   The ID of the receiver's public key
     * @param sign    True if the message should be signed with the local private key or false otherwise.
     * @return The ID of the message in the chat log.
     */
    @Override
    public long sendPrivateChatMessage(String message, long receiverKeyId, boolean sign) {
        byte[] plaintext, encodedMessage = encodeText(message);
        int encodedMessageOffset;

        if (sign) {
            // Create signature of encoded message
            byte[] signature = friendCipher.sign(encodedMessage, 0, encodedMessage.length);
            // Allocate plaintext buffer
            plaintext = new byte[1 + signature.length + encodedMessage.length];
            // Set "signature present" byte
            plaintext[0] = 1;
            // Copy signature to plaintext
            System.arraycopy(signature, 0, plaintext, 1, signature.length);
            // Copy encoded message to plaintext
            System.arraycopy(encodedMessage, 0, plaintext, 1 + signature.length, encodedMessage.length);
        } else {
            // Allocate plaintext buffer
            plaintext = new byte[1 + encodedMessage.length];
            // Copy encoded message to plaintext
            System.arraycopy(encodedMessage, 0, plaintext, 1, encodedMessage.length);
        }

        // Encrypt with intended receiver's public key
        KeyStoreEntry<PublicKey> entry = friendKeyStore.getEntry(receiverKeyId);
        if (entry == null) return 0; // Cancel if ID is invalid
        byte[] encrypted = friendCipher.encrypt(plaintext, 0, plaintext.length, entry.getKey());

        // Send and add to chat log
        sendChatMessage((byte) 1, encrypted);
        return chatLog.addSentPrivateMessage(message, receiverKeyId);
    }

    /**
     * @return The maximum number of bytes in an encoded public message.
     */
    @Override
    public int getMaxPublicChatMessageSize() {
        return ProtocolConstants.MAX_MESSAGE_CONTENT_SIZE;
    }

    /**
     * @return The maximum number of bytes in an encoded, unsigned private message.
     */
    @Override
    public int getMaxUnsignedPrivateChatMessageSize() {
        // Friend cipher plaintext size minus "signature present" byte
        return friendCipher.getMaxPlaintextSize(ProtocolConstants.MAX_MESSAGE_CONTENT_SIZE) - 1;
    }

    /**
     * @return The maximum number of bytes in an encoded, signed private message.
     */
    @Override
    public int getMaxSignedPrivateChatMessageSize() {
        // Friend cipher plaintext size minus "signature present" byte minus signature
        return getMaxUnsignedPrivateChatMessageSize() - friendCipher.getNumBytesInSignature();
    }

    @SuppressWarnings("PointlessBitwiseExpression")
    private void sendChatMessage(byte header, byte[] content) {
        sendMessage((byte) (header << 2 | MESSAGE_TYPE_CHAT), content);
    }

    private void sendMessage(byte header, byte[] content) {
        adtnService.sendMessage(header, content);
    }

    private void handleReceivedPublicChatMessage(byte[] content) {
        // Decode message
        String messageText = decodeText(content, 0);

        // Add to chat log
        long id = chatLog.addReceivedPublicMessage(messageText);

        // Send message arrival broadcast
        Intent intent = new Intent(IMessageHandler.ACTION_HANDLE_RECEIVED_PUBLIC_CHAT_MESSAGE);
        intent.putExtra(IMessageHandler.INTENT_ARG_ID, id);
        broadcastManager.sendBroadcast(intent);
    }

    private void handleReceivedPrivateChatMessage(byte[] content) {
        // Decrypt private message. Ignore if not decipherable.
        byte[] decrypted = friendCipher.tryDecrypt(content);
        if (decrypted == null) return;

        // Check if message is signed and if the sender is known
        if (decrypted.length < 1) return; // Ignore invalid messages
        long sender = 0;
        int textOffset = 1;
        if (decrypted[0] != 0) { // First byte indicates if signature follows
            int signatureLen = friendCipher.getNumBytesInSignature();
            if (content.length < 1 + signatureLen) return; // Ignore invalid messages
            sender = getSenderBySignature(
                    decrypted, 1 + signatureLen, content.length - 1 - signatureLen,
                    decrypted, 1);
            textOffset = 1 + signatureLen;
        }

        // Decode message, put in chat log and message arrival broadcast
        String messageText = decodeText(decrypted, textOffset);

        // Add to chat log
        long id = chatLog.addReceivedPrivateMessage(messageText, sender);

        // Send message arrival broadcast
        Intent intent = new Intent(IMessageHandler.ACTION_HANDLE_RECEIVED_PRIVATE_CHAT_MESSAGE);
        intent.putExtra(IMessageHandler.INTENT_ARG_ID, id);
        intent.putExtra(IMessageHandler.INTENT_ARG_SENDER, sender);
        broadcastManager.sendBroadcast(intent);
    }

    private long getSenderBySignature(byte[] data, int dataOffset, int dataCount,
                                      byte[] signature, int signatureOffset) {
        // Try to verify signature with any known public key
        Collection<KeyStoreEntry<PublicKey>> entries = friendKeyStore.getEntries();
        HashMap<PublicKey, Long> keyIdMap = new HashMap<>(entries.size());
        for (KeyStoreEntry<PublicKey> entry : entries) {
            keyIdMap.put(entry.getKey(), entry.getId());
        }
        PublicKey key = friendCipher.checkSignature(data, dataOffset, dataCount,
                signature, signatureOffset, keyIdMap.keySet());

        // Return associated key alias if verification succeeded for a key
        return key == null ? 0 : keyIdMap.get(key);
    }

    /**
     * Frees resources.
     */
    @Override
    public void close() {
        broadcastManager.unregisterReceiver(messageReceiver);
    }

    private byte[] encodeText(String text) {
        return text.getBytes(CHAT_MESSAGE_CHARSET);
    }

    private String decodeText(byte[] encoded, int offset) {
        return new String(encoded, offset, encoded.length - offset, CHAT_MESSAGE_CHARSET);
    }
}
