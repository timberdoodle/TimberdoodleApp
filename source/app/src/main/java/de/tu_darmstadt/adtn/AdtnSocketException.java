package de.tu_darmstadt.adtn;

/**
 * Exception class for exceptions thrown by ISocket implementations.
 */
public class AdtnSocketException extends RuntimeException {

    /**
     * Creates a new AdtnSocketException object.
     *
     * @param detailMessage A description of the error that occurred.
     * @param throwable     The cause of this exception or null.
     */
    public AdtnSocketException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
