package de.tu_darmstadt.timberdoodle.messagehandler;

import java.nio.charset.Charset;

/**
 * Encrypts/decrypts private messages and passes messages between UI and aDTN service.
 */
public interface IMessageHandler {

    String ACTION_HANDLE_RECEIVED_PUBLIC_CHAT_MESSAGE =
            "de.tu_darmstadt.timberdoodle.messagehandler.IMessageHandler.ACTION_HANDLE_RECEIVED_PUBLIC_CHAT_MESSAGE";
    String ACTION_HANDLE_RECEIVED_PRIVATE_CHAT_MESSAGE =
            "de.tu_darmstadt.timberdoodle.messagehandler.IMessageHandler.ACTION_HANDLE_RECEIVED_PRIVATE_CHAT_MESSAGE";

    String INTENT_ARG_ID = "id";
    String INTENT_ARG_SENDER = "sender";

    // Chat message handling

    /**
     * The charset for encoding chat messages.
     */
    Charset CHAT_MESSAGE_CHARSET = Charset.forName("UTF-8");

    /**
     * Stores a public chat message in the chat log and passes it to the aDTN service.
     *
     * @param message The message to send.
     * @return The ID of the message in the chat log.
     */
    long sendPublicChatMessage(String message);

    /**
     * Stores a private chat message in the chat log and passes it to the aDTN service.
     *
     * @param message The message to send.
     * @param keyId   The ID of the receiver's public key
     * @param sign    True if the message should be signed with the local private key or false otherwise.
     * @return The ID of the message in the chat log.
     */
    long sendPrivateChatMessage(String message, long keyId, boolean sign);

    /**
     * @return The maximum number of bytes in an encoded public message.
     */
    int getMaxPublicChatMessageSize();

    /**
     * @return The maximum number of bytes in an encoded, unsigned private message.
     */
    int getMaxUnsignedPrivateChatMessageSize();

    /**
     * @return The maximum number of bytes in an encoded, signed private message.
     */
    int getMaxSignedPrivateChatMessageSize();

    /**
     * Frees resources.
     */
    void close();
}
