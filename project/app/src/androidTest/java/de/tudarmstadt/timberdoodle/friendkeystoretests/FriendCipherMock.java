package de.tudarmstadt.timberdoodle.friendkeystoretests;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Collection;

import de.tudarmstadt.timberdoodle.friendcipher.IFriendCipher;

class FriendCipherMock implements IFriendCipher {

    private final int encodedPublicKeySize;

    public FriendCipherMock(int encodedPublicKeySize) {
        this.encodedPublicKeySize = encodedPublicKeySize;
    }

    @Override
    public byte[] encrypt(byte[] plaintext, int offset, int count, PublicKey key) {
        return null;
    }

    @Override
    public byte[] tryDecrypt(byte[] ciphertext) {
        return null;
    }

    @Override
    public int getEncodedPublicKeySize() {
        return encodedPublicKeySize;
    }

    @Override
    public int getNumBytesInSignature() {
        return 0;
    }

    @Override
    public PublicKey byteArrayToPublicKey(byte[] key) {
        return new PublicKeyMock(key);
    }

    @Override
    public byte[] publicKeyToByteArray(PublicKey publicKey) {
        return publicKey instanceof PublicKeyMock ? ((PublicKeyMock) publicKey).getEncoded() : null;
    }

    @Override
    public PublicKey checkSignature(byte[] data, int dataOffset, int dataCount, byte[] signature, int signatureOffset, Collection<PublicKey> publicKeys) {
        return null;
    }

    @Override
    public byte[] sign(byte[] buffer, int offset, int count) {
        return null;
    }

    @Override
    public void setPrivateKey(PrivateKey privateKey) {
    }

    @Override
    public int getCiphertextSize(int plaintextSize) {
        return 0;
    }

    @Override
    public int getMaxPlaintextSize(int ciphertextSize) {
        return 0;
    }

    @Override
    public KeyPair generateKeyPair() {
        return null;
    }

    @Override
    public boolean isPrivateKeySet() {
        return false;
    }

    private class PublicKeyMock implements PublicKey {
        private final static String ALGORITHM = "Test key algorithm";
        private final static String FORMAT = "RAW";
        private final byte[] encoded;

        public PublicKeyMock(byte[] encoded) {
            this.encoded = encoded.clone();
        }

        @Override
        public String getAlgorithm() {
            return ALGORITHM;
        }

        @Override
        public String getFormat() {
            return FORMAT;
        }

        @Override
        public byte[] getEncoded() {
            return encoded;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof PublicKeyMock)) return false;
            return ((PublicKeyMock) o).getAlgorithm().equals(ALGORITHM) &&
                    ((PublicKeyMock) o).getFormat().equals(FORMAT) &&
                    Arrays.equals(((PublicKeyMock) o).getEncoded(), encoded);
        }
    }
}
