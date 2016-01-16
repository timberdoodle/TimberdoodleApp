package de.tudarmstadt.adtn.groupciphersuitetests.utils;

import java.security.Provider;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import de.tudarmstadt.adtn.ciphersuite.PublicMessageEncryption;
import de.tudarmstadt.adtn.ciphersuite.utils.ISymmetricKeyGenerator;
import de.tudarmstadt.adtn.ciphersuite.utils.GroupKey;
import de.tudarmstadt.adtn.ciphersuite.utils.IGroupKey;
import de.tudarmstadt.adtn.ciphersuite.utils.SymmetricKeyGenerator;
import de.tudarmstadt.adtn.ciphersuite.ciphers.INonceGenerator;
import de.tudarmstadt.adtn.ciphersuite.ciphers.IPublicMessageCipher;
import de.tudarmstadt.adtn.ciphersuite.ciphers.PublicMessageCipherFactory;
import de.tudarmstadt.adtn.ciphersuite.ciphers.PublicMessageCipherImpl;
import de.tudarmstadt.adtn.ciphersuite.ciphers.SimpleNonceGenerator;
import de.tudarmstadt.adtn.ciphersuite.hashes.ComputeMacFactory;
import de.tudarmstadt.adtn.ciphersuite.hashes.IComputeMAC;
import de.tudarmstadt.adtn.groupciphersuitetests.mockobjects.PublicMessageDecryptionDebug;

import static de.tudarmstadt.adtn.groupciphersuitetests.utils.CipherTestVectors.getByteInput;

/**
 * Utilities and constants for the cipher tests
 */
public class CipherSuiteTestsUtility {

    public static final int ivLengthCipher = 8;
    public static final int ivLengthMAC = 16;
    public static final int keyLength = 32;
    public static final int macLength = 16;
    public static final String cipherAlgorithm = "ChaCha";
    public static final String macAlgorithm = "Poly1305-AES";
    public static final String charEncoding = "UTF-8";
    public static final Provider provider = new org.spongycastle.jce.provider.BouncyCastleProvider();
    public static final int macOffset = 0;
    public static final int ivOffset = 16;
    public static final int textOffset = 32;
    public static final int PLAINSIZE = 1468;
    public static final int CIPHERSIZE = PLAINSIZE + textOffset;
    public static final String ls = System.getProperty("line.separator");
    public static final boolean logReport = true;

    public static byte[][] getSubArraysFromPacketArray(byte[][] pArray, int offset, int length) {
        byte[][] result = new byte[pArray.length][];
        for (int i = 0; i < pArray.length; i++) result[i] = getSubArrayFromPacket(pArray[i], offset, length);
        return result;
    }

    /**
     * Extracts a sub array from a packet
     *
     * @param packet a Timberdoodle packet
     * @param offset start index
     * @param length length of sub array (length + offset <= packet.length)
     * @return returns the sub array
     */
    public static byte[] getSubArrayFromPacket(byte[] packet, int offset, int length) {
        byte[] result = new byte[length];
        System.arraycopy(packet, offset, result, 0, length);
        return result;
    }

    /**
     * Generates a list of ChaCha keys with amount elements
     *
     * @param amount amount of keys that are generated
     * @return returns a list with SecretKeys
     */
    public static List<SecretKey> generateChaChaKeys(int amount) {
        List<SecretKey> result = new ArrayList<>(amount);
        ISymmetricKeyGenerator gen = new SymmetricKeyGenerator(provider, cipherAlgorithm, keyLength*8);
        for (int i = 0; i < amount; i++) {
            SecretKey key = gen.generateKey();
            result.add(i, key);
        }
        return result;
    }

    /**
     * Generates a list of poly keys with amount elements
     *
     * @param amount amount of keys that are generated
     * @return returns a list with SecretKeys
     */
    public static List<SecretKey> generatePolyKeys(int amount) {
        List<SecretKey> result = new ArrayList<>(amount);
        ISymmetricKeyGenerator gen = new SymmetricKeyGenerator(provider, macAlgorithm, keyLength*8);
        for (int i = 0; i < amount; i++)
            result.add(i, gen.generateKey());
        return result;
    }

