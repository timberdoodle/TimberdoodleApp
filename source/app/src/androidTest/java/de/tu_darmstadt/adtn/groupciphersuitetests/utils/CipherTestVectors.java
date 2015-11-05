package de.tu_darmstadt.adtn.groupciphersuitetests.utils;

import java.util.Arrays;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import de.tu_darmstadt.adtn.ciphersuite.Utils.GroupKey;

/**
 * Test vectors for the cipher suite tests
 */
public class CipherTestVectors {

    public static final byte[][] nonces = {
            {-87, 111, -18, 22, -115, 60, 109, -67, 93, 23, -123, -12, -78, -57, 54, 36},
            {-16, 96, -100, -47, 58, -23, 8, -77, -56, -70, 30, 1, -73, 44, -87, -43},
            {1, -74, 59, -49, -37, 44, -113, -27, -5, 56, -102, -37, 9, -109, -37, -45},
            {-36, 32, 71, 94, 124, 82, -62, -76, 96, 74, -51, -80, 93, 35, 84, -101},
            {-103, -39, 111, 46, 122, -90, 71, 120, 53, -111, 116, 59, 59, -103, 43, -106},
            {-89, -15, 92, -108, 71, -63, 5, 56, 60, 9, 70, 73, -50, 42, -2, 116},
            {8, 97, 106, -52, -127, 88, 91, 77, 75, -28, -84, 68, 66, -118, -113, 103},
            {-7, -38, 61, 72, 123, -83, 51, -21, 13, -40, -126, -56, 49, 74, 93, -100},
            {-96, 73, 120, -105, 20, -29, 42, -103, 32, -31, 127, -74, -44, -27, -2, 47},
            {-99, 80, -114, -82, 13, 40, -101, 7, -8, -94, 41, -3, 10, -116, -27, -120},
            {-35, 118, 127, -10, 76, 123, -112, -20, -65, 43, -11, 37, 20, -4, -111, 2},
            {106, -35, 49, -35, -108, 104, -117, -50, 101, 19, -12, -119, 103, -2, 77, 12},
            {-81, 29, -104, 75, -34, -84, -41, 83, 70, 64, -119, -65, -14, -73, 11, 120},
            {-79, 20, 87, 109, 56, 96, -32, -77, -50, -106, -54, 59, 14, -25, -20, 95},
            {103, 28, -112, 89, -48, 115, -66, -118, 39, -70, -50, 35, -4, -75, -23, -70},
            {0, 25, -49, -121, 95, 30, 32, 5, 127, -72, 18, -84, -41, 118, -69, 124}
    };

