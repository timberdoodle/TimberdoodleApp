package de.tudarmstadt.adtn.integrationtests;

import android.graphics.Bitmap;
import android.test.AndroidTestCase;
import android.util.Log;

import com.google.zxing.ReaderException;
import com.google.zxing.Result;

import org.joda.time.Instant;

import javax.crypto.SecretKey;

import de.tudarmstadt.adtn.ProtocolConstants;
import de.tudarmstadt.adtn.ciphersuite.GroupCipherSuite;
import de.tudarmstadt.adtn.ciphersuite.IGroupCipher;
import de.tudarmstadt.adtn.ui.groupmanager.GroupQRReaderWriter;
import de.tudarmstadt.adtn.ui.groupmanagertests.QRReaderWriterTests;

import static de.tudarmstadt.adtn.groupciphersuitetests.utils.CipherTestVectors.groupKeyList;
import static de.tudarmstadt.adtn.TestUtility.*;

/**
 * Tests if a SecretKey can be turned to a QR-Code and read from a QR Code
 */
public class KeyToImageAndBackTests extends AndroidTestCase {

    private IGroupCipher cipher = new GroupCipherSuite(ProtocolConstants.MAX_MESSAGE_SIZE);
    private GroupQRReaderWriter qrReaderWriter = new GroupQRReaderWriter();
    private int[] keyIgnoredCounter = new int[groupKeyList.size()];

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testFlowWithSetKeyData() {
        for (int i = 0; i < groupKeyList.size(); i++) {
            Bitmap bmp = qrReaderWriter.createQrCode(cipher, groupKeyList.get(i), Instant.now(), 200, 200);

            try {
                Result decoded = QRReaderWriterTests.decodePureBitmap(bmp);
                String temporary = decoded.getText();
                GroupQRReaderWriter.ScannedGroupKey key = qrReaderWriter.parseCode(temporary, cipher);
                SecretKey result = cipher.byteArrayToSecretKey(key.getKey().getEncoded());
                assertEquals("Key #" + i + " was not recognized : ", groupKeyList.get(i), result);
            } catch (ReaderException e) {
                Log.d("KeyToImageAndBackTests", "Key #" + i + " was ignored due to a Reader Error");
                keyIgnoredCounter[i]++;
            }
        }
    }

    public void testManyTimes() {
        int amount = stressTestAmount/16;
        for (int i = 0; i < amount; i++)
            testFlowWithSetKeyData();
        for (int j = 0; j < groupKeyList.size(); j++)
            Log.d("Key #" + j + " :", "was ignored " + keyIgnoredCounter[j] + " times");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
