package de.tudarmstadt.adtn.ciphersuite.utils;

import java.util.Arrays;

import javax.crypto.SecretKey;

/**
 * Represents a group key to be used with the group cipher.
 */
public class GroupKey implements SecretKey, IGroupKey {

    private final SecretKey cipherKey, macKey;

    public GroupKey(SecretKey cipherKey, SecretKey macKey) {
        this.cipherKey = cipherKey;
        this.macKey = macKey;
    }

    @Override
    public String getAlgorithm() {
        return "ChaCha20Poly1305GroupKey";
    }

    @Override
    public String getFormat() {
        return "RAW";
    }

    @Override
    public byte[] getEncoded() {
        // Concatenate ChaCha and Poly key
        byte[] encodedChaChaKey = cipherKey.getEncoded(), encodedPolyKey = macKey.getEncoded();
        byte[] result = new byte[encodedChaChaKey.length + encodedPolyKey.length];
        System.arraycopy(encodedChaChaKey, 0, result, 0, encodedChaChaKey.length);
        System.arraycopy(encodedPolyKey, 0, result, encodedChaChaKey.length, encodedPolyKey.length);
        return result;
    }

    /**
     * Getter for the cipher key
     *
     * @return
     */
    public SecretKey getCipherKey() {
        return cipherKey;
    }

    /**
     * Getter for the mac key
     *
     * @return
     */
    public SecretKey getMACKey() {
        return macKey;
    }

    /**
     * Compares this instance with the specified object and indicates if they
     * are equal. In order to be equal, {@code o} must represent the same object
     * as this instance using a class-specific comparison. The general contract
     * is that this comparison should be reflexive, symmetric, and transitive.
     * Also, no object reference other than null is equal to null.
     * <p/>
     * <p>The default implementation returns {@code true} only if {@code this ==
     * o}. See <a href="{@docRoot}reference/java/lang/Object.html#writing_equals">Writing a correct
     * {@code equals} method</a>
     * if you intend implementing your own {@code equals} method.
     * <p/>
     * <p>The general contract for the {@code equals} and {@link
     * #hashCode()} methods is that if {@code equals} returns {@code true} for
     * any two objects, then {@code hashCode()} must return the same value for
     * these objects. This means that subclasses of {@code Object} usually
     * override either both methods or neither of them.
     *
     * @param o the object to compare this instance with.
     * @return {@code true} if the specified object is equal to this {@code
     * Object}; {@code false} otherwise.
     * @see #hashCode
     */
    @Override
    public boolean equals(Object o) {
        if(o == null) return false;
        else if(!(o instanceof GroupKey)) return false;
        else {
            GroupKey other = (GroupKey) o;
            return hashCode() == o.hashCode();
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new int[]{macKey.hashCode(), cipherKey.hashCode()});
    }
}
