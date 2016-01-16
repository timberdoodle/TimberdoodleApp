package de.tudarmstadt.adtn.ui.groupmanager;

import java.util.zip.CRC32;

/**
 * Generates a non-cryptographic checksum of a byte array.
 */
public class ChecksumGenerator {
    private final CRC32 crc32;

    /**
     * Creates a new ChecksumGenerator object.
     */
    public ChecksumGenerator() {
        crc32 = new CRC32();
    }

    /**
     * Generates a checksum for the specified data.
     *
     * @param buffer The data to generate a checksum for.
     * @return A checksum.
     */
    public long generate(byte[] buffer) {
        crc32.reset();
        crc32.update(buffer);
        return crc32.getValue();
    }
}
