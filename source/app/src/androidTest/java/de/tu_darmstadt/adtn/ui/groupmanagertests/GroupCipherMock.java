package de.tu_darmstadt.adtn.ui.groupmanagertests;

import java.util.Collection;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import de.tu_darmstadt.adtn.ciphersuite.IGroupCipher;

public class GroupCipherMock implements IGroupCipher {

    private final static String ALGORITHM = "GroupCipherMock";

    private int encodedKeySize = 1;

    @Override
    public int getCipherTextSize() {
        return 0;
    }

    @Override
    public SecretKey generateKey() {
        return null;
    }

    @Override
    public byte[][] encrypt(byte[] plaintext, Collection<SecretKey> keys) {
        return new byte[0][];
    }

    @Override
    public byte[] tryDecrypt(byte[] ciphertext, Collection<SecretKey> keys) {
        return new byte[0];
    }

    @Override
    public SecretKey byteArrayToSecretKey(byte[] keybytes) {
        return new SecretKeySpec(keybytes, ALGORITHM);
    }

    @Override
    public byte[] secretKeyToByteArray(SecretKey secretKey) {
        return secretKey.getEncoded();
    }

    @Override
    public int getEncodedKeySize() {
        return encodedKeySize;
    }

    public void setEncodedKeySize(int encodedKeySize) {
        this.encodedKeySize = encodedKeySize;
    }
}
