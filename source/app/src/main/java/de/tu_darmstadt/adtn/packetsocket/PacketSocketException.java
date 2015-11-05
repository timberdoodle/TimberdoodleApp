package de.tu_darmstadt.adtn.packetsocket;

/**
 * Represents an exception in the packet socket.
 */
public class PacketSocketException extends RuntimeException {

    private final int errno;

    /**
     * Creates a new PacketSocketException.
     *
     * @param detailMessage Describes the error.
     * @param errno The C errno value if present.
     */
    public PacketSocketException(String detailMessage, int errno) {
        super(detailMessage);
        this.errno = errno;
    }

    /**
     * @return The C errno value if present.
     */
    public int getErrno() {
        return errno;
    }
}
