package de.tu_darmstadt.adtn.ciphersuite.hashes;

import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import de.tu_darmstadt.adtn.errorlogger.ErrorLoggingSingleton;

/**
 * Computes a Poly1305 MAC with a given cipher text (as byte array), a given key (key has to match the Poly1305 key format),
 * and a given nonce (initialisation vector). DESPERATELY NEEDS SOME REDOING
 */
public class Poly1305 implements IComputeMAC {

    private Mac messageAuthenticationCode;

    public Poly1305() throws Exception {
        this.messageAuthenticationCode = Mac.getInstance("Poly1305-AES", new org.spongycastle.jce.provider.BouncyCastleProvider());
    }

    /**
     * Computes a message authentication code of a given text (as byte array) with a given
     * key and a nonce (iv).
     *
     * @param iv         Nonce that is used to create MAC.
     * @param key        Key that is used to create MAC.
     * @param text       Text that is used to create MAC.
     * @param textOffset starting index of the text
     * @param buffer     result buffer that will hold the calculated MAC.
     * @param offset     @return Returns the MAC as byte array.
     */
    @Override
    public void computeMAC(byte[] iv, SecretKey key, byte[] text, int textOffset, byte[] buffer, int offset) {
        try {
            AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv);
            messageAuthenticationCode.init(key, ivSpec);
            messageAuthenticationCode.update(text, textOffset, text.length - textOffset);
            messageAuthenticationCode.doFinal(buffer, offset);
        } catch (Exception e) {
            //Error logging
            ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
            log.storeError(ErrorLoggingSingleton.getExceptionStackTraceAsFormattedString(e));
            throw new RuntimeException();
        }
    }

    /**
     * Returns the length of the MAC.
     *
     * @return Length is measured in bytes. Therefore the returned int is the byte count.
     */
    @Override
    public int length() {
        return 16;
    }
}
