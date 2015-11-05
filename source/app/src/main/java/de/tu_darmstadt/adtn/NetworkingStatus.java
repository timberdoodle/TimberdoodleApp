package de.tu_darmstadt.adtn;

/**
 * Represents the networking status of the network service.
 */
public class NetworkingStatus {

    public final static int STATUS_ENABLED = 0, STATUS_DISABLED = 1, STATUS_ERROR = 2;

    private final int status;
    private final String errorMessage;

    /**
     * Creates a NetworkingStatus object representing either the "enabled" or "disabled" status.
     *
     * @param enabled true if networking is enabled or false if it is disabled.
     */
    public NetworkingStatus(boolean enabled) {
        status = enabled ? STATUS_ENABLED : STATUS_DISABLED;
        errorMessage = null;
    }

    /**
     * Creates a NetworkingStatus object that indicates an error occurred.
     *
     * @param errorMessage The error message specifying why networking failed.
     */
    public NetworkingStatus(String errorMessage) {
        status = STATUS_ERROR;
        this.errorMessage = errorMessage;
    }

    /**
     * @return The networking status. Either STATUS_ENABLED, STATUS_DISABLED or STATUS_ERROR.
     */
    public int getStatus() {
        return status;
    }

    /**
     * @return The error message if the status is STATUS_ERROR or null otherwise.
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
