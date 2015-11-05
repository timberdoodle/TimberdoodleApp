package de.tu_darmstadt.adtn.sendingpool;

import de.tu_darmstadt.adtn.AdtnSocketException;

/**
 * Wraps messages from the message store in packets and stores them.
 * The packets are then broadcasted to network in batches.
 */
public interface ISendingPool {

    /**
     * A listener for getting notifications about sending errors.
     */
    interface OnSendingErrorListener {

        /**
         * Gets called when the socket threw an error in a call to send.
         *
         * @param e The exception thrown by the socket.
         */
        void onSendingError(AdtnSocketException e);
    }

    /**
     * Cancels message processing. Note that this could block if message store or network are
     * blocking.
     */
    void close();
}
