package de.tudarmstadt.adtn.ciphersuite;

import java.util.Collection;

import javax.crypto.SecretKey;

import de.tudarmstadt.adtn.ciphersuite.utils.IGroupKey;
import de.tudarmstadt.adtn.ciphersuite.ciphers.INonceGenerator;
import de.tudarmstadt.adtn.ciphersuite.ciphers.IPublicMessageCipher;
import de.tudarmstadt.adtn.ciphersuite.hashes.IComputeMAC;

/**
 * Tries to encrypt a byte array with every passed in key.
 */
public class PublicMessageEncryption {

    private final int macOffset;
    private final int nonceOffset;
    private final int textOffset;
    private final int cipherSize;
    private IComputeMAC calcMAC;
    private IPublicMessageCipher cipher;
    private INonceGenerator nonceGenerator;

    /**
     * @param cmac       An instance of IComputeMAC that can calculate message authentication codes
     * @param cipher     An IPublicMessageCipher instance that is set to decrypt something
     * @param nG         An INonceGenerator instance to generate a nonce
     * @param macOff     the current starting index of the cipher text's MAC
     * @param nonceOff   the current starting index of the cipher text's nonce
     * @param textOff    the current starting index of the cipher text's payload
     * @param cipherSize the current cipher text size
     */
    public PublicMessageEncryption(IComputeMAC cmac, IPublicMessageCipher cipher, INonceGenerator nG, int macOff, int nonceOff, int textOff, int cipherSize) {
        this.calcMAC = cmac;
        this.cipher = cipher;
        this.nonceGenerator = nG;
        this.macOffset = macOff;
        this.nonceOffset = nonceOff;
        this.textOffset = textOff;
        this.cipherSize = cipherSize;
    }

    /**
     * Reads the cipher iv from the whole iv.
     *
     * @return
     */
    private byte[] getCipherIV(byte[] ivbytes) {
        byte[] result = new byte[cipher.getNonceLength()];
        System.arraycopy(ivbytes, 0, result, 0, result.length);
        return result;
    }

    /**
     * Tries to encrypt a plain text with every key in the passed in list. This method also
     * appends a cipher header to each encrypted plain text.
     *
     * @param plaintext the plain text that is encrypted
     * @param keys      Collection of keys that are used for encryption
     * @return Returns an array of the cipher texts (i.e. an array of array of byte)
     */
    public byte[][] encrypt(byte[] plaintext, Collection<SecretKey> keys) {
        byte[][] result = new byte[keys.size()][];
        int i = 0;
        //for every key
        for (SecretKey key : keys) {
            //construct a new array with the correct size
            byte[] resultbuffer = new byte[cipherSize];
            //generate a nonce
            byte[] iv = nonceGenerator.generateNonce();
            //get the nonce for the cipher
            byte[] cipherIV = getCipherIV(iv);
            //encrypt the array
            cipher.doFinalOptimized(cipherIV, ((IGroupKey) key).getCipherKey(), plaintext, 0, resultbuffer, textOffset);
            //compute mac and write it into the array
            calcMAC.computeMAC(iv, ((IGroupKey) key).getMACKey(), resultbuffer, textOffset, resultbuffer, macOffset);
            //write iv into the array
            System.arraycopy(iv, 0, resultbuffer, nonceOffset, iv.length);
            //append encrypted array to the result
            result[i] = resultbuffer;
            ++i;
        }
        return result;
    }
}
