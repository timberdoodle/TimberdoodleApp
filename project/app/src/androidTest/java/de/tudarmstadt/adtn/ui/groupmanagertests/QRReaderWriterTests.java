package de.tudarmstadt.adtn.ui.groupmanagertests;

import android.graphics.Bitmap;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import org.joda.time.Instant;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

import javax.crypto.SecretKey;

import de.tudarmstadt.adtn.ui.groupmanager.GroupQRReaderWriter;

public class QRReaderWriterTests extends AndroidTestCase {

    private GroupCipherMock cipher;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        cipher = new GroupCipherMock();
    }

    @SmallTest
    public void test1() throws Exception {
        GroupQRReaderWriter groupQrrw = new GroupQRReaderWriter();

        Random rnd = new Random();

        for (int i = 0; i < 100; ++i) {
            // Create random key
            byte[] rawKey = new byte[1 + rnd.nextInt(100)];
            rnd.nextBytes(rawKey);
            // Convert key to QR code bitmap
            cipher.setEncodedKeySize(rawKey.length);
            SecretKey key = cipher.byteArrayToSecretKey(rawKey);
            Instant instant = Instant.now();
            Bitmap bmp = groupQrrw.createQrCode(cipher, key, instant, 200, 200);
            // Scan key
            String decodedQRString = decodePureBitmap(bmp).toString();
            GroupQRReaderWriter.ScannedGroupKey scanned = groupQrrw.parseCode(decodedQRString, cipher);
            assertEquals(instant, scanned.getTimestamp());
            assertEquals(key, scanned.getKey());
        }
    }

    public static Result decodePureBitmap(Bitmap bmp) throws FormatException, ChecksumException, NotFoundException {
        int width = bmp.getWidth(), height = bmp.getHeight();
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        QRCodeReader reader = new QRCodeReader();
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
        return reader.decode(binaryBitmap, hints);
    }
}
