package de.tudarmstadt.adtn.sendingpool;

/**
 * Wraps messages from the message store in packets and stores them.
 * The packets are then broadcasted to network in batches.
 */
public interface ISendingPool {

    /**
     * Cancels message processing. Note that this could block if message store or network are
     * blocking.
     */
    void close();
}
