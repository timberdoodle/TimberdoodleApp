package de.tudarmstadt.adtn.groupciphersuitetests;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKey;

import de.tudarmstadt.adtn.ciphersuite.GroupCipherSuite;
import de.tudarmstadt.adtn.ciphersuite.IGroupCipher;
import de.tudarmstadt.adtn.ciphersuite.utils.IGroupKey;
import de.tudarmstadt.adtn.errorlogger.ErrorLoggingSingleton;

import static de.tudarmstadt.adtn.groupciphersuitetests.utils.CipherSuiteTestsUtility.PLAINSIZE;
import static de.tudarmstadt.adtn.groupciphersuitetests.utils.CipherSuiteTestsUtility.charEncoding;
import static de.tudarmstadt.adtn.groupciphersuitetests.utils.CipherSuiteTestsUtility.isClamped;
import static de.tudarmstadt.adtn.groupciphersuitetests.utils.CipherSuiteTestsUtility.keyLength;
import static de.tudarmstadt.adtn.groupciphersuitetests.utils.CipherTestVectors.getByteInput;
import static de.tudarmstadt.adtn.groupciphersuitetests.utils.CipherTestVectors.groupKeyList;
import static de.tudarmstadt.adtn.groupciphersuitetests.utils.CipherTestVectors.testInput;


/**
 * GroupCipherSuite tests
 */
public class CipherSuiteTests extends AndroidTestCase {

    private IGroupCipher uut = new GroupCipherSuite(PLAINSIZE);

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
        log.setContext(new RenamingDelegatingContext(getContext(), "test."));
    }

    public void testPlains() throws Exception {
        byte[][] encrypted = uut.encrypt(getByteInput(), groupKeyList);
        byte[] decrypted = uut.tryDecrypt(encrypted[0], groupKeyList);
        String result = new String(decrypted);
        assertEquals(testInput, result);
    }

    public void testKeyGeneration() throws Exception {
        IGroupKey key = (IGroupKey) uut.generateKey();
        assertEquals(keyLength, key.getCipherKey().getEncoded().length);
        assertEquals(keyLength, key.getMACKey().getEncoded().length);
        assertTrue(isClamped(key.getMACKey().getEncoded()));
    }

    public void testReadKeyFromByteArray() throws Exception{
        SecretKey key = uut.generateKey();
        byte[] inputData = key.getEncoded();
        key = uut.byteArrayToSecretKey(inputData);
        //test if key can be used to encrypt and decrypt
        byte[] plaintext = getByteInput();
        List<SecretKey> list = new ArrayList<SecretKey>(1);
        list.add(key);
        byte[][] encrypted = uut.encrypt(plaintext, list);
        assertEquals(testInput, new String(uut.tryDecrypt(encrypted[0], list), charEncoding));
    }

    public void testGetByteArrayFromSecretKey() throws Exception{
        SecretKey key = uut.generateKey();
        byte[] result = key.getEncoded();
        assertEquals(uut.getEncodedKeySize(), result.length);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
