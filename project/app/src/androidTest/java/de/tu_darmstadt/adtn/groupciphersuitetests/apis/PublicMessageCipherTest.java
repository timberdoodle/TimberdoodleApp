package de.tu_darmstadt.adtn.groupciphersuitetests.apis;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import javax.crypto.Cipher;

import de.tu_darmstadt.adtn.ciphersuite.ciphers.IPublicMessageCipher;
import de.tu_darmstadt.adtn.errorlogger.ErrorLoggingSingleton;
import de.tu_darmstadt.adtn.groupciphersuitetests.utils.CipherChaCha20Poly1305Vectors;
import de.tu_darmstadt.adtn.groupciphersuitetests.utils.CipherSuiteTestsUtility;
import de.tu_darmstadt.adtn.groupciphersuitetests.utils.FaultyVectors;

import static de.tu_darmstadt.adtn.groupciphersuitetests.utils.CipherTestVectors.getByteInput;
import static de.tu_darmstadt.adtn.groupciphersuitetests.utils.CipherTestVectors.keyList;
import static de.tu_darmstadt.adtn.groupciphersuitetests.utils.CipherTestVectors.nonces;
import static de.tu_darmstadt.adtn.groupciphersuitetests.utils.CipherTestVectors.testInput;
import static de.tu_darmstadt.adtn.TestUtility.*;


/**
 * PublicMessageEncryption Tests
 */
public class PublicMessageCipherTest extends AndroidTestCase {

    private byte[] nonce;

    public PublicMessageCipherTest() throws Exception {
        super();
        ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
        log.setContext(new RenamingDelegatingContext(getContext(), "test."));
        nonce = new byte[CipherSuiteTestsUtility.ivLengthCipher];
        System.arraycopy(nonces[0], 0, nonce, 0, CipherSuiteTestsUtility.ivLengthCipher);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Test if uut can encrypt a plain text
     *
     * @throws Exception
     */
    @SmallTest
    public void testEncrypt() throws Exception {
        int encrypt = Cipher.DECRYPT_MODE;
        IPublicMessageCipher pmc = CipherSuiteTestsUtility.setUpPublicMessageCipher(encrypt);
        byte[] result = new byte[CipherSuiteTestsUtility.PLAINSIZE];
        pmc.doFinalOptimized(nonce, keyList[0], getByteInput(), 0, result, 0);
        assertTrue(bytesEqual(result, CipherChaCha20Poly1305Vectors.getChacha20Poly1305Output()[0], CipherSuiteTestsUtility.textOffset));
    }

    /**
     * Test if uut can decrypt a cipher text
     *
     * @throws Exception
     */
    @SmallTest
    public void testDecrypt() throws Exception {
        int decrypt = Cipher.ENCRYPT_MODE;
        IPublicMessageCipher pmc = CipherSuiteTestsUtility.setUpPublicMessageCipher(decrypt);
        byte[] result = new byte[CipherSuiteTestsUtility.PLAINSIZE];
        pmc.doFinalOptimized(nonce, keyList[0], CipherChaCha20Poly1305Vectors.getChacha20Poly1305Output()[0], CipherSuiteTestsUtility.textOffset, result, 0);
        String comp = new String(result);
        assertEquals(testInput, comp);
    }

    @SmallTest
    public void testFaulty() throws Exception {
        int encrypt = Cipher.ENCRYPT_MODE;
        IPublicMessageCipher pmc = CipherSuiteTestsUtility.setUpPublicMessageCipher(encrypt);
        byte[] result = new byte[CipherSuiteTestsUtility.PLAINSIZE];
        try {
            for (int i = 0; i < FaultyVectors.vecCount; i++) {
                pmc.doFinalOptimized(FaultyVectors.halfNonces[i], FaultyVectors.cipherKeys[i], getByteInput(), 0, result, 0);
                assertFalse(new String(result).equals(testInput));
            }
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    @SmallTest
    public void testBrokenEncryption() throws Exception {
        int encrypt = Cipher.DECRYPT_MODE;
        IPublicMessageCipher pmc = CipherSuiteTestsUtility.setUpPublicMessageCipher(encrypt);
        byte[] result = new byte[CipherSuiteTestsUtility.PLAINSIZE];
        try {
            pmc.doFinalOptimized(FaultyVectors.halfnonce, FaultyVectors.failureCK, getByteInput(), 0, result, 0);
            assertFalse(new String(result).equals(testInput));
        } catch (Exception e) {
            assertTrue(false);
        }
    }

    @SmallTest
    public void testBrokenEncryption2() throws Exception {
        int encrypt = Cipher.ENCRYPT_MODE;
        IPublicMessageCipher pmc = CipherSuiteTestsUtility.setUpPublicMessageCipher(encrypt);
        byte[] result = new byte[CipherSuiteTestsUtility.PLAINSIZE];
        try {
            pmc.doFinalOptimized(FaultyVectors.halfnonce, FaultyVectors.failureCK, getByteInput(), 0, result, 0);
            assertTrue(new String(result).equals(new String(FaultyVectors.cipher)));
        } catch (Exception e) {
            throw e;
        }
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
