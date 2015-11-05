package de.tu_darmstadt.adtn.groupciphersuitetests;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import de.tu_darmstadt.adtn.ciphersuite.PublicMessageDecryption;
import de.tu_darmstadt.adtn.ciphersuite.ciphers.Ciphermode;
import de.tu_darmstadt.adtn.ciphersuite.ciphers.PublicMessageCipherFactory;
import de.tu_darmstadt.adtn.ciphersuite.hashes.ComputeMacFactory;
import de.tu_darmstadt.adtn.errorlogger.ErrorLoggingSingleton;

import static de.tu_darmstadt.adtn.groupciphersuitetests.utils.CipherChaCha20Poly1305Vectors.getChacha20Poly1305Output;
import static de.tu_darmstadt.adtn.groupciphersuitetests.utils.CipherSuiteTestsUtility.PLAINSIZE;
import static de.tu_darmstadt.adtn.TestUtility.bytesEqual;
import static de.tu_darmstadt.adtn.groupciphersuitetests.utils.CipherSuiteTestsUtility.cipherAlgorithm;
import static de.tu_darmstadt.adtn.groupciphersuitetests.utils.CipherSuiteTestsUtility.ivLengthCipher;
import static de.tu_darmstadt.adtn.groupciphersuitetests.utils.CipherSuiteTestsUtility.ivLengthMAC;
import static de.tu_darmstadt.adtn.groupciphersuitetests.utils.CipherSuiteTestsUtility.ivOffset;
import static de.tu_darmstadt.adtn.groupciphersuitetests.utils.CipherSuiteTestsUtility.macOffset;
import static de.tu_darmstadt.adtn.groupciphersuitetests.utils.CipherSuiteTestsUtility.textOffset;
import static de.tu_darmstadt.adtn.groupciphersuitetests.utils.CipherTestVectors.groupKeyList;
import static de.tu_darmstadt.adtn.groupciphersuitetests.utils.CipherTestVectors.testInput;

/**
 * PublicMessageDecryption tests
 */
public class PublicMessageDecryptionTests extends AndroidTestCase {

    private PublicMessageDecryption uut;
    private byte[][] input = getChacha20Poly1305Output();


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
        log.setContext(new RenamingDelegatingContext(getContext(), "test."));
        uut = new PublicMessageDecryption(
                ComputeMacFactory.getInstance(),
                PublicMessageCipherFactory.getPublicMessageCipherInstance(
                        cipherAlgorithm, Ciphermode.ENCRYPT,
                        ivLengthCipher),
                ivLengthMAC,
                macOffset,
                ivOffset,
                textOffset,
                PLAINSIZE
        );
    }

    /**
     * Test if decrypted ciphertext has the right size.
     */
    @SmallTest
    public void testCase1() throws Exception {
        for (byte[] anInput : input) {
            byte[] result = uut.decrypt(anInput, groupKeyList);
            assertEquals(PLAINSIZE, result.length);
        }
    }

    /**
     * Test if cipher text is decrypted correctly
     */
    @SmallTest
    public void testCase2() throws Exception {
        for (byte[] anInput : input) {
            byte[] result = uut.decrypt(anInput, groupKeyList);
            assertEquals(testInput, new String(result));
        }
    }

    /**
     * Test if decryption can be used multiple times and delivers the same result
     */
    @SmallTest
    public void testCase3() throws Exception {
        byte[] result1 = uut.decrypt(input[0], groupKeyList);
        byte[] result2 = uut.decrypt(input[0], groupKeyList);
        assertTrue(bytesEqual(result1, result2, 0));
        assertTrue(bytesEqual(result2, result1, 0));
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
