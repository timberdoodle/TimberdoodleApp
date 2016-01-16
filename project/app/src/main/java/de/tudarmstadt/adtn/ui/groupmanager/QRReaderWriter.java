package de.tudarmstadt.adtn.ui.groupmanager;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Provides methods for reading bytes from / writing bytes to a QR code using ZXing.
 */
public class QRReaderWriter {

    private final static int BASE64_FLAGS = Base64.NO_PADDING | Base64.NO_WRAP;

    /**
     * Launches the ZXing QR code scanner.
     *
     * @param fragment The fragment that uses the scanner.
     * @param prompt   The prompt message.
     */
    public void initiateScan(Fragment fragment, @Nullable String prompt) {
        IntentIntegrator.forSupportFragment(fragment)
                .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES)
                .setPrompt(prompt)
                .setResultDisplayDuration(5)
                .setCameraId(0)
                .autoWide()
                .initiateScan();
    }

    /**
     * Creates a QR code bitmap with the specified dimensions from the specified string.
     *
     * @param content The data the QR code should represent.
     * @param width   The preferred width of the bitmap.
     * @param height  The preferred height of the bitmap.
     * @return The bitmap containing the QR code.
     */
    protected Bitmap createQRCode(String content, int width, int height) {
        // Create BitMatrix from content string
        BitMatrix bitMatrix;
        QRCodeWriter writer = new QRCodeWriter();
        try {
            bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height);
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }

        // Create bitmap from BitMatrix
        width = bitMatrix.getWidth();
        height = bitMatrix.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }

        return bitmap;
    }

    /**
     * Converts the specified bytes to a Base64 string.
     *
     * @param bytes The bytes to convert.
     * @return A Base64 string representing the specified bytes.
     */
    protected String bytesToBase64(byte[] bytes) {
        return Base64.encodeToString(bytes, BASE64_FLAGS);
    }

    /**
     * Converts a Base64 string to a byte array.
     *
     * @param base64 The Base64 encoded bytes.
     * @return A byte array representing the bytes of the specified Base64 string.
     */
    protected byte[] base64ToBytes(String base64) {
        return Base64.decode(base64, BASE64_FLAGS);
    }

    /* Calculates the number of characters in a base64 encoded string without padding and line
     * breaks if it encodes the specified number of bytes. */
    protected int getBase64StringLength(int numBytes) {
        final int BASE64_BITS_PER_CHAR = 6;
        return (numBytes * 8 + BASE64_BITS_PER_CHAR - 1) / BASE64_BITS_PER_CHAR;
    }
}
