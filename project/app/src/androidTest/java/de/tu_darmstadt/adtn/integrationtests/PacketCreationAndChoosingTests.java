package de.tu_darmstadt.adtn.integrationtests;

import android.test.InstrumentationTestCase;

import java.util.List;

import de.tu_darmstadt.adtn.AdtnSocketException;
import de.tu_darmstadt.adtn.GroupKeyStoreMock;
import de.tu_darmstadt.adtn.ProtocolConstants;
import de.tu_darmstadt.adtn.ciphersuite.GroupCipherSuite;
import de.tu_darmstadt.adtn.ciphersuite.IGroupCipher;
import de.tu_darmstadt.adtn.groupkeystore.IGroupKeyStore;
import de.tu_darmstadt.adtn.messagestore.MessageStore;
import de.tu_darmstadt.adtn.mocks.PreferencesMock;
import de.tu_darmstadt.adtn.mocks.SocketMock;
import de.tu_darmstadt.adtn.packetbuilding.IPacketBuilder;
import de.tu_darmstadt.adtn.packetbuilding.PacketBuilder;
import de.tu_darmstadt.adtn.preferences.IPreferences;
import de.tu_darmstadt.adtn.sendingpool.ISendingPool;
import de.tu_darmstadt.adtn.sendingpool.SendingPool;

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
            sendingPool = new SendingPool(prefs, destination, new MessageStore(getInstrumentation().getTargetContext()), packetBuilder, store, new ISendingPool.OnSendingErrorListener() {
                @Override
                public void onSendingError(AdtnSocketException e) {
                }
            });
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
