package de.tudarmstadt.adtn;

import android.app.Instrumentation;

import org.spongycastle.util.encoders.Hex;

import java.io.UnsupportedEncodingException;

/**
 * Utility class for test cases.
 */
public class TestUtility{

    public static int stressTestAmount = 256;

    /**
     * Transforms a given byte array into a string where the
     * bytes are presented in hex format. The bytes are treated
     * as unsigned bytes.
     *
     * @param sequence a byte sequence
     * @return returns the byte sequence in a hex string
     */
    public static String toHex(byte... sequence) {
        return Hex.toHexString(sequence);
    }

    /**
     * Checks if byte sequences are equal
     *
     * @param someBytes  some bytes, that are not longer than ciphertext
     * @param ciphertext a cipher text
     * @param offset     start index in cipher text
     * @return returns true iff someBytes are a sub array of cipher text
     */
    public static boolean bytesEqual(byte[] someBytes, byte[] ciphertext, int offset) {
        boolean result = true;
        for (int i = 0; i < someBytes.length; ++i) {
            result = result && (someBytes[i] == ciphertext[i + offset]);
            if(!result) break;
        }
        return result;
    }

    /**
     * Parses a String to a byte array.
     * @param array The String representation of the byte array.
     *              The String is the byte array in hex representation (without spaces)
     * @return Returns the byte array.
     */
    public static byte[] parseByteArrayFromPlainString(String array){
        return Hex.decode(array);
    }

    /**
     * Loads a string array from the resources. The array is determined by id.
     * @param instrumentation the current instrumentation object
     * @param id id of the array
     * @return returns the string array
     */
    public static String[] loadStringArrayFromResources(Instrumentation instrumentation, int id){
        return instrumentation.getContext().getResources().getStringArray(id);
    }

    /**
     * Loads a string array that represents a byte array (in hex code) from the resources.
     * The array is determined by id
     * @param instrumentation the current instrumentation object
     * @param id id of the array
     * @return returns an array of array of byte
     */
    public static byte[][] loadByteInputFromHexResources(Instrumentation instrumentation, int id){
        String[] res = loadStringArrayFromResources(instrumentation, id);
        byte[][] result = new byte[res.length][];
        for(int i = 0; i < res.length; i++) result[i] = parseByteArrayFromPlainString(res[i]);
        return result;
    }

    public static byte[] loadStringResourceAsUTF8EncodedByteArray(Instrumentation instrumentation, int id){
        String res = instrumentation.getContext().getResources().getString(id);
        try{
            return res.getBytes("UTF8");
        } catch (UnsupportedEncodingException e){
            return null;
        }
    }

}
