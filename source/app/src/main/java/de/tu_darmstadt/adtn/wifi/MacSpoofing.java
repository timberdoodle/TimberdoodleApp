package de.tu_darmstadt.adtn.wifi;

import java.io.IOException;
import java.util.Random;

import de.tu_darmstadt.adtn.Utilities;

/**
 * Static class for Wi-Fi MAC spoofing.
 */
public class MacSpoofing {

    private final static String FILENAME = "/data/.nvmac.info";

    // Returns a random MAC address
    private static String createRandomMac() {
        Random rnd = new Random();
        char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F'};
        char[] secondNibbleDigits = new char[]{'2', '6', 'A', 'E'};

        char[] MAC = new char[17];
        for (int i = 0; i < MAC.length; ++i) {
            if (i == 1) {
                MAC[i] = secondNibbleDigits[rnd.nextInt(secondNibbleDigits.length)];
            } else if (i % 3 == 2) {
                MAC[i] = ':';
            } else {
                MAC[i] = hexDigits[rnd.nextInt(hexDigits.length)];
            }
        }

        return new String(MAC);
    }

    /**
     * Tries to set a random Wi-Fi MAC address.
     */
    public static void trySetRandomMac() {
        String command = "printf " + createRandomMac() + " > " + FILENAME;
        try {
            Utilities.runAsRoot(command);
        } catch (IOException e) {
            // Ignore
        }
    }

    /**
     * Tries to disable Wi-Fi MAC spoofing.
     */
    public static void tryDisable() {
        try {
            Utilities.runAsRoot("rm -f " + FILENAME);
        } catch (IOException e) {
            // Ignore
        }
    }
}
