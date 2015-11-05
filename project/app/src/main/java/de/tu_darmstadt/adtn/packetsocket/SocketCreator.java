package de.tu_darmstadt.adtn.packetsocket;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import de.tu_darmstadt.adtn.AdtnSocketException;
import de.tu_darmstadt.adtn.Utilities;

/**
 * Helps with starting the packet socket creator process.
 */
public class SocketCreator {

    private final static String HELPER_FILENAME = "libpacketsocket_creator.so";

    private volatile String errorMessage;

    private final Process process;
    private final InputStream stdout;

    /**
     * Creates a new SocketCreator object.
     *
     * @param context The context to use.
     * @param udsPath The abstract path for the Unix domain socket.
     * @param family The socket family.
     * @param type The socket type.
     * @param protocol The socket protocol.
     */
    public SocketCreator(Context context, String udsPath, int family, int type, int protocol) {
        // Run socket creator process
        try {
            String helperPath = new File(context.getApplicationInfo().nativeLibraryDir, HELPER_FILENAME).getCanonicalPath();
            process = Utilities.runAsRoot(helperPath + " " + android.os.Process.myPid() +
                    " " + udsPath + " " + family + " " + type + " " + protocol);
        } catch (IOException e) {
            throw new AdtnSocketException("Could not run socket creator process", e);
        }
        stdout = process.getInputStream();

        // Check if an error message appears on stderr
        final InputStream stderr = process.getErrorStream();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    errorMessage = new BufferedReader(new InputStreamReader(stderr)).readLine();
                } catch (IOException e) {
                    // Ignore
                }
            }
        });
    }

    /**
     * Blocks until the Unix domain socket of the socket creator process is listening or an error
     * occurred.
     *
     * @return true if the socket creator process is listening or false if an error occurred.
     */
    public boolean waitForListen() {
        // Wait for the notification on stdout that the Unix domain socket is listening
        try {
            if (stdout.read() == '\n') return true;
            errorMessage = "Unexpected data on stdout";
        } catch (IOException e) {
            // Ignore
        }

        return false;
    }

    /**
     * Waits for the socket creator process to exit.
     *
     * @return An error message if an error occurred or null otherwise.
     */
    public String waitForExit() {
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return errorMessage;
    }
}
