package de.tudarmstadt.timberdoodle.ui.contactmanager;

import android.graphics.Bitmap;

import java.security.PublicKey;

import de.tudarmstadt.adtn.ui.groupmanager.QRReaderWriter;
import de.tudarmstadt.timberdoodle.friendcipher.IFriendCipher;

public class FriendQRReaderWriter extends QRReaderWriter {

    private final static String MAGIC_STRING = "lTQ8C2w4";

    /**
     * Creates a QR code bitmap from a friend key.
     *
     * @param friendCipher The friend cipher object.
     * @param friendKey    The key to create the QR code for.
     * @param width        The preferred width of the bitmap.
     * @param height       The preferred height of the bitmap.
     * @return A bitmap containing the QR code.
     */
    public Bitmap createQrCode(IFriendCipher friendCipher, PublicKey friendKey,
                               int width, int height) {
        return createQRCode(MAGIC_STRING +
                bytesToBase64(friendCipher.publicKeyToByteArray(friendKey)), width, height);
    }

    /**
     * Tries to parse a friend key that was scanned from a QR code.
     *
     * @param code   The QR code data.
     * @param cipher The friend cipher object.
     * @return The parsed key or null if the QR code is invalid.
     */
    public PublicKey parseCode(String code, IFriendCipher cipher) {
        try {
            // Check if magic string of friend key QR code is present
            if (!code.startsWith(MAGIC_STRING)) return null;

            // Check size of code
            final int magicLen = MAGIC_STRING.length();
            final int keyBase64Len = getBase64StringLength(cipher.getEncodedPublicKeySize());
            if (code.length() < magicLen + keyBase64Len) return null;

            // Decode key and return it
            return cipher.byteArrayToPublicKey(base64ToBytes(
                    code.substring(magicLen, magicLen + keyBase64Len)));
        } catch (Exception e) {
            return null; // Report invalid QR code format
        }
    }
}
