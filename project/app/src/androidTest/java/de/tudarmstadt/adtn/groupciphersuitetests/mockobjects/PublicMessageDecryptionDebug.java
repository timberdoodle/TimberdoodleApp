package de.tudarmstadt.adtn.groupciphersuitetests.mockobjects;

import java.util.Collection;

import javax.crypto.SecretKey;

import de.tudarmstadt.adtn.ciphersuite.utils.GroupKey;
import de.tudarmstadt.adtn.ciphersuite.ciphers.IPublicMessageCipher;
import de.tudarmstadt.adtn.ciphersuite.hashes.IComputeMAC;

/**
 * A mock object that returns the tried key.
 */
public class PublicMessageDecryptionDebug {

    private final int macOffset;
    private final int nonceOffset;
    private final int textOffset;
    private final int plainSize;
    private final int nonceLength;
    private IComputeMAC calcMAC;
    private IPublicMessageCipher cipher;
    public int keyIndex = 0;

    /**
     * @param cmac        An instance of IComputeMAC that can calculate message authentication codes
     * @param cipher      An IPublicMessageCipher instance that is set to decrypt something
     * @param nonceLength The current length of the nonce
     * @param macOff      the current starting index of the cipher text's MAC
     * @param nonceOff    the current starting index of the cipher text's nonce
     * @param textOff     the current starting index of the cipher text's payload
     * @param plainSize   size of the payload
     */
    public PublicMessageDecryptionDebug(IComputeMAC cmac, IPublicMessageCipher cipher, int nonceLength, int macOff, int nonceOff, int textOff, int plainSize) {
        this.calcMAC = cmac;
        this.cipher = cipher;
        this.nonceLength = nonceLength;
        this.macOffset = macOff;
        this.nonceOffset = nonceOff;
        this.textOffset = textOff;
        this.plainSize = plainSize;
    }

    /**
     * compares two macs with the first byte array being a single mac and the second byte
     * array being a whole cipher text with added header. The mac in the cipher text starts at offset.
     *
     * @param mac        Single mac.
     * @param ciphertext Whole cipher text
     * @param offset     Start index of the mac in the cipher text.
     * @return returns true if the macs are equal, else false.
     */
    private boolean macsEqual(byte[] mac, byte[] ciphertext, int offset) {
        boolean result = false;
        for (int i = 0; i < mac.length; ++i) {
            result = mac[i] == ciphertext[i + offset];
            if(!result) break;
        }
        return result;
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
     * Tries to decrypt the passed in ciphertext. If it cannot be decrypted
     * with a key in the passed in list, null is returned.
     *
     * @param ciphertext cipher text that gets decrypted
     * @param keys       keys that will be tried for decryption
     * @return returns the decrypted byte array that is also cut down to
     * the actual payload
     * @throws Exception
     */
    public byte[] decrypt(byte[] ciphertext, Collection<SecretKey> keys) {
        byte[] result = null;
        byte[] mac = new byte[calcMAC.length()];
        byte[] iv = new byte[nonceLength];
        keyIndex = 0;
        //copy the nonce from the input
        System.arraycopy(ciphertext, nonceOffset, iv, 0, nonceLength);
        //for every key
        for (SecretKey key : keys) {
            //calculate the mac of the cipher text
            calcMAC.computeMAC(iv, ((GroupKey) key).getMACKey(), ciphertext, textOffset, mac, 0);
            //if macs equal, the right key was found
            if (macsEqual(mac, ciphertext, macOffset)) {
                result = new byte[plainSize];
                //get the cipher nonce
                byte[] cipherIV = getCipherIV(iv);
                //decrypt the packet
                cipher.doFinalOptimized(cipherIV, ((GroupKey) key).getCipherKey(), ciphertext, textOffset, result, 0);
                break;
            }
            keyIndex++;
        }
        return result;
    }
}
