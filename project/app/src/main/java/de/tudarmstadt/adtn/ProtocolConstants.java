package de.tudarmstadt.adtn;

/**
 * Constants of the aDTN protocol.
 */
public class ProtocolConstants {

    public final static int MESSAGE_HEADER_SIZE = 1;
    public final static int MAX_MESSAGE_CONTENT_SIZE = 1453;
    public final static int MAX_MESSAGE_SIZE = MESSAGE_HEADER_SIZE + MAX_MESSAGE_CONTENT_SIZE;
}
