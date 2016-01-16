package de.tudarmstadt.adtn.groupciphersuitetests;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import junit.framework.Assert;

import de.tudarmstadt.adtn.ciphersuite.PublicMessageEncryption;
import de.tudarmstadt.adtn.ciphersuite.ciphers.PublicMessageCipherFactory;
import de.tudarmstadt.adtn.ciphersuite.hashes.ComputeMacFactory;
import de.tudarmstadt.adtn.errorlogger.ErrorLoggingSingleton;
import de.tudarmstadt.adtn.groupciphersuitetests.mockobjects.NonceGenMock;
import de.tudarmstadt.adtn.groupciphersuitetests.utils.CipherChaCha20Poly1305Vectors;
import de.tudarmstadt.adtn.groupciphersuitetests.utils.CipherSuiteTestsUtility;
import de.tudarmstadt.adtn.groupciphersuitetests.utils.CipherTestVectors;
import static de.tudarmstadt.adtn.TestUtility.*;


/**
 * Tests for PublicMessageEncryption
 */
public class PublicMessageEncryptionTests extends AndroidTestCase {

    private PublicMessageEncryption uut;
    private byte[][] output = CipherChaCha20Poly1305Vectors.getChacha20Poly1305Output();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
        log.setContext(new RenamingDelegatingContext(getContext(), "test."));
        uut = CipherSuiteTestsUtility.setUpPublicMessageEncryptor(new NonceGenMock(), ComputeMacFactory.getInstance());
    }

    /**
     * Test if created arrays do not exceed ciphertextsize
     */
    @SmallTest
    public void testCase1() throws Exception {
        byte[][] result = uut.encrypt(CipherTestVectors.getByteInput(),
                CipherTestVectors.groupKeyList);
        for (byte[] aResult : result) Assert.assertEquals(CipherSuiteTestsUtility.CIPHERSIZE, aResult.length);
    }

    /**
     * Test if plaintext bytes and encrypted bytes are not equal after encryption
     */
    @SmallTest
    public void testCase2() throws Exception {
        byte[] plaintext = CipherTestVectors.getByteInput();
        byte[][] result = uut.encrypt(plaintext,
                CipherTestVectors.groupKeyList);
        assertFalse(bytesEqual(plaintext,
                result[0], 24));
    }

    /**
     * Check if output is correct
     *
     * @throws Exception
     */
    @SmallTest
    public void testCase3() throws Exception {
        byte[][] result = uut.encrypt(CipherTestVectors.getByteInput(), CipherTestVectors.groupKeyList);
        for (int i = 0; i < result.length; i++) {
            assertTrue(bytesEqual(output[i], result[i], 0));
            assertTrue(bytesEqual(result[i], output[i], 0));
        }
    }

    /**
     * Test if encryption can be used multiple times without delivering the same result
     */
    @SmallTest
    public void testCase4() throws Exception {
        byte[] bytes = CipherTestVectors.getByteInput();
        uut = CipherSuiteTestsUtility.setUpPublicMessageEncryptor(PublicMessageCipherFactory.getNonceGenerator(CipherSuiteTestsUtility.ivLengthMAC), ComputeMacFactory.getInstance());
        byte[][] result1 = uut.encrypt(bytes, CipherTestVectors.groupKeyList);
        byte[][] result2 = uut.encrypt(bytes, CipherTestVectors.groupKeyList);
        assertFalse(bytesEqual(result1[0], result2[0], 0));
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        uut = null;
    }
}
