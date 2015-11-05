package de.tu_darmstadt.adtn.mocks;

import java.util.ArrayList;
import java.util.List;

import de.tu_darmstadt.adtn.ISocket;

/**
 * A Socket mock
 */
public class SocketMock implements ISocket{

    private List<byte[]> packetsToSend = new ArrayList<>();
    private boolean sendInvoked = false;
    private boolean receiveInvoked = false;
    private boolean closeInvoked = false;

    /**
     * Receives data from the socket.
     *
     * @param buffer The buffer to put the received data in.
     * @param offset The offset in buffer.
     */
    @Override
    public void receive(byte[] buffer, int offset) {
        receiveInvoked = true;
    }

    /**
     * @param buffer The buffer containing the data to be sent.
     * @param offset The offset in buffer.
     */
    @Override
    public void send(byte[] buffer, int offset) {
        sendInvoked = true;
    }

    /**
     * Closes the socket.
     */
    @Override
    public void close() {
        closeInvoked = true;
    }

    //Control methods for tests

    public boolean isSendInvoked(){
        return sendInvoked;
    }

    public boolean isReceiveInvoked(){
        return receiveInvoked;
    }

    public boolean isCloseInvoked(){
        return closeInvoked;
    }

    public List<byte[]> getPacketsToSend(){
        return packetsToSend;
    }

}
