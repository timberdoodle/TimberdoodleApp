package de.tudarmstadt.adtn.ui.groupmanager;

import android.graphics.Bitmap;
import android.util.Log;

import org.joda.time.Instant;

import javax.crypto.SecretKey;

import de.tudarmstadt.adtn.ciphersuite.IGroupCipher;

public class GroupQRReaderWriter extends QRReaderWriter {

    private static final String TAG = "GroupQRReaderWriter";
    /**
     * Contains the scanned group key data, i.e. the key itself and its timestamp.
     */
    public static class ScannedGroupKey {
        private SecretKey key;
        private Instant timestamp;

        /**
         * Creates a new ScannedGroupKey object.
         *
         * @param key       The scanned group key.
         * @param timestamp The timestamp of the group key.
         */
        private ScannedGroupKey(SecretKey key, Instant timestamp) {
            this.key = key;
            this.timestamp = timestamp;
        }

        /**
         * @return The scanned group key.
         */
        public SecretKey getKey() {
            return key;
        }

        /**
         * @return The timestamp of the group key.
         */
        public Instant getTimestamp() {
            return timestamp;
        }
    }

    private static final String MAGIC_STRING = "iN6qKVeG";

    /**
     * Creates a QR code bitmap from a group key.
     *
     * @param groupCipher The group cipher object.
     * @param groupKey    The key to create the QR code for.
     * @param width       The preferred width of the bitmap.
     * @param height      The preferred height of the bitmap.
     * @return A bitmap containing the QR code.
     */
    public Bitmap createQrCode(IGroupCipher groupCipher, SecretKey groupKey, Instant timestamp,
                               int width, int height) {
        return createQRCode(MAGIC_STRING +
                bytesToBase64(groupCipher.secretKeyToByteArray(groupKey)) +
                String.format("%016x", timestamp.getMillis()), width, height);
    }

    /**
     * Tries to parse a group key that was scanned from a QR code.
     *
     * @param code   The QR code data.
     * @param cipher The group cipher object.
     * @return The parsed key or null if the QR code is invalid.
     */
    public ScannedGroupKey parseCode(String code, IGroupCipher cipher) {
        try {
            // Check if magic string of group key QR code is present
            if (!code.startsWith(MAGIC_STRING)) {
                return null;
            }

            // Check size of code
            final int magicLen = MAGIC_STRING.length();
            final int keyBase64Len = getBase64StringLength(cipher.getEncodedKeySize());
            final int timestampLen = 16; // Zero-padded hex long
            if (code.length() < magicLen + keyBase64Len + timestampLen) {
                return null;
            }

            // Decode key
            String base64Key = code.substring(magicLen, magicLen + keyBase64Len);
            byte[] keyBytes = base64ToBytes(base64Key);
            SecretKey key = cipher.byteArrayToSecretKey(keyBytes);

            // Parse timestamp
            final int timestampOffset = magicLen + keyBase64Len;
            String hexTimestamp = code.substring(timestampOffset, timestampOffset + timestampLen);
            Instant timestamp = new Instant(Long.parseLong(hexTimestamp, 16));

            return new ScannedGroupKey(key, timestamp);
        } catch (Exception e) {
            Log.w(TAG, e.getMessage(), e);
            return null; // Report invalid QR code format
        }
    }
}
