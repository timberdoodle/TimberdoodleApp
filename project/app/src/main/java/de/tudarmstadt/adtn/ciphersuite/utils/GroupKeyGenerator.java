package de.tudarmstadt.adtn.ciphersuite.utils;

import java.security.Provider;

import javax.crypto.SecretKey;

/**
 * Generates a group key object
 */
public class GroupKeyGenerator implements ISymmetricKeyGenerator {

    private ISymmetricKeyGenerator cipherGen;
    private ISymmetricKeyGenerator macGen;

    public GroupKeyGenerator(String cipherAlgorithm, int keyLengthCipher,
                             String macAlgorithm, int keyLengthMac) {
        Provider provider = new org.spongycastle.jce.provider.BouncyCastleProvider();
        cipherGen = new SymmetricKeyGenerator(provider, cipherAlgorithm, keyLengthCipher);
        macGen = new SymmetricKeyGenerator(provider, macAlgorithm, keyLengthMac);
    }

    /**
     * Generates a group key
     * @return returns a GroupKey instance
     */
    public SecretKey generateKey() {
        return new GroupKey(cipherGen.generateKey(), macGen.generateKey());
    }

    /**
     * Converts a byte array to a GroupKey
     * @param keyBytes bytes that represent the GroupKey. This method does not check
     *                 the constraints of the GroupKey, which means the conformity of
     *                 the bytes have to be checked beforehand.
     * @return Returns a GroupKey instance
     */
    public SecretKey readKeyFromByteArray(byte[] keyBytes){
        byte[] cipherKey = new byte[cipherGen.getLength()];
        byte[] macKey = new byte[macGen.getLength()];
        System.arraycopy(keyBytes, 0, cipherKey, 0, cipherGen.getLength());
        System.arraycopy(keyBytes, cipherGen.getLength(), macKey, 0, macGen.getLength());
        return new GroupKey(cipherGen.readKeyFromByteArray(cipherKey),
                macGen.readKeyFromByteArray(macKey));
    }

    /**
     * Returns the length of a GroupKey in bytes
     * @return
     */
    public int getLength(){
        return cipherGen.getLength() + macGen.getLength();
    }
}
