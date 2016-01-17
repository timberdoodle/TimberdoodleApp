package de.tudarmstadt.adtn;

/**
 * Constants of the aDTN protocol.
 */
public class ProtocolConstants {

    public static final int MESSAGE_HEADER_SIZE = 1;
    public static final int MAX_MESSAGE_CONTENT_SIZE = 1453;
    public static final int MAX_MESSAGE_SIZE = MESSAGE_HEADER_SIZE + MAX_MESSAGE_CONTENT_SIZE;

    private ProtocolConstants() {}

}