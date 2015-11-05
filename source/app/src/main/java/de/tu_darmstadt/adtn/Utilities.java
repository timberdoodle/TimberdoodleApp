package de.tu_darmstadt.adtn;

import java.io.IOException;

public final class Utilities {

    // Make class static
    private Utilities() {
    }

    /**
     * Triggers the root permission dialog.
     *
     * @return true on success or false otherwise.
     */
    public static boolean requestRootPermissions() {
        try {
            runAsRoot("exit").waitFor();
        } catch (IOException | InterruptedException e) {
            return false;
        }

        return true;
    }

    /**
     * Runs the specified command as superuser.
     *
     * @param command The command to run.
     * @return The started process.
     * @throws IOException {@see Runtime#exec(String)}
     */
    public static Process runAsRoot(String command) throws IOException {
        return Runtime.getRuntime().exec(new String[]{"su", "-c", command});
    }
}
