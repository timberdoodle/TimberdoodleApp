package de.tudarmstadt.adtn.groupciphersuitetests.apis;

import android.test.AndroidTestCase;

import de.tudarmstadt.adtn.ciphersuite.utils.ISymmetricKeyGenerator;
import de.tudarmstadt.adtn.ciphersuite.utils.SymmetricKeyGenerator;

/**
 * Tests generation of ChaCha keys.
 */
public class ChachaKeyGenTests extends AndroidTestCase {

    private ISymmetricKeyGenerator gen = new SymmetricKeyGenerator(new org.spongycastle.jce.provider.BouncyCastleProvider(),
            "ChaCha", 256);

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testKeyGen() {
        byte[] key = gen.generateKey().getEncoded();
        assertEquals(32, key.length);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
