package de.tu_darmstadt.adtn.packetsocket;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.File;
import java.nio.ByteBuffer;

import de.tu_darmstadt.adtn.AdtnSocketException;
import de.tu_darmstadt.adtn.ISocket;
import de.tu_darmstadt.adtn.wifi.WifiPowerLock;

/**
 * Socket for sending and receiving fixed-size Ethernet packets on link layer.
 */
public class PacketSocket implements ISocket {

    // Load packet socket library
    static {
        System.loadLibrary("packetsocket");
    }

    private final static int ETHER_ADDR_LEN = 6, ETHER_TYPE_LEN = 2;
    private final static int ETHERNET_HEADER_SIZE = ETHER_ADDR_LEN * 2 + ETHER_TYPE_LEN;

    // Path in abstract namespace to use for the Unix domain socket
    private final static String UDS_PATH = "timberdoodle_socket_creator";

    private final int payloadSize, packetSize;
    private final ByteBuffer receiveBuffer, sendBuffer;

    private final int fileDescriptor;

    private final WifiPowerLock wifiPowerLock;

    /**
     * Creates a new PacketSocket object.
     *
     * @param context The context to use.
     * @param iface The name of the network interface to use.
     * @param etherType The EtherType to use for sending and receiving.
     * @param destMac The destination MAC address to use when sending.
     * @param srcMac The source MAC address to use when sending.
     * @param payloadSize The payload size of all sent and received packets.
     */
    public PacketSocket(Context context, String iface, int etherType, byte[] destMac, byte[] srcMac,
                        int payloadSize) {
        final int PAYLOAD_MIN_SIZE = 46, PAYLOAD_MAX_SIZE = 1500;
        final int IFNAMSIZ = 16;

        // Check arguments
        if (iface == null || iface.length() > IFNAMSIZ - 1) {
            throw new IllegalArgumentException("Invalid network interface name specified");
        }
        if (etherType <= 0 || etherType >= 0x10000) {
            throw new IllegalArgumentException("etherType is out of range");
        }
        if (destMac.length != ETHER_ADDR_LEN || srcMac.length != ETHER_ADDR_LEN) {
            throw new IllegalArgumentException("Wrong size of MAC address array");
        }
        if (payloadSize < PAYLOAD_MIN_SIZE || payloadSize > PAYLOAD_MAX_SIZE) {
            throw new IllegalArgumentException("payloadSize is out of range");
        }

        // Store packet size and allocate buffers
        this.payloadSize = payloadSize;
        this.packetSize = ETHERNET_HEADER_SIZE + payloadSize;
        receiveBuffer = ByteBuffer.allocateDirect(packetSize);
        sendBuffer = ByteBuffer.allocateDirect(packetSize);

        // Prepare send buffer
        sendBuffer.put(destMac).put(srcMac).putShort((short) etherType);

        try {
            // Create socket using superuser process and receive file descriptor via UDS
            fileDescriptor = createPacketSocket(context, etherType);

            // Obtain the interface index of the interface to use
            int ifaceIndex = ioctlGetInterfaceIndex(fileDescriptor, iface);

            // Bind socket to interface
            bind(fileDescriptor, etherType, ifaceIndex);

            // Disable custom FCS if supported
            final int SOL_SOCKET = 1, SO_NOFCS = 43;
            setsockoptInt(fileDescriptor, SOL_SOCKET, SO_NOFCS, 0);
        } catch (PacketSocketException e) {
            throw new AdtnSocketException("Socket creation failed", e);
        }

        // Prevent Wifi from going into power saving mode
        wifiPowerLock = new WifiPowerLock(context);
        wifiPowerLock.lockPowerSaving();
    }

    private int createPacketSocket(Context context, int etherType) {
        // Run packet socket creator process
        final int AF_PACKET = 17, SOCK_RAW = 3;
        SocketCreator socketCreator = new SocketCreator(context, UDS_PATH, AF_PACKET, SOCK_RAW, etherType);

        // Obtain socket file descriptor from socket creator process
        socketCreator.waitForListen();
        int socketFileDescriptor = readFileDescriptorFromUds(UDS_PATH);
        socketCreator.waitForExit();
        return socketFileDescriptor;
    }

    private native int readFileDescriptorFromUds(String abstractPath);

    private native int setsockoptInt(int sockfd, int level, int optname, int optval);

    private native void bind(int sockfd, int protocol, int ifindex);

    private native int ioctlGetInterfaceIndex(int fileDescriptor, String interfaceName);

    private native int close(int fileDescriptor);

    private native int recv(int fileDescriptor, ByteBuffer buffer, int offset, int count, int flags);

    private native int send(int fileDescriptor, ByteBuffer buffer, int offset, int count, int flags);

    /**
     * Receives data from the socket.
     *
     * @param buffer The buffer to put the received data in.
     * @param offset The offset in buffer.
     */
    @Override
    public void receive(byte[] buffer, int offset) {
        final int MSG_TRUNC = 0x20;

        // Receive until a packet with the correct size arrives
        while (true) {
            try {
                if (recv(fileDescriptor, receiveBuffer, 0, packetSize, MSG_TRUNC) == packetSize) {
                    break;
                }
            } catch (PacketSocketException e) {
                throw new AdtnSocketException("receive failed", e);
            }
        }

        // Copy from receive buffer
        System.arraycopy(receiveBuffer.array(), ETHERNET_HEADER_SIZE, buffer, offset, payloadSize);
    }

    /**
     * @param buffer The buffer containing the data to be sent.
     * @param offset The offset in buffer.
     */
    @Override
    public void send(byte[] buffer, int offset) {
        // Copy to send buffer
        System.arraycopy(buffer, offset, sendBuffer.array(), ETHERNET_HEADER_SIZE, payloadSize);

        // Send the packet
        try {
            send(fileDescriptor, sendBuffer, 0, packetSize, 0);
        } catch (PacketSocketException e) {
            throw new AdtnSocketException("send failed", e);
        }
    }

    /**
     * Closes the socket.
     */
    @Override
    public void close() {
        try {
            close(fileDescriptor);
        } catch (PacketSocketException e) {
            throw new AdtnSocketException("Could not close socket", e);
        } finally {
            wifiPowerLock.unlockPowerSaving();
        }
    }
}