    public static final byte[][] keys = {
            {-98, 71, -87, 70, 60, -39, 101, -39, -124, 72, -3, -90, -85, 92, 50, -46, -53, 86, -67, -66, -29, -69, -123, -58, 74, 5, -105, 63, -96, -68, 80, -72},
            {-15, -38, -76, -12, 52, -31, 61, -94, 96, -78, 67, -99, 97, 114, -91, -27, 98, -25, 6, -123, 119, -119, 67, -6, -17, -30, -107, 39, 40, -21, 21, -9},
            {68, -46, 109, 96, -43, 62, -62, 29, -17, 22, -13, -90, 10, -92, 120, 97, -11, 49, 54, 90, 85, -104, 12, -127, -7, -86, -37, -104, 6, 72, 74, 10},
            {99, -65, -16, -126, -102, 7, 92, 66, 33, -31, -6, 123, -47, -73, 46, 6, -80, 12, -58, 57, -22, -55, -73, -110, -117, -126, 50, -128, -100, -15, -86, 122},
            {-95, -10, -83, 71, -73, 37, 92, 56, -79, -83, 73, 79, -66, -97, 108, 122, 90, 74, 49, -79, -63, -52, -77, 96, -128, 102, -54, -22, 105, 32, 124, 30},
            {116, -58, -9, -29, 26, 57, 48, 32, 105, 65, 77, 103, -43, -30, -9, -121, 91, 22, 24, 49, 6, 127, -53, -101, -117, 96, 95, 4, -37, 61, -26, 105},
            {9, 56, -31, 34, -75, -97, -30, 70, -49, -119, 43, 79, 73, 117, 94, -119, 93, 8, 73, 16, -101, 11, 7, -10, -81, 106, -106, -70, -12, 45, -58, -35},
            {67, -13, 92, 49, -54, 30, 47, -32, -3, -8, -21, 9, 39, 54, -3, 9, -19, -94, -66, 50, -44, 127, -123, -27, -117, 123, -34, -12, 115, -98, -14, -100},
            {-80, 60, -128, -126, -15, 85, -40, 16, -20, -12, -95, 36, -117, 119, -88, -119, 108, -99, 10, 69, -59, 15, 59, 96, -45, -5, -111, -80, 73, 120, 77, 26},
            {-67, -8, 57, -31, 4, 124, 2, 49, 18, 28, -99, 4, 101, -12, -91, -4, 11, 95, -108, -53, 125, 123, 40, 25, -112, -82, 106, -38, -93, 5, -34, 38},
            {90, -80, 61, -52, 86, 50, 7, -30, 45, -70, -101, 21, -43, -11, 53, -59, -36, -124, 100, 34, 58, -123, -60, -76, -18, 43, -100, 116, -102, -26, 86, 120},
            {35, 20, 30, 74, -100, 63, -108, 7, 16, -35, 100, -65, 25, 0, -32, -67, 80, -111, 8, -122, -11, -9, -107, 98, 93, 117, -21, -124, -8, 84, 21, 57},
            {-62, 45, 89, 16, 76, -34, 11, 52, 41, 52, 120, 85, -80, 6, -106, 66, 37, 0, 84, 91, 33, -20, 7, 21, -104, 80, 66, -110, -17, -102, 78, 66},
            {25, -15, 40, 62, 86, -100, -15, 27, -123, 79, 106, 40, -116, -18, 72, 110, -23, 97, -20, 118, -43, 90, 39, -99, 66, 71, 109, 106, 82, 8, 24, -55},
            {111, -63, -15, -11, 119, 52, -121, 73, 108, 59, 111, 9, 87, -11, -17, 93, -91, -3, -19, -85, 13, 79, -77, -100, 103, 79, 26, -38, 49, 6, -86, 34},
            {83, -94, -22, -88, 82, -59, 56, 120, -126, -1, -35, -5, -101, -104, 87, -82, 73, 48, -118, -77, -39, 98, 32, -38, 15, -104, 69, -16, -108, 78, -13, 2}
    };

