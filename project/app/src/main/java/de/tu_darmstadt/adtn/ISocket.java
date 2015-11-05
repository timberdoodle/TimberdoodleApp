package de.tu_darmstadt.adtn;

/**
 * A socket for sending and receiving data.
 */
public interface ISocket {

    /**
     * Receives data from the socket.
     *
     * @param buffer The buffer to put the received data in.
     * @param offset The offset in buffer.
     */
    void receive(byte[] buffer, int offset);

    /**
     * @param buffer The buffer containing the data to be sent.
     * @param offset The offset in buffer.
     */
    void send(byte[] buffer, int offset);

    /**
     * Closes the socket.
     */
    void close();
}
