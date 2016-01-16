package de.tudarmstadt.keymanager;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import de.tudarmstadt.adtn.ui.groupmanager.ChecksumGenerator;

public class ChecksumGeneratorTests extends AndroidTestCase {

    @SmallTest
    public void test() {
        final byte[] testData1 = new byte[]{1, 2, 3, 4};
        final byte[] testData2 = new byte[]{5, 9, 30, 14, 123, (byte) 255, (byte) 234, (byte) 456};

        // Generate checksum of different data and assume checksum is not the same
        ChecksumGenerator checksumGenerator = new ChecksumGenerator();
        long testData1Checksum = checksumGenerator.generate(testData1);
        long testData2Checksum = checksumGenerator.generate(testData2);
        assertTrue(testData1Checksum != testData2Checksum);

        // Generate the checksum again, in reverse order and check if they are still correct
        assertEquals(testData2Checksum, checksumGenerator.generate(testData2));
        assertEquals(testData1Checksum, checksumGenerator.generate(testData1));

        // Now recreate the ChecksumGenerator object and check again
        checksumGenerator = new ChecksumGenerator();
        assertEquals(testData1Checksum, checksumGenerator.generate(testData1));
        assertEquals(testData2Checksum, checksumGenerator.generate(testData2));
    }
}
