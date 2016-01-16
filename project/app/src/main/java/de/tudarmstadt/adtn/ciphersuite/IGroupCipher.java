package de.tudarmstadt.adtn.ciphersuite;

import java.util.Collection;

import javax.crypto.SecretKey;

/**
 * Interface for a cipher suite.
 */
public interface IGroupCipher {

    /**
     * Computes the cipher text's size and returns it.
     *
     * @return Returns the size in bytes as an integer.
     */
    int getCipherTextSize();

    /**
     * Generates a key.
     *
     * @return returns the generated key as a SecretKey.
     */
    SecretKey generateKey();

    /**
     * Encrypts the given plaintext with every key of the given list.
     *
     * @param plaintext Plaintext that is encrypted.
     * @param keys      Collection of SecretKeys that are used to encrypt the plaintext
     * @return Returns the cipher texts as array of array of byte.
     */
    byte[][] encrypt(byte[] plaintext, Collection<SecretKey> keys);

    /**
     * Tries to decrypt a given cipher text and returns the plaintext on success.
     * If a matching key cannot be found in the list of keys null is returned.
     *
     * @param ciphertext Cipher text that is decrypted.
     * @param keys       Collection of keys that are used to decrypt the cipher text.
     * @return Returns the decrypted cipher text as byte array. If there was no matching
     * key in the key list, null is returned.
     */
    byte[] tryDecrypt(byte[] ciphertext, Collection<SecretKey> keys);

    /**
     * Transforms an encoded key to a SecretKey.
     *
     * @param keybytes
     * @return
     */
    SecretKey byteArrayToSecretKey(byte[] keybytes);

    /**
     * Creates a byte array from a secret key that can be converted back using byteArrayToSecretKey.
     *
     * @param secretKey The secret key.
     * @return A byte array representing the key.
     */
    byte[] secretKeyToByteArray(SecretKey secretKey);

    /**
     * @return The size in bytes of a group key that was converted to a byte array.
     */
    int getEncodedKeySize();
}
