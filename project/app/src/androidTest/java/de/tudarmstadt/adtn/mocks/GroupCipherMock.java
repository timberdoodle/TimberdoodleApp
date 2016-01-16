package de.tudarmstadt.adtn.mocks;

import java.util.Collection;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import de.tudarmstadt.adtn.ciphersuite.IGroupCipher;

/**
 * Mocks the GroupCipher
 */
public class GroupCipherMock implements IGroupCipher{
    /**
     * Computes the cipher text's size and returns it.
     *
     * @return Returns the size in bytes as an integer.
     */
    @Override
    public int getCipherTextSize() {
        return 0;
    }

    /**
     * Generates a key.
     *
     * @return returns the generated key as a SecretKey.
     */
    @Override
    public SecretKey generateKey() {
        return null;
    }

    /**
     * Encrypts the given plaintext with every key of the given list.
     *
     * @param plaintext Plaintext that is encrypted.
     * @param keys      Collection of SecretKeys that are used to encrypt the plaintext
     * @return Returns the cipher texts as array of array of byte.
     */
    @Override
    public byte[][] encrypt(byte[] plaintext, Collection<SecretKey> keys) {
        return new byte[0][];
    }

    /**
     * Tries to decrypt a given cipher text and returns the plaintext on success.
     * If a matching key cannot be found in the list of keys null is returned.
     *
     * @param ciphertext Cipher text that is decrypted.
     * @param keys       Collection of keys that are used to decrypt the cipher text.
     * @return Returns the decrypted cipher text as byte array. If there was no matching
     * key in the key list, null is returned.
     */
    @Override
    public byte[] tryDecrypt(byte[] ciphertext, Collection<SecretKey> keys) {
        return new byte[0];
    }

    /**
     * Transforms an encoded key to a SecretKey.
     *
     * @param keybytes
     * @return
     */
    @Override
    public SecretKey byteArrayToSecretKey(byte[] keybytes) {
        return new SecretKeySpec(keybytes, "Some algo");
    }

    /**
     * Creates a byte array from a secret key that can be converted back using byteArrayToSecretKey.
     *
     * @param secretKey The secret key.
     * @return A byte array representing the key.
     */
    @Override
    public byte[] secretKeyToByteArray(SecretKey secretKey) {
        return new byte[0];
    }

    /**
     * @return The size in bytes of a group key that was converted to a byte array.
     */
    @Override
    public int getEncodedKeySize() {
        return 0;
    }
}
