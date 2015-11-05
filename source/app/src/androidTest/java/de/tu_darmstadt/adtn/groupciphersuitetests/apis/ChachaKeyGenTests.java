package de.tu_darmstadt.adtn.groupciphersuitetests.apis;

import android.test.AndroidTestCase;

import de.tu_darmstadt.adtn.ciphersuite.Utils.ISymmetricKeyGenerator;
import de.tu_darmstadt.adtn.ciphersuite.Utils.SymmetricKeyGenerator;

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