    public static final byte[][] macs = {
            {103, 63, -46, -67, 13, -16, -74, 86, 121, -69, 24, -76, 52, 105, -126, -41, -120, 97, 106, 8, 16, -6, -8, 7, 96, 65, -119, 4, -48, 9, -107, 14},
            {105, 119, 35, 109, -22, 93, -70, -117, -9, 36, -98, 4, -128, -92, -128, -95, 105, -7, -66, 3, -128, -99, 18, 0, 52, -20, 10, 10, -60, -80, 119, 11},
            {107, 63, -16, -38, 10, 71, 66, 112, -26, -45, -115, -56, -29, 123, 107, 103, -96, -33, -55, 14, -24, -7, -5, 12, -4, -110, -115, 8, -116, -81, -87, 14},
            {42, 122, -99, -112, -95, -60, 122, -71, 18, -89, 108, -114, 56, -10, 12, 104, -128, -46, -92, 6, 12, 127, 53, 10, -80, -64, -71, 6, -128, 61, 24, 12},
            {-76, 30, 99, 82, -104, -70, -47, -66, -31, 93, -99, -76, -23, -9, 116, 66, 66, 50, -25, 2, -80, -66, -119, 14, 100, -96, 119, 0, -4, 65, 113, 11},
            {-43, -4, 2, -57, -47, -83, 62, 32, 118, -1, 44, 3, -45, -62, -109, -52, -40, 106, -55, 12, -84, -67, -96, 3, -44, -44, -22, 15, -24, 11, -71, 5},
            {-84, 9, -96, 87, -15, 28, 89, 31, 26, 50, -94, -33, 101, -107, -47, 73, 106, 4, -75, 2, 84, 67, 41, 9, 16, -119, -69, 14, 60, -90, -18, 3},
            {124, 113, -71, 109, -87, 7, 88, -117, -123, 48, 67, -122, -31, -126, 77, -73, 29, -14, -82, 8, -32, -73, -126, 1, 88, 70, -73, 14, -80, -107, 116, 1},
            {49, -27, -7, -20, 94, -43, 31, 25, 58, 67, -23, 52, 55, -75, 86, 10, 91, -67, -96, 1, 80, 80, -113, 2, -64, 32, -56, 3, -28, 111, 16, 10},
            {94, 3, 60, 24, 24, 79, 125, -73, -28, -30, 69, -67, -84, -18, 118, 95, 78, 67, 124, 10, 124, -14, -101, 15, 92, 82, -39, 14, 4, 100, 13, 11},
            {111, 30, 30, 64, -23, -103, -81, -87, 39, -25, -54, -22, 99, 106, -8, 90, 87, 9, -90, 11, -44, 96, -74, 7, 96, 36, -102, 15, 96, -43, -49, 10},
            {-56, -43, 56, -125, -19, 89, 40, 40, -50, 17, 33, -40, 81, -115, -56, 21, 81, -36, 58, 5, -76, -71, -46, 12, 24, 122, 55, 7, -52, 65, -37, 7},
            {-48, 64, -48, -51, 11, 80, -18, 94, -119, -11, 67, -13, 117, -127, 52, 43, -90, 90, 4, 0, 48, -58, 45, 14, -32, 31, 21, 10, 124, -50, 40, 14},
            {-93, -108, -63, 101, -95, -90, 16, 108, -17, -105, -87, -115, -55, 29, -122, 31, -103, -11, 84, 14, -76, 44, -80, 8, 88, -9, -98, 12, 100, -10, 26, 1},
            {-95, 39, -27, -123, -13, 9, 19, 47, 61, -56, 107, -30, 119, -26, -86, -51, -57, 19, 40, 11, -60, -3, 13, 13, 16, -7, -30, 0, -104, -33, -54, 0},
            {-73, 18, -63, 108, 9, 3, -74, 105, 78, 62, -45, -70, 104, 58, -26, 70, -28, -97, 11, 14, 32, -79, -69, 7, 108, -9, 1, 12, -12, -51, 3, 3}
    };

    public static final SecretKey[] keyList = {
            new SecretKeySpec(keys[0], CipherSuiteTestsUtility.cipherAlgorithm),
            new SecretKeySpec(keys[1], CipherSuiteTestsUtility.cipherAlgorithm),
            new SecretKeySpec(keys[2], CipherSuiteTestsUtility.cipherAlgorithm),
            new SecretKeySpec(keys[3], CipherSuiteTestsUtility.cipherAlgorithm),
            new SecretKeySpec(keys[4], CipherSuiteTestsUtility.cipherAlgorithm),
            new SecretKeySpec(keys[5], CipherSuiteTestsUtility.cipherAlgorithm),
            new SecretKeySpec(keys[6], CipherSuiteTestsUtility.cipherAlgorithm),
            new SecretKeySpec(keys[7], CipherSuiteTestsUtility.cipherAlgorithm),
            new SecretKeySpec(keys[8], CipherSuiteTestsUtility.cipherAlgorithm),
            new SecretKeySpec(keys[9], CipherSuiteTestsUtility.cipherAlgorithm),
            new SecretKeySpec(keys[10], CipherSuiteTestsUtility.cipherAlgorithm),
            new SecretKeySpec(keys[11], CipherSuiteTestsUtility.cipherAlgorithm),
            new SecretKeySpec(keys[12], CipherSuiteTestsUtility.cipherAlgorithm),
            new SecretKeySpec(keys[13], CipherSuiteTestsUtility.cipherAlgorithm),
            new SecretKeySpec(keys[14], CipherSuiteTestsUtility.cipherAlgorithm),
            new SecretKeySpec(keys[15], CipherSuiteTestsUtility.cipherAlgorithm)
    };