    /**
     * Generates a list of GroupKey instances with amount elements
     *
     * @param amount amount of keys that are generated
     * @return returns a list with GroupKeys
     */
    public static List<SecretKey> generateGroupKeys(int amount) {
        List<SecretKey> cipherKeys = generateChaChaKeys(amount);
        List<SecretKey> macKeys = generatePolyKeys(amount);
        List<SecretKey> result = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++)
            result.add(new GroupKey(cipherKeys.get(i), macKeys.get(i)));
        return result;
    }

    /**
     * Extracts a specific key from a group key
     *
     * @param key       a GroupKey
     * @param cipherKey This determines the kind of key that is extracted. True for cipher key, false for mac key
     * @return returns the specified SecretKey
     */
    public static SecretKey getSpecificKeyFromGroupKey(SecretKey key, boolean cipherKey) {
        if (cipherKey) return (((IGroupKey) key).getCipherKey());
        else return ((IGroupKey) key).getMACKey();
    }

    /**
     * Extracts specific keys from a GroupKey list
     *
     * @param list       a GroupKey list
     * @param cipherKeys This determines the kind of keys that are extracted. True for cipher keys, false for mac keys
     * @return Returns a list with the specified SecretKeys
     */
    public static List<SecretKey> getSpecificKeysFromGroupKeyList(List<SecretKey> list, boolean cipherKeys) {
        List<SecretKey> result = new ArrayList<>(list.size());
        for (SecretKey key : list) {
            result.add(getSpecificKeyFromGroupKey(key, cipherKeys));
        }
        return result;
    }

    /**
     * Generates a list of random nonces. The list has amount elements of
     * length length.
     *
     * @param amount amount of nonces that are generated
     * @param length length of the generated nonces
     * @return Returns a list of nonces
     */
    public static List<byte[]> generateNonceList(int amount, int length) {
        List<byte[]> result = new ArrayList<>(amount);
        SimpleNonceGenerator gen = new SimpleNonceGenerator(length);
        for (int i = 0; i < amount; i++)
            result.add(i, gen.generateNonce());
        return result;
    }

    /**
     * Sets up a PublicMessageCipher
     *
     * @param mode Cipher.ENCRYPT_MODE or CIPHER.DECRYPT_MODE
     * @return Returns a PublicMessageCipher in the specified mode
     */
    public static IPublicMessageCipher setUpPublicMessageCipher(int mode) {
        try {
            Cipher cipher = Cipher.getInstance(cipherAlgorithm, CipherSuiteTestsUtility.provider);
            return new PublicMessageCipherImpl(cipher, mode, ivLengthCipher);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Sets up a PublicMessageEncryption with the passed in IComputeMAC instanceand
     * INonceGenerator instance
     *
     * @param generator Instance of INonceGenerator to generate a nonce (can be mocked)
     * @param mac       Instance of IComputeMac (can be mocked)
     * @return returns a PublicMessageEncryption instance.
     */
    public static PublicMessageEncryption setUpPublicMessageEncryptor(INonceGenerator generator, IComputeMAC mac) {
        try {
            return new PublicMessageEncryption(
                    mac,
                    setUpPublicMessageCipher(Cipher.ENCRYPT_MODE),
                    generator,
                    macOffset,
                    ivOffset,
                    textOffset, CIPHERSIZE
            );
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Sets up a PublicMessageDecryption with the passed in IComputeMAC instance
     *
     * @param mac an IComputeMAC instance
     * @return Returns a PublicMessageDecryption instance
     */
    public static PublicMessageDecryptionDebug setUpPublicMessageDecryptionDebug(IComputeMAC mac) {
        try {
            return new PublicMessageDecryptionDebug(
                    mac,
                    setUpPublicMessageCipher(Cipher.DECRYPT_MODE),
                    ivLengthMAC,
                    macOffset,
                    ivOffset,
                    textOffset, PLAINSIZE
            );
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks if the given key is clamped (in the poly1305 format)
     *
     * @param key a Poly1305 SecretKey
     * @return returns true if the key is clamped, false if not
     */
    public static boolean isClamped(byte[] key) {
        boolean isClamped;
        isClamped = ((key[19] & 0xF0) == 0) && ((key[23] & 0xF0) == 0) &&
                ((key[27] & 0xF0) == 0) && ((key[31] & 0xF0) == 0);
        isClamped = isClamped && ((key[20] & 0x03) == 0) && ((key[24] & 0x03) == 0) &&
                ((key[28] & 0x03) == 0);
        return isClamped;
    }

    /**
     * Generates encrypted test vectors.
     *
     * @param keys list of GroupKeys
     * @return returns an array of encrypted packets. Result.length == keys.size();
     */
    public static byte[][] generateCipherTexts(List<SecretKey> keys) {
        INonceGenerator generator = PublicMessageCipherFactory.getNonceGenerator(ivLengthMAC);
        IComputeMAC mac;
        try {
            mac = ComputeMacFactory.getInstance();
        } catch (Exception e){
            mac = null;
        }
        PublicMessageEncryption pme = setUpPublicMessageEncryptor(generator, mac);
        return pme.encrypt(getByteInput(), keys);
    }


}
