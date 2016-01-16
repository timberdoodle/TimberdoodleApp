package de.tudarmstadt.adtn.packetsockettests;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.LargeTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import de.tudarmstadt.adtn.packetsocket.PacketSocket;

public class PacketSocketTests extends AndroidTestCase {

    /* This test requires packetsocket_test to be running on another device. Manually enable the
     * test if it is running. */
    private final boolean ENABLE_TEST = false;

    private final byte[] dstMac = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    private final byte[] srcMac = new byte[]{(byte) 0x00, (byte) 0x41, (byte) 0xAC, (byte) 0xC2, (byte) 0x96, (byte) 0xC6};
    private final int ETHER_TYPE = 0xD948;
    private final int PACKET_SIZE = 1500;

    private volatile boolean allPacketsReceived;

    @LargeTest
    public void test() throws Exception {
        if (!ENABLE_TEST) return;

        // Create packets with random contents
        Random rnd = new Random();
        final byte[][] packets = new byte[100][];
        for (int i = 0; i < packets.length; ++i) {
            packets[i] = new byte[PACKET_SIZE];
            rnd.nextBytes(packets[i]);
        }

        // Create packet socket
        final PacketSocket ps = new PacketSocket(getContext(), "wlan0", ETHER_TYPE, dstMac, srcMac, PACKET_SIZE);

        // Run receive thread
        Thread receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                checkReceive(ps, packets);
            }
        });
        receiveThread.start();

        // Keep sending packets until responses for all packets arrived
        while (!allPacketsReceived) {
            Thread.sleep(20);
            for (byte[] packet : packets) {
                ps.send(packet, 0);
            }
        }

        receiveThread.join();
        ps.close();
    }

    private void checkReceive(PacketSocket ps, byte[][] packets) {
        // Make copy so received packets can be replaced with null
        ArrayList<byte[]> remainingPackets = new ArrayList<>(Arrays.asList(packets));

        byte[] receiveBuffer = new byte[PACKET_SIZE];
        while (!allPacketsReceived) {
            ps.receive(receiveBuffer, 0);
            byte[] foundPacket = null;
            for (byte[] remainingPacket : remainingPackets) {
                foundPacket = remainingPacket;
                for (int i = 0; i < PACKET_SIZE; ++i) {
                    if (receiveBuffer[i] != (byte) (remainingPacket[i] + 1)) {
                        foundPacket = null;
                        break;
                    }
                }
                if (foundPacket != null) break;
            }
            if (foundPacket != null) {
                remainingPackets.remove(foundPacket);
                if (remainingPackets.isEmpty()) {
                    allPacketsReceived = true;
                    break;
                }
            }
        }
    }
}
