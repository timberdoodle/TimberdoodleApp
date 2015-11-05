package de.tu_darmstadt.adtn.debug;

import java.util.Arrays;
import java.util.Collection;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import de.tu_darmstadt.adtn.ciphersuite.IGroupCipher;

/**
 * Debug group cipher that does not encrypt.
 */
public class PassthroughGroupCipher implements IGroupCipher {

    private final static String ALGORITHM_NAME = "DummyCipherPassthrough";
    private final static byte[] MAGIC_BYTES = {(byte) 0xC8, (byte) 0xAA, (byte) 0xEC, 0x31, 0x08, 0x04, (byte) 0xB6, (byte) 0xB4};

    private final int plaintextSize, ciphertextSize;

    public PassthroughGroupCipher(int plaintextSize) {
        this.plaintextSize = plaintextSize;
        this.ciphertextSize = MAGIC_BYTES.length + plaintextSize;
    }

    @Override
    public int getCipherTextSize() {
        return ciphertextSize;
    }

    @Override
    public SecretKey generateKey() {
        return new SecretKeySpec(new byte[1], ALGORITHM_NAME);
    }

    @Override
    public byte[][] encrypt(byte[] plaintext, Collection<SecretKey> keys) {
        if (plaintext.length != plaintextSize) {
            throw new RuntimeException("Size of plaintext is not as expected");
        }

        // ciphertext = MAGIC_BYTES + plaintext
        byte[] ciphertext = new byte[ciphertextSize];
        System.arraycopy(MAGIC_BYTES, 0, ciphertext, 0, MAGIC_BYTES.length);
        System.arraycopy(plaintext, 0, ciphertext, MAGIC_BYTES.length, plaintextSize);

        // Returns array of references to ciphertext
        byte[][] clones = new byte[keys.size()][];
        for (int i = 0; i < clones.length; ++i) {
            clones[i] = ciphertext;
        }
        return clones;
    }

    @Override
    public byte[] tryDecrypt(byte[] ciphertext, Collection<SecretKey> keys) {
        if (ciphertext.length != ciphertextSize) {
            throw new RuntimeException("Size of ciphertext is not as expected");
        }

        // Check if ciphertext starts with MAGIC_BYTES
        for (int i = 0; i < MAGIC_BYTES.length; ++i) {
            if (ciphertext[i] != MAGIC_BYTES[i]) return null;
        }

        // Payload is ciphertext without MAGIC_BYTES
        return Arrays.copyOfRange(ciphertext, MAGIC_BYTES.length, ciphertext.length);
    }

    @Override
    public SecretKey byteArrayToSecretKey(byte[] key) {
        return new SecretKeySpec(key, ALGORITHM_NAME);
    }

    @Override
    public byte[] secretKeyToByteArray(SecretKey secretKey) {
        return secretKey.getEncoded();
    }

    @Override
    public int getEncodedKeySize() {
        return 1;
    }
}
