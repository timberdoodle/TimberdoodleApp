package de.tudarmstadt.adtn.ciphersuite.ciphers;

/**
 * Enum to differ between encryption and decryption.
 * (Used mainly to split the ciphersuites logic from API logic)
 */
public enum Ciphermode {
    ENCRYPT, DECRYPT
}
