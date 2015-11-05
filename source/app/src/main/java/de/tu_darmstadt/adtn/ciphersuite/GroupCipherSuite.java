package de.tu_darmstadt.adtn.ciphersuite;

import java.util.Collection;

import javax.crypto.SecretKey;

import de.tu_darmstadt.adtn.ciphersuite.Utils.GroupKeyGenerator;
import de.tu_darmstadt.adtn.ciphersuite.ciphers.Ciphermode;
import de.tu_darmstadt.adtn.ciphersuite.ciphers.INonceGenerator;
import de.tu_darmstadt.adtn.ciphersuite.ciphers.PublicMessageCipherFactory;
import de.tu_darmstadt.adtn.ciphersuite.hashes.ComputeMacFactory;
import de.tu_darmstadt.adtn.ciphersuite.hashes.IComputeMAC;
import de.tu_darmstadt.adtn.errorlogger.ErrorLoggingSingleton;

/**
 * Ciphersuite that offers functionality to encrypt and decrypt byte arrays in the timberdoodle packet
 * format and functionality to generate keys.
 */
public class GroupCipherSuite implements IGroupCipher {

    private final int cipherSize;
    private final String cipherAlgorithm = "ChaCha";
    private final String macAlgorithm = "Poly1305-AES";
    private final int nonceLengthCipher = 8;
    private final int nonceLengthMAC = 16;
    private final int cipherKeySize = 256, macKeySize = 256;

    private PublicMessageDecryption decryptor = null;
    private PublicMessageEncryption encryptor = null;

    public GroupCipherSuite(int sizeOfPlaintext) {
        try {
            cipherSize = ComputeMacFactory.getInstance().length() + nonceLengthMAC + sizeOfPlaintext;
            IComputeMAC mac = ComputeMacFactory.getInstance();
            INonceGenerator nonce = PublicMessageCipherFactory.getNonceGenerator(nonceLengthMAC);
            encryptor = new PublicMessageEncryption(
                    mac,
                    PublicMessageCipherFactory.getPublicMessageCipherInstance(cipherAlgorithm, Ciphermode.ENCRYPT, nonceLengthCipher),
                    nonce,
                    0,
                    mac.length(),
                    mac.length() + nonceLengthMAC,
                    cipherSize
            );
            decryptor = new PublicMessageDecryption(
                    mac,
                    PublicMessageCipherFactory.getPublicMessageCipherInstance(cipherAlgorithm, Ciphermode.DECRYPT, nonceLengthCipher),
                    nonceLengthMAC,
                    0,
                    mac.length(),
                    mac.length() + nonceLengthMAC,
                    sizeOfPlaintext
            );
        } catch (Exception e) {
            ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
            log.storeError(ErrorLoggingSingleton.getExceptionStackTraceAsFormattedString(e));
            throw new RuntimeException(e);
        }
    }


    /**
     * Computes the cipher text's size and returns it.
     *
     * @return Returns the size in bytes as an integer.
     */
    @Override
    public int getCipherTextSize() {
        return cipherSize;
    }

    /**
     * Generates a key.
     *
     * @return returns the generated key as a SecretKey.
     */
    @Override
    public SecretKey generateKey() {
        GroupKeyGenerator keyGenerator = new GroupKeyGenerator(cipherAlgorithm,
                cipherKeySize, macAlgorithm, macKeySize);
        return keyGenerator.generateKey();
    }

    /**
     * Encrypts the given plaintext with every key of the given collection.
     *
     * @param plaintext Plaintext that is encrypted.
     * @param keys      Collection of SecretKeys that are used to encrypt the plaintext
     * @return Returns the cipher texts as array of array of byte.
     */
    @Override
    public byte[][] encrypt(byte[] plaintext, Collection<SecretKey> keys) {
        return encryptor.encrypt(plaintext, keys);
    }

    /**
     * Tries to decrypt a given cipher text and returns the plaintext on success.
     * If a matching key cannot be found in the collection of keys null is returned.
     *
     * @param ciphertext Cipher text that is decrypted.
     * @param keys       Collection of keys that are used to decrypt the cipher text.
     * @return Returns the decrypted cipher text as byte array. If there was no matching
     * key in the key collection, null is returned.
     */
    @Override
    public byte[] tryDecrypt(byte[] ciphertext, Collection<SecretKey> keys) {
        return decryptor.decrypt(ciphertext, keys);
    }

    /**
     * Transforms an encoded key to a SecretKey.
     *
     * @param keybytes Byte array that represent a key
     * @return Returns the key as SecretKey
     */
    @Override
    public SecretKey byteArrayToSecretKey(byte[] keybytes) {
        GroupKeyGenerator keyGenerator = new GroupKeyGenerator(cipherAlgorithm,
                cipherKeySize, macAlgorithm, macKeySize);
        return keyGenerator.readKeyFromByteArray(keybytes);
    }

    /**
     * Creates a byte array from a secret key that can be converted back using byteArrayToSecretKey.
     *
     * @param secretKey The secret key.
     * @return A byte array representing the key.
     */
    @Override
    public byte[] secretKeyToByteArray(SecretKey secretKey) {
        return secretKey.getEncoded();
    }

    /**
     * @return The size in bytes of a group key that was converted to a byte array.
     */
    @Override
    public int getEncodedKeySize() {
        return cipherKeySize/8 + macKeySize/8;
    }
}
