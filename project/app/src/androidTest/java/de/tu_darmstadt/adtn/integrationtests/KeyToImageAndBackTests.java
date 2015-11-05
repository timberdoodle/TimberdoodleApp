package de.tu_darmstadt.adtn.integrationtests;

import android.graphics.Bitmap;
import android.test.AndroidTestCase;
import android.util.Log;

import com.google.zxing.ReaderException;
import com.google.zxing.Result;

import org.joda.time.Instant;

import javax.crypto.SecretKey;

import de.tu_darmstadt.adtn.ProtocolConstants;
import de.tu_darmstadt.adtn.ciphersuite.GroupCipherSuite;
import de.tu_darmstadt.adtn.ciphersuite.IGroupCipher;
import de.tu_darmstadt.adtn.ui.groupmanager.GroupQRReaderWriter;
import de.tu_darmstadt.adtn.ui.groupmanagertests.QRReaderWriterTests;

import static de.tu_darmstadt.adtn.groupciphersuitetests.utils.CipherTestVectors.groupKeyList;
import static de.tu_darmstadt.adtn.TestUtility.*;

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
