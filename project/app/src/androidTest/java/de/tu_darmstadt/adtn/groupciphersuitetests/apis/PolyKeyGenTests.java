package de.tu_darmstadt.adtn.groupciphersuitetests.apis;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import javax.crypto.SecretKey;

import de.tu_darmstadt.adtn.ciphersuite.utils.ISymmetricKeyGenerator;
import de.tu_darmstadt.adtn.ciphersuite.utils.SymmetricKeyGenerator;
import de.tu_darmstadt.adtn.errorlogger.ErrorLoggingSingleton;

import static de.tu_darmstadt.adtn.groupciphersuitetests.utils.CipherSuiteTestsUtility.isClamped;

/**
 * Tests for Poly1305 key generation
 */
public class PolyKeyGenTests extends AndroidTestCase {

    private ISymmetricKeyGenerator uut;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        uut = new SymmetricKeyGenerator(new org.spongycastle.jce.provider.BouncyCastleProvider(),
                "Poly1305-AES", 256);
        ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
        log.setContext(new RenamingDelegatingContext(getContext(), "test."));
    }

    @SmallTest
    public void testKeyGen() {
        SecretKey key = uut.generateKey();
        byte[] result = key.getEncoded();
        //Check if key has correct format
        assertTrue(isClamped(result));
    }

    @SmallTest
    public void testKeyGenLength() {
        SecretKey key = uut.generateKey();
        byte[] result = key.getEncoded();
        assertEquals(32, result.length);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        uut = null;
    }
}
