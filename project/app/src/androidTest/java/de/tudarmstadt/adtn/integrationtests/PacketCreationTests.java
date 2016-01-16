package de.tudarmstadt.adtn.integrationtests;

import android.test.InstrumentationTestCase;
import android.util.Log;

import org.spongycastle.util.encoders.Hex;

import java.util.Random;

import de.tudarmstadt.adtn.ProtocolConstants;
import de.tudarmstadt.adtn.ciphersuite.GroupCipherSuite;
import de.tudarmstadt.adtn.ciphersuite.IGroupCipher;
import de.tudarmstadt.adtn.groupciphersuitetests.utils.CipherTestVectors;
import de.tudarmstadt.adtn.packetbuilding.IPacketBuilder;
import de.tudarmstadt.adtn.packetbuilding.PacketBuilder;
import static de.tudarmstadt.adtn.groupciphersuitetests.utils.CipherTestVectors.*;
import static de.tudarmstadt.adtn.TestUtility.*;
import de.tudarmstadt.timberdoodle.test.R;

/**
 * This class tests if the PacketBuilder and the CipherSuite together work correctly
 */
public class PacketCreationTests extends InstrumentationTestCase {

    private IPacketBuilder packetBuilder = new PacketBuilder(ProtocolConstants.MAX_MESSAGE_SIZE);
    private IGroupCipher groupCipher = new GroupCipherSuite(packetBuilder.getUnencryptedPacketSize());
    private byte[] plaintext;
    private byte[][] ciphertexts;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        packetBuilder.setCipher(groupCipher);
    }

    private void loadSetup(){
        plaintext = loadStringResourceAsUTF8EncodedByteArray(getInstrumentation(), R.string.inputTextMaxSize);
        ciphertexts = loadByteInputFromHexResources(getInstrumentation(), R.array.encryptedHex);
    }

    public void testPlaintextToPacketsIsCorrect(){
        loadSetup();
        byte[] input = new byte[plaintext.length];
        System.arraycopy(plaintext, 0, input, 0, input.length);
        byte[][] result = packetBuilder.createPackets(input, CipherTestVectors.groupKeyList);
        int i = 0;
        for (byte[] aResult : result) {
            Log.d("hex " + i, Hex.toHexString(aResult));
            i++;
            assertEquals(1488, aResult.length);
            assertFalse(bytesEqual(input, aResult, 0));
        }
    }

    public void testMany(){
        Random rand = new Random();
        byte[][] inputs = new byte[stressTestAmount][];
        for(int i = 0; i < inputs.length; i++){
            int k = rand.nextInt(ProtocolConstants.MAX_MESSAGE_SIZE);
            byte[] current = new byte[k];
            rand.nextBytes(current);
            inputs[i] = current;
            byte[][] encrypted = packetBuilder.createPackets(current, groupKeyList);
            for(int j = 0; j < encrypted.length; j++){
                byte[] decrypted = packetBuilder.tryUnpackPacket(encrypted[j], groupKeyList);
                assertEquals(current.length, decrypted.length);
                assertTrue(bytesEqual(current, decrypted, 0));
            }
        }
    }

    public void testPacketToPlaintextIsCorrect(){
        loadSetup();
        for (byte[] ciphertext : ciphertexts)
            assertTrue(bytesEqual(plaintext, packetBuilder.tryUnpackPacket(ciphertext, groupKeyList), 0));
    }

    public void testPacketMessageTooShortError(){
        byte[] input = new byte[0];
        try{
            packetBuilder.createPackets(input, groupKeyList);
        } catch (IllegalArgumentException e){
            assertTrue(true);
        }
    }

    public void testMessageTooLargeException(){
        byte[] input = new byte[Short.MAX_VALUE];
        try{
            packetBuilder.createPackets(input, groupKeyList);
        } catch (IllegalArgumentException e){
            assertTrue(true);
        }
    }

    public void testPacketNullIsReturnedIfItCantBeDecrypted(){
        byte[] input = new byte[1488];
        assertNull(packetBuilder.tryUnpackPacket(input, groupKeyList));
    }

    public void testPacketNullIsReturnedBecauseMessageLengthFieldIsOff(){
        loadSetup();
        byte[] input = new byte[plaintext.length + 3];
        System.arraycopy(plaintext, 0, input, 3, plaintext.length);
        input[0] = (byte) 0xff;
        input[1] = (byte) 0xff;
        byte[][] encrypted = groupCipher.encrypt(input, groupKeyList);
        for(byte[] aPacket: encrypted){
            assertNull(packetBuilder.tryUnpackPacket(aPacket, groupKeyList));
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
