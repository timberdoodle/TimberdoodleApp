package de.tu_darmstadt.adtn.groupciphersuitetests.apis;

import android.test.AndroidTestCase;

import java.util.List;

import javax.crypto.SecretKey;

import de.tu_darmstadt.adtn.TestUtility;
import de.tu_darmstadt.adtn.ciphersuite.hashes.Poly1305;
import de.tu_darmstadt.adtn.groupciphersuitetests.utils.CipherSuiteTestsUtility;
import de.tu_darmstadt.adtn.groupciphersuitetests.utils.CipherTestVectors;
import static de.tu_darmstadt.adtn.TestUtility.*;

/**
 * Tests poly1305
 */
public class PoyTests extends AndroidTestCase {

    private final int amount = TestUtility.stressTestAmount;
    private Poly1305 uut;
    private List<byte[]> nonces = CipherSuiteTestsUtility.generateNonceList(amount, 16);
    private List<SecretKey> keys = CipherSuiteTestsUtility.generatePolyKeys(amount);
    private byte[] plain;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        uut = new Poly1305();
        plain = CipherTestVectors.getByteInput();
    }

    /**
     * Test if the MAC is not 0
     */
    public void testPoly1305() {
        byte[] result = new byte[uut.length()];
        uut.computeMAC(nonces.get(0), keys.get(0), plain, 0, result, 0);
        assertFalse(bytesEqual(result, new byte[uut.length()], 0));
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
