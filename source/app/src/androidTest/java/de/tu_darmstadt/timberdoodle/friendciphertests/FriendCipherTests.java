package de.tu_darmstadt.timberdoodle.friendciphertests;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import de.tu_darmstadt.timberdoodle.friendcipher.FriendCipher;

public class FriendCipherTests extends AndroidTestCase {

    private FriendCipher uut;
    private KeyPair keyPair;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        uut = new FriendCipher();
        keyPair = uut.generateKeyPair();
        uut.setPrivateKey(keyPair.getPrivate());
    }

    @SmallTest
    public void testEncryptDecryptAndCiphertextSizeEstimation() throws Exception {
        Random rnd = new Random();
        for (int i = 0; i < 50; ++i) {
            // Create random plaintext of max. 1500 bytes
            byte[] plaintext = new byte[rnd.nextInt(1501)];
            rnd.nextBytes(plaintext);
            // Encrypt and decrypt
            byte[] ciphertext = uut.encrypt(plaintext, 0, plaintext.length, keyPair.getPublic());
            assertEquals(ciphertext.length, uut.getCiphertextSize(plaintext.length));
            byte[] decrypted = uut.tryDecrypt(ciphertext);
            assertTrue(Arrays.equals(plaintext, decrypted));
        }
    }

    @SmallTest
    public void testSignature() {
        Random rnd = new Random();
        for (int i = 0; i < 50; ++i) {
            // Create random buffer of max. 1500 bytes
            byte[] buffer = new byte[rnd.nextInt(1501)];
            rnd.nextBytes(buffer);
            // Create and verify signature
            byte[] signature = uut.sign(buffer, 0, buffer.length);
            assertSame(keyPair.getPublic(), uut.checkSignature(buffer, 0, buffer.length, signature, 0, Collections.singleton(keyPair.getPublic())));
        }
    }

    @SmallTest
    public void testKeyByteConversion() {
        byte[] keyBytes = uut.publicKeyToByteArray(keyPair.getPublic());
        PublicKey key = uut.byteArrayToPublicKey(keyBytes);
        byte[] keyBytes2 = uut.publicKeyToByteArray(key);
        assertTrue(Arrays.equals(keyBytes, keyBytes2));
    }

    @SmallTest
    public void testMaxPlaintextSizeEstimation() {
        assertEquals(1135, uut.getMaxPlaintextSize(1453));
    }
}
