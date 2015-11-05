package de.tu_darmstadt.adtn.ciphersuite.ciphers;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;

/**
 * Implements IPublicMessageCipher with a initialised cipher instance. Thus this
 * class only implements the calls to the cipher instance and has no influence on
 * the encryption algorithm that is actually used. The instance of the cipher class that is
 * used, has to be instantiated by the caller (who also sets the cipher parameters. See
 * JavaDoc of the constructor)
 */
public class PublicMessageCipherImpl implements IPublicMessageCipher {

    //Cipher instance
    private Cipher cipher = null;
    //Encryption or decryption mode
    private int mode;
    private int ivLength;


    /**
     * Constructor of this class. Cipher has to be a initialised cipher instance (which means
     * Cipher.getInstance(String transformation, Provider provider) has already been called and
     * the returned instance is passed in as argument).
     *
     * @param cipher Initialised cipher instance.
     * @param mode   Basically only Cipher.ENCRYPT_MODE and Cipher.DECRYPT_MODE are allowed.
     */
    public PublicMessageCipherImpl(Cipher cipher, int mode, int noncelength) {
        this.cipher = cipher;
        this.mode = mode;
        ivLength = noncelength;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doFinalOptimized(byte[] ivBytes, SecretKey key, byte[] text, int textOffset, byte[] outBuffer, int outOffset) {
        try {
            //int outOff = outOffset;
            AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            cipher.init(mode, key, ivSpec);
            //outOff += cipher.update(text, textOffset, text.length - textOffset, outBuffer, outOffset);
            cipher.doFinal(text, textOffset, text.length - textOffset, outBuffer, outOffset);
        } catch (BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException | InvalidKeyException | ShortBufferException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the nonce length for this specific cipher.
     *
     * @return returns teh legnth in bytes.
     */
    @Override
    public int getNonceLength() {
        return ivLength;
    }
}
