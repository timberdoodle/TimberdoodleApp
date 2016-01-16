package de.tudarmstadt.adtn.integrationtests;

import android.test.InstrumentationTestCase;

import java.util.List;

import de.tudarmstadt.adtn.GroupKeyStoreMock;
import de.tudarmstadt.adtn.ProtocolConstants;
import de.tudarmstadt.adtn.ciphersuite.GroupCipherSuite;
import de.tudarmstadt.adtn.ciphersuite.IGroupCipher;
import de.tudarmstadt.adtn.groupkeystore.IGroupKeyStore;
import de.tudarmstadt.adtn.messagestore.MessageStore;
import de.tudarmstadt.adtn.mocks.PreferencesMock;
import de.tudarmstadt.adtn.mocks.SocketMock;
import de.tudarmstadt.adtn.packetbuilding.IPacketBuilder;
import de.tudarmstadt.adtn.packetbuilding.PacketBuilder;
import de.tudarmstadt.adtn.preferences.IPreferences;
import de.tudarmstadt.adtn.sendingpool.ISendingPool;
import de.tudarmstadt.adtn.sendingpool.SendingPool;

/**
 * Integration tests for CipherSuite, PacketBuilder, SendingPool and MessageStore.
 */
public class PacketCreationAndChoosingTests extends InstrumentationTestCase{

    private SocketMock destination = new SocketMock();
    private ISendingPool sendingPool;

    private boolean setUpDone = false;

    private void loadSetup(){
        if(!setUpDone) {
            IPreferences prefs = new PreferencesMock();
            IGroupKeyStore store = new GroupKeyStoreMock();
            IPacketBuilder packetBuilder = new PacketBuilder(ProtocolConstants.MAX_MESSAGE_SIZE);
            IGroupCipher groupCipher = new GroupCipherSuite(packetBuilder.getUnencryptedPacketSize());
            packetBuilder.setCipher(groupCipher);
            sendingPool = new SendingPool(prefs, destination, new MessageStore(getInstrumentation().getTargetContext()), packetBuilder, store);
            setUpDone = true;
        }
    }

    public void testSending() throws Exception{
        loadSetup();
        Thread.sleep(10000);
        assertTrue(destination.isSendInvoked());
        List<byte[]> packets = destination.getPacketsToSend();
        for(byte[] packet : packets)assertEquals(1488, packet.length);
        sendingPool.close();
    }

}
