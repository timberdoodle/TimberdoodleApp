package de.tudarmstadt.adtn.groupciphersuitetests;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

import junit.framework.Assert;
import junit.framework.ComparisonFailure;

import org.spongycastle.util.encoders.Hex;

import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import de.tudarmstadt.adtn.ciphersuite.GroupCipherSuite;
import de.tudarmstadt.adtn.ciphersuite.IGroupCipher;
import de.tudarmstadt.adtn.ciphersuite.PublicMessageEncryption;
import de.tudarmstadt.adtn.ciphersuite.utils.ISymmetricKeyGenerator;
import de.tudarmstadt.adtn.ciphersuite.utils.IGroupKey;
import de.tudarmstadt.adtn.ciphersuite.utils.SymmetricKeyGenerator;
import de.tudarmstadt.adtn.ciphersuite.ciphers.IPublicMessageCipher;
import de.tudarmstadt.adtn.ciphersuite.ciphers.PublicMessageCipherFactory;
import de.tudarmstadt.adtn.ciphersuite.hashes.ComputeMacFactory;
import de.tudarmstadt.adtn.ciphersuite.hashes.IComputeMAC;
import de.tudarmstadt.adtn.ciphersuite.hashes.Poly1305;
import de.tudarmstadt.adtn.groupciphersuitetests.utils.CipherSuiteTestsUtility;
import de.tudarmstadt.adtn.utility.FileWriter;
import de.tudarmstadt.adtn.groupciphersuitetests.mockobjects.PublicMessageDecryptionDebug;
import de.tudarmstadt.adtn.groupciphersuitetests.utils.CipherTestVectors;
import static de.tudarmstadt.adtn.TestUtility.*;

/**
 * Stress test for the group cipher suite
 */
public class CipherStressTests extends AndroidTestCase {