    public static final SecretKey[] macList = {
            new SecretKeySpec(macs[0], CipherSuiteTestsUtility.macAlgorithm),
            new SecretKeySpec(macs[1], CipherSuiteTestsUtility.macAlgorithm),
            new SecretKeySpec(macs[2], CipherSuiteTestsUtility.macAlgorithm),
            new SecretKeySpec(macs[3], CipherSuiteTestsUtility.macAlgorithm),
            new SecretKeySpec(macs[4], CipherSuiteTestsUtility.macAlgorithm),
            new SecretKeySpec(macs[5], CipherSuiteTestsUtility.macAlgorithm),
            new SecretKeySpec(macs[6], CipherSuiteTestsUtility.macAlgorithm),
            new SecretKeySpec(macs[7], CipherSuiteTestsUtility.macAlgorithm),
            new SecretKeySpec(macs[8], CipherSuiteTestsUtility.macAlgorithm),
            new SecretKeySpec(macs[9], CipherSuiteTestsUtility.macAlgorithm),
            new SecretKeySpec(macs[10], CipherSuiteTestsUtility.macAlgorithm),
            new SecretKeySpec(macs[11], CipherSuiteTestsUtility.macAlgorithm),
            new SecretKeySpec(macs[12], CipherSuiteTestsUtility.macAlgorithm),
            new SecretKeySpec(macs[13], CipherSuiteTestsUtility.macAlgorithm),
            new SecretKeySpec(macs[14], CipherSuiteTestsUtility.macAlgorithm),
            new SecretKeySpec(macs[15], CipherSuiteTestsUtility.macAlgorithm)
    };

    public static final SecretKey[] groupKeys = {
            new GroupKey(keyList[0], macList[0]),
            new GroupKey(keyList[1], macList[1]),
            new GroupKey(keyList[2], macList[2]),
            new GroupKey(keyList[3], macList[3]),
            new GroupKey(keyList[4], macList[4]),
            new GroupKey(keyList[5], macList[5]),
            new GroupKey(keyList[6], macList[6]),
            new GroupKey(keyList[7], macList[7]),
            new GroupKey(keyList[8], macList[8]),
            new GroupKey(keyList[9], macList[9]),
            new GroupKey(keyList[10], macList[10]),
            new GroupKey(keyList[11], macList[11]),
            new GroupKey(keyList[12], macList[12]),
            new GroupKey(keyList[13], macList[13]),
            new GroupKey(keyList[14], macList[14]),
            new GroupKey(keyList[15], macList[15]),
    };

    public static final List<SecretKey> groupKeyList = Arrays.asList(groupKeys);

    public static final String testInput =
            "Es ist ein paradiesmatisches Land, in dem einem gebratene Satzteile in den " +
                    "Mund fliegen. Nicht einmal von der allmächtigen Interpunktion werden die " +
                    "Blindtexte beherrscht – ein geradezu unorthographisches Leben. Eines Tages " +
                    "aber beschloß eine kleine Zeile Blindtext, ihr Name war Lorem Ipsum, hinaus " +
                    "zu gehen in die weite Grammatik. Der große Oxmox riet ihr davon ab, da es dort " +
                    "wimmele von bösen Kommata, wilden Fragezeichen und hinterhältigen Semikoli, doch " +
                    "das Blindtextchen ließ sich nicht beirren. Es packte seine sieben Versalien, schob " +
                    "sich sein Initial in den Gürtel und machte sich auf den Weg. Als es die ersten " +
                    "Hügel des Kursivgebirges erklommen hatte, warf es einen letzten Blick zurück " +
                    "auf die Skyline seiner Heimatstadt Buchstabhausen, die Headline von Alphabetdorf " +
                    "und die Subline seiner eigenen Straße, der Zeilengasse. Wehmütig lief " +
                    "ihm eine rethorische Frage über die Wange, dann setzte es seinen Weg fort. " +
                    "Unterwegs traf es eine Copy. Die Copy warnte das Blindtextchen, da, wo sie herkäme " +
                    "wäre sie zigmal umgeschrieben worden und alles, was von ihrem Ursprung noch übrig " +
                    "wäre, sei das Wort “und” und das Blindtextchen solle umkehren und wieder in sein" +
                    " eigenes, sicheres Land zurückkehren. Doch alles Gutzureden konnte es nicht " +
                    "überzeugen und so dauerte es nicht lange, bis ihm ein paar heimtückische " +
                    "Werbetexter auflauerten, es mit Longe und Parole betrunken machten und es dann in " +
                    "ihre Agentur schleppten, wo sie es für ihr";

    public static byte[] getByteInput() {
        try {
            return testInput.getBytes(CipherSuiteTestsUtility.charEncoding);
        } catch (Exception e) {
            return new byte[CipherSuiteTestsUtility.PLAINSIZE];
        }
    }


}
