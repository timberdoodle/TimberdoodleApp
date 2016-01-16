package de.tu_darmstadt.adtn.groupciphersuitetests.apis;

import android.test.AndroidTestCase;

import javax.crypto.SecretKey;

import de.tu_darmstadt.adtn.ciphersuite.utils.GroupKey;
import de.tu_darmstadt.adtn.ciphersuite.utils.GroupKeyGenerator;
import de.tu_darmstadt.adtn.ciphersuite.utils.ISymmetricKeyGenerator;
import static de.tu_darmstadt.adtn.groupciphersuitetests.utils.CipherSuiteTestsUtility.*;
import static de.tu_darmstadt.adtn.groupciphersuitetests.utils.CipherTestVectors.*;

/**
 * Tests for Group Key generation
 */
public class GroupKeyGeneratorTests extends AndroidTestCase{

    private ISymmetricKeyGenerator uut = new GroupKeyGenerator(cipherAlgorithm, keyLength*8, macAlgorithm, keyLength*8);

    private boolean macsEqual(byte[] mac, byte[] ciphertext, int offset) {
        for (int i = 0; i < mac.length; ++i) {
            if(!(mac[i] == ciphertext[i + offset])) return false;
        }
        return true;
    }

    public void testGetLength(){
        assertEquals(2*keyLength, uut.getLength());
    }

    public void testKeyGeneration(){
        GroupKey result = (GroupKey) uut.generateKey();
        //Check if algorithm, key length and key format is correct
        assertEquals(macAlgorithm, result.getMACKey().getAlgorithm());
        assertEquals(cipherAlgorithm, result.getCipherKey().getAlgorithm());
        assertTrue(isClamped(result.getMACKey().getEncoded()));
        //Check if length of appended key is correct
        assertEquals(2 * keyLength, result.getEncoded().length);
        byte[] compare = new byte[keyLength];
        //Check if keys can be separated
        System.arraycopy(result.getMACKey().getEncoded(), 0, compare,0,keyLength);
        assertTrue(macsEqual(compare, result.getEncoded(), keyLength));
        System.arraycopy(result.getCipherKey().getEncoded(), 0, compare, 0, keyLength);
        assertTrue(macsEqual(compare, result.getEncoded(), 0));
    }

    public void testReadKeyFromByteArray(){
        SecretKey expected = groupKeyList.get(0);
        byte[] input = expected.getEncoded();
        SecretKey actual = uut.readKeyFromByteArray(input);
        assertEquals(expected, actual);
    }

    public void testEquals(){
        SecretKey reference = groupKeyList.get(0);
        SecretKey different = groupKeyList.get(1);
        assertFalse(reference.equals(false));
        assertEquals(reference, reference);
        assertFalse(reference.equals(different));
    }
}