    private static String path = "./sdcard/Download/CipherStressTestsReport.txt";
    private IGroupCipher uut = new GroupCipherSuite(CipherSuiteTestsUtility.PLAINSIZE);
    private StringBuilder outputs;
    private List<SecretKey> keyList = CipherSuiteTestsUtility.generateGroupKeys(stressTestAmount);
    private byte[][] encryptedInput= CipherSuiteTestsUtility.generateCipherTexts(keyList);
    private boolean storeSetup = true;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if(CipherSuiteTestsUtility.logReport) {
            if (storeSetup) {
                FileWriter.delete(path);
                outputs = new StringBuilder();
                appendSetUp(outputs);
                FileWriter.write(outputs.toString(), path);
                storeSetup = false;
            }
            outputs = new StringBuilder();
        }
    }

    /**
     * Report message for failed group cipher tests
     *
     * @param groupKey a GroupKey
     * @param encrypted a packet
     * @param result output from a test
     * @param withMac true if mac is written into the report              
     */
    private void appendGroupInfo(SecretKey groupKey, byte[] encrypted, byte[] result, boolean withMac) {
        StringBuilder sb = new StringBuilder();
        sb.append("Decryption failed with the following data: ").append(CipherSuiteTestsUtility.ls);
        sb.append("The used keys are: ").append(CipherSuiteTestsUtility.ls);

        //Append cipher key info
        appendCipherKeyInfo(((IGroupKey) groupKey).getCipherKey(),
                CipherSuiteTestsUtility.getSubArrayFromPacket(encrypted, CipherSuiteTestsUtility.ivOffset, CipherSuiteTestsUtility.ivLengthCipher), sb);


        if (withMac) {
            //Append mac info
            appendMacInfo(((IGroupKey) groupKey).getMACKey(),
                    CipherSuiteTestsUtility.getSubArrayFromPacket(encrypted, CipherSuiteTestsUtility.ivOffset, CipherSuiteTestsUtility.ivLengthMAC),
                    CipherSuiteTestsUtility.getSubArrayFromPacket(encrypted, CipherSuiteTestsUtility.macOffset, CipherSuiteTestsUtility.macLength), sb);
        }

        //Append cipher text info
        byte[] ciphertext = new byte[encrypted.length - CipherSuiteTestsUtility.textOffset];
        System.arraycopy(encrypted, CipherSuiteTestsUtility.textOffset, ciphertext, 0, encrypted.length - CipherSuiteTestsUtility.textOffset);
        appendTextInfo("The cipher text is : ", ciphertext, sb, true);

        //Append input info
        appendTextInfo("The input is : ", encrypted, sb, true);

        //Append result info
        appendTextInfo("The output is : ", result, sb, false);

        outputs.append(sb);
    }

    /**
     * Append info of a cipher key
     * @param key SecretKey that was used
     * @param iv Nonce that was used
     * @param sb StringBuilder that contains the report
     */
    private void appendCipherKeyInfo(SecretKey key, byte[] iv, StringBuilder sb) {
        sb.append("ChaCha key: ").append(Hex.toHexString(key.getEncoded())).append(CipherSuiteTestsUtility.ls);
        sb.append("The used nonce is :").append(Hex.toHexString(iv)).append(CipherSuiteTestsUtility.ls);
    }

    /**
     * Append info of a mac
     * @param key SecretKey that was used
     * @param iv Nonce that was used
     * @param mac Mac that was computed
     * @param sb StringBuilder that contains the report
     */
    private void appendMacInfo(SecretKey key, byte[] iv, byte[] mac, StringBuilder sb) {
        if (key != null) {
            sb.append("Poly key  : ").append(Hex.toHexString(key.getEncoded())).append(CipherSuiteTestsUtility.ls);
            sb.append("The used nonce is :").append(Hex.toHexString(iv)).append(CipherSuiteTestsUtility.ls);
        }
        sb.append("The computed Mac is : ").append(Hex.toHexString(mac)).append(CipherSuiteTestsUtility.ls);
    }

    private void appendTextInfo(String TAG, byte[] text, StringBuilder sb, boolean trueIfHex) {
        sb.append(TAG).append(" : ").append(CipherSuiteTestsUtility.ls);
        if (trueIfHex)
            sb.append(Hex.toHexString(text)).append(CipherSuiteTestsUtility.ls);
        else
            sb.append(new String(text)).append("||°||").append(CipherSuiteTestsUtility.ls);
    }

    private void appendSetUp(StringBuilder sb){
        for(int i = 0; i < stressTestAmount; i++){
            appendDataSplit(sb);
            sb.append(i).append(" : ").append(CipherSuiteTestsUtility.ls);
            appendGroupInfo(keyList.get(i), encryptedInput[i], new byte[1],true);
        }
    }

    private void appendAdditionalKeyInfo(SecretKey key, StringBuilder sb, int i, int j){
        sb.append("Should have used keys at number ").append(i).append(CipherSuiteTestsUtility.ls);
        sb.append("But used keys at ").append(j).append(" :").append(CipherSuiteTestsUtility.ls);
        sb.append("Cipher key : ").append(Hex.toHexString(((IGroupKey) key).getCipherKey().getEncoded())).append(CipherSuiteTestsUtility.ls);
        sb.append("Mac key : ").append(Hex.toHexString(((IGroupKey) key).getMACKey().getEncoded())).append(CipherSuiteTestsUtility.ls);
    }

    private void appendMacKeyInfo(SecretKey key, int i, StringBuilder sb){
        sb.append("Mac key at ").append(i).append(" : ").
                append(Hex.toHexString(key.getEncoded())).append(CipherSuiteTestsUtility.ls);
    }

    /**
     * Appends a test report header to the passed in StringBuilder.
     * The TAG describes the test case's name
     *
     * @param testTAG a descriptive tag
     * @param sb StringBuilder that contains the report
     */
    private void appendHeader(String testTAG, StringBuilder sb) {
        sb.append(testTAG).append(CipherSuiteTestsUtility.ls).
                append("===============================================================================================================================================================================").
                append(CipherSuiteTestsUtility.ls).append(CipherSuiteTestsUtility.ls).append(CipherSuiteTestsUtility.ls);
    }

    /**
     * Appends a report footer
     * @param sb StringBuilder that contains the report
     */
    private void appendFooter(StringBuilder sb) {
        sb.append(CipherSuiteTestsUtility.ls).append(CipherSuiteTestsUtility.ls).
                append("===============================================================================================================================================================================").
                append(CipherSuiteTestsUtility.ls).append(CipherSuiteTestsUtility.ls).append(CipherSuiteTestsUtility.ls);
    }

    /**
     * Appends //=====================//
     * @param sb StringBuilder that contains the report
     */
    private void appendDataSplit(StringBuilder sb) {
        sb.append(CipherSuiteTestsUtility.ls).append("//=====================//").append(CipherSuiteTestsUtility.ls).append(CipherSuiteTestsUtility.ls);
    }


    /**
     * Test if group cipher can handle high amounts of tasks and still work properly
     *
     * @throws Exception
     */
    public void testGroupCipher() throws Exception {
        if(CipherSuiteTestsUtility.logReport)
            appendHeader("GroupCipherTest", outputs);
        ComparisonFailure ex = null;
        //initialise input
        byte[] plaintext = CipherTestVectors.getByteInput();
        //start test
        byte[][] result = uut.encrypt(plaintext, keyList);
        for (int i = 0; i < result.length; ++i) {
            byte[] decrypted = uut.tryDecrypt(result[i], keyList);
            try {
                Assert.assertEquals(CipherSuiteTestsUtility.PLAINSIZE, decrypted.length);
                Assert.assertEquals(CipherTestVectors.testInput, new String(decrypted, CipherSuiteTestsUtility.charEncoding).trim());
            } catch (ComparisonFailure e) {
                if(CipherSuiteTestsUtility.logReport) {
                    appendDataSplit(outputs);
                    appendGroupInfo(keyList.get(i), result[i], decrypted, true);
                }
                ex = e;
            }
        }
        if (ex != null) throw ex;
    }

    /**
     * Test if public message cipher can handle high amounts of tasks and still work properly
     *
     * @throws Exception
     */
    @LargeTest
    public void testPublicMessageCipher() throws Exception {
        ComparisonFailure ex = null;
        //initialise and get data and units under test
        List<SecretKey> keyList = CipherSuiteTestsUtility.getSpecificKeysFromGroupKeyList(this.keyList, true);
        List<byte[]> nonceList = CipherSuiteTestsUtility.generateNonceList(stressTestAmount, CipherSuiteTestsUtility.ivLengthCipher);
        byte[] result = new byte[CipherSuiteTestsUtility.PLAINSIZE];
        byte[] buffer = new byte[CipherSuiteTestsUtility.PLAINSIZE];
        IPublicMessageCipher decryption = CipherSuiteTestsUtility.setUpPublicMessageCipher(Cipher.DECRYPT_MODE);
        IPublicMessageCipher encryption= CipherSuiteTestsUtility.setUpPublicMessageCipher(Cipher.ENCRYPT_MODE);
        //start the test
        for (int i = 0; i < keyList.size(); ++i) {
            assert encryption != null;
            encryption.doFinalOptimized(nonceList.get(i), keyList.get(i), CipherTestVectors.getByteInput(), 0, buffer, 0);
            assert decryption != null;
            decryption.doFinalOptimized(nonceList.get(i), keyList.get(i), buffer, 0, result, 0);
            try {
                Assert.assertEquals(CipherSuiteTestsUtility.PLAINSIZE, result.length);
                Assert.assertEquals(CipherTestVectors.testInput, new String(result));
            } catch (ComparisonFailure e) {
                ex = e;
            }
        }
        if (ex != null) throw ex;
    }

    /**
     * Test if PublicMessageEncryption and PublicMessageDecryption can handle high amounts of
     * data and work properly
     * @throws Exception
     */
    public void testEncryptionVSDecryption() throws Exception {
        ComparisonFailure ex = null;
        if(CipherSuiteTestsUtility.logReport)
            appendHeader("Encryption vs Decryption test", outputs);
        //initialise units under test and data
        PublicMessageEncryption pme = CipherSuiteTestsUtility.setUpPublicMessageEncryptor(PublicMessageCipherFactory.getNonceGenerator(CipherSuiteTestsUtility.ivLengthMAC), ComputeMacFactory.getInstance());
        PublicMessageDecryptionDebug pmd = CipherSuiteTestsUtility.setUpPublicMessageDecryptionDebug(ComputeMacFactory.getInstance());
        //start test
        assert pme != null;
        byte[][] buffer = pme.encrypt(CipherTestVectors.getByteInput(), keyList);
        for (int j = 0; j < buffer.length; j++) {
            assert pmd != null;
            byte[] result = pmd.decrypt(buffer[j], keyList);
            try {
                Assert.assertEquals(CipherSuiteTestsUtility.PLAINSIZE, result.length);
                Assert.assertEquals(CipherTestVectors.testInput, new String(result, CipherSuiteTestsUtility.charEncoding));
            } catch (ComparisonFailure e) {
                if (CipherSuiteTestsUtility.logReport) {
                    appendDataSplit(outputs);
                    appendGroupInfo(keyList.get(j), buffer[j], result, true);
                    appendAdditionalKeyInfo(keyList.get(pmd.keyIndex), outputs, j, pmd.keyIndex);
                }
                ex = e;
            }
        }
        if (ex != null) throw ex;
    }

    /**
     * Test if PublicMessageDecryption can handle high amounts of data and work properly
     * @throws Exception
     */
    public void testDecryption() throws Exception {
        ComparisonFailure ex = null;
        if(CipherSuiteTestsUtility.logReport)
            appendHeader("Test decryption", outputs);
        //initialise and get data and unit under test
        byte[][] input = encryptedInput;
        PublicMessageDecryptionDebug uut = CipherSuiteTestsUtility.setUpPublicMessageDecryptionDebug(ComputeMacFactory.getInstance());
        //start test
        for (int i = 0; i < input.length; i++) {
            byte[] result = new byte[0];
            try {
                assert uut != null;
                Log.d("decrypt test", "I'm at packet " + i);
                result = uut.decrypt(input[i], keyList);
                Assert.assertEquals(CipherSuiteTestsUtility.PLAINSIZE, result.length);
                Assert.assertEquals(CipherTestVectors.testInput, new String(result, CipherSuiteTestsUtility.charEncoding));
            } catch (ComparisonFailure e) {
                if (CipherSuiteTestsUtility.logReport) {
                    appendDataSplit(outputs);
                    appendGroupInfo(keyList.get(i), input[i], result, true);
                    appendAdditionalKeyInfo(keyList.get(uut.keyIndex), outputs, i, uut.keyIndex);
                }
                ex = e;
            } catch (Exception e){
                throw e;
            }
        }
        if (ex != null) throw ex;
    }

    /**
     * Test if PublicMessageEncryption can handle high amounts of data and work properly
     * @throws Exception
     */
    public void testEncryption() throws Exception {
        ComparisonFailure ex = null;
        if(CipherSuiteTestsUtility.logReport)
            appendHeader("Test encryption", outputs);
        //initialise and get data and unit under test
        PublicMessageEncryption pme =
                CipherSuiteTestsUtility.setUpPublicMessageEncryptor(
                        PublicMessageCipherFactory.getNonceGenerator(CipherSuiteTestsUtility.ivLengthMAC),
                        ComputeMacFactory.getInstance());
        //start test
        assert pme != null;
        byte[][] result = pme.encrypt(CipherTestVectors.getByteInput(), keyList);
        //test if output can be decrypted at all
        IPublicMessageCipher pmc = CipherSuiteTestsUtility.setUpPublicMessageCipher(Cipher.DECRYPT_MODE);
        List<SecretKey> cipherKeys = CipherSuiteTestsUtility.getSpecificKeysFromGroupKeyList(keyList, true);
        byte[] decrypted = new byte[CipherSuiteTestsUtility.PLAINSIZE];
        for (int i = 0; i < stressTestAmount; i++) {
            try {
                assert pmc != null;
                pmc.doFinalOptimized(CipherSuiteTestsUtility.getSubArrayFromPacket(result[i], CipherSuiteTestsUtility.ivOffset, CipherSuiteTestsUtility.ivLengthCipher),
                        cipherKeys.get(i), result[i], CipherSuiteTestsUtility.textOffset, decrypted, 0);
                Assert.assertEquals(CipherTestVectors.testInput, new String(decrypted, CipherSuiteTestsUtility.charEncoding));
            } catch (ComparisonFailure e) {
                if(CipherSuiteTestsUtility.logReport) {
                    appendDataSplit(outputs);
                    appendGroupInfo(keyList.get(i), result[i], decrypted, false);
                }
                ex = e;
            }
        }
        if (ex != null) throw ex;
    }

    /**
     * Test if Poly1305 can handle high amounts of data and still function properly
     */
    public void testPoly1305RAW() throws Exception {
        Poly1305 uut = new Poly1305();
        List<byte[]> nonces = CipherSuiteTestsUtility.generateNonceList(stressTestAmount, CipherSuiteTestsUtility.ivLengthMAC);
        List<SecretKey> keys = CipherSuiteTestsUtility.getSpecificKeysFromGroupKeyList(keyList, false);
        byte[][] results = new byte[stressTestAmount][uut.length()];
        for (int i = 0; i < stressTestAmount; i++) {
            uut.computeMAC(nonces.get(i), keys.get(i), CipherTestVectors.getByteInput(), 0, results[i], 0);
        }
        boolean compare = false;
        for (int i = 0; i < results.length; ++i)
            for (int j = i + 1; j < results.length; ++j) {
                compare = compare || bytesEqual(results[i], results[j], CipherSuiteTestsUtility.macOffset);
            }
        assertFalse(compare);
    }

    /**
     * Test if two different mac operations compute the same mac
     */
    public void testTwoDifferentKeysDoNotProduceSameMac() throws Exception{
        if(CipherSuiteTestsUtility.logReport)
            appendHeader("Two different mac key produce same mac", outputs);
        AssertionError ex = null;
        List<SecretKey> keys = CipherSuiteTestsUtility.getSpecificKeysFromGroupKeyList(keyList, false);
        IComputeMAC mac = ComputeMacFactory.getInstance();
        byte[] computedMac;
        byte[] computedNonce;
        byte[] currentResult = new byte[CipherSuiteTestsUtility.macLength];
        byte[] currentNonce = new byte[CipherSuiteTestsUtility.ivLengthMAC];
        for(int i = 0; i < stressTestAmount; i++){
            computedMac = CipherSuiteTestsUtility.getSubArrayFromPacket(encryptedInput[i], CipherSuiteTestsUtility.macOffset, CipherSuiteTestsUtility.macLength);
            computedNonce = CipherSuiteTestsUtility.getSubArrayFromPacket(encryptedInput[i], CipherSuiteTestsUtility.ivOffset, CipherSuiteTestsUtility.ivLengthMAC);
            for(int j = 0; j < stressTestAmount; j++){
                try {
                    if (j != i) {
                        currentNonce = CipherSuiteTestsUtility.getSubArrayFromPacket(encryptedInput[j], CipherSuiteTestsUtility.ivOffset, CipherSuiteTestsUtility.ivLengthMAC);
                        mac.computeMAC(currentNonce, keys.get(j), encryptedInput[j],
                                CipherSuiteTestsUtility.textOffset, currentResult, CipherSuiteTestsUtility.macOffset);
                        assertFalse(bytesEqual(computedMac, currentResult, 0));
                    }
                } catch(AssertionError e){
                    if(CipherSuiteTestsUtility.logReport) {
                        appendDataSplit(outputs);
                        outputs.append("Hit at : (").append(i).append(",").append(j).append(")").append(CipherSuiteTestsUtility.ls);
                        outputs.append("Original packet and mac key info: ").append(CipherSuiteTestsUtility.ls);
                        appendTextInfo("Packet is: ", encryptedInput[i], outputs, true);
                        appendMacInfo(keys.get(i), computedNonce, computedMac, outputs);
                        appendMacInfo(keys.get(j), currentNonce, currentResult, outputs);
                    }
                    ex = e;
                }
            }
        }
        if(ex != null) throw ex;
    }

    /**
     * Check the mac collisions in the test input
     * @throws Exception
     */
    public void testMacCollisions() throws Exception  {
        if(CipherSuiteTestsUtility.logReport)
            appendHeader("Mac collisions",outputs);
        ComparisonFailure ex = null;
        byte[][] input = CipherSuiteTestsUtility.getSubArraysFromPacketArray(encryptedInput, CipherSuiteTestsUtility.macOffset, CipherSuiteTestsUtility.macLength);
        for(int j = 0; j < stressTestAmount - 1; j ++)
           for(int k = j+1; k < stressTestAmount; k++)
               try {
                   assertFalse(bytesEqual(input[j], input[k], 0));
               } catch (ComparisonFailure e){
                   if(CipherSuiteTestsUtility.logReport) {
                       appendDataSplit(outputs);
                       appendGroupInfo(keyList.get(j), encryptedInput[j], new byte[1], true);
                       appendGroupInfo(keyList.get(k), encryptedInput[k], new byte[1], true);
                   }
                   ex = e;
               }
        if(ex != null) throw ex;
    }

    /**
     * Check if any generated mac keys are equal
     */
    public void testEqualMacKeys() {
        if(CipherSuiteTestsUtility.logReport)
            appendHeader("Mac key equals", outputs);
        ComparisonFailure ex = null;
        List<SecretKey> keys = CipherSuiteTestsUtility.getSpecificKeysFromGroupKeyList(keyList, false);
        for(int j = 0; j < stressTestAmount - 1; j ++)
            for(int k = j+1; k < stressTestAmount; k++)
                try {
                    assertFalse(bytesEqual(keys.get(j).getEncoded(), keys.get(k).getEncoded(), 0));
                } catch (ComparisonFailure e){
                    if(CipherSuiteTestsUtility.logReport) {
                        appendDataSplit(outputs);
                        appendMacKeyInfo(keys.get(j), j, outputs);
                        appendMacKeyInfo(keys.get(k), k, outputs);
                    }
                    ex = e;
                }
        if(ex != null) throw ex;
    }

    /**
     * Test if Poly1305 key generation works if it is invoked many times.
     *
     * @throws Exception
     */
    public void testPolyKeyGen() throws Exception {
        ISymmetricKeyGenerator uut = new SymmetricKeyGenerator(CipherSuiteTestsUtility.provider, CipherSuiteTestsUtility.macAlgorithm, CipherSuiteTestsUtility.keyLength*8);
        for (int i = 0; i < stressTestAmount; i++) {
            byte[] key = uut.generateKey().getEncoded();
            assertEquals(32, key.length);
            assertTrue(CipherSuiteTestsUtility.isClamped(key));
        }
    }

    /**
     * Test if ChaCha key generation works if it is invoked many times.
     *
     * @throws Exception
     */
    public void testChaChaKeyGen() throws Exception {
        ISymmetricKeyGenerator uut = new SymmetricKeyGenerator(CipherSuiteTestsUtility.provider, CipherSuiteTestsUtility.macAlgorithm, CipherSuiteTestsUtility.keyLength*8);
        for (int i = 0; i < stressTestAmount; i++) {
            byte[] key = uut.generateKey().getEncoded();
            assertEquals(32, key.length);
        }
    }

    /**
     * Test if GroupKey generation works if it is invoked many times.
     *
     * @throws Exception
     */
    public void testGroupKeyGeneration() throws Exception {
        for (int i = 0; i < stressTestAmount; i++) {
            IGroupKey key = (IGroupKey) uut.generateKey();
            Assert.assertEquals(CipherSuiteTestsUtility.keyLength, key.getCipherKey().getEncoded().length);
            Assert.assertEquals(CipherSuiteTestsUtility.keyLength, key.getMACKey().getEncoded().length);
            assertTrue(CipherSuiteTestsUtility.isClamped(key.getMACKey().getEncoded()));
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (outputs != null && CipherSuiteTestsUtility.logReport) {
            if(outputs.length() > 0) {
                appendFooter(outputs);
                FileWriter.write(outputs.toString(), path);
                outputs = null;
            }
        }
    }
}
