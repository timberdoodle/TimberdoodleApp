package de.tudarmstadt.adtn.preferences;

/**
 * Provides methods for accessing and modifying the aDTN preferences.
 */
public interface IPreferences extends de.tudarmstadt.adtn.genericpreferences.IPreferences {

    // Sending pool send interval
    int DEFAULT_SENDING_POOL_SEND_INTERVAL = 10;
    // Sending pool refill threshold
    int DEFAULT_SENDING_POOL_REFILL_THRESHOLD = 10;
    // Sending pool batch size
    int DEFAULT_SENDING_POOL_BATCH_SIZE = 10;
    // Auto-join ad-hoc network
    boolean DEFAULT_AUTO_JOIN_AD_HOC_NETWORK = true;
    // Show help buttons
    boolean DEFAULT_SHOW_HELP_BUTTONS = true;

    int getSendingPoolSendInterval();

    void setSendingPoolSendInterval(int sendInterval);

    int getSendingPoolRefillThreshold();

    void setSendingPoolRefillThreshold(int refillThreshold);

    int getSendingPoolBatchSize();

    void setSendingPoolBatchSize(int batchSize);

    boolean getAutoJoinAdHocNetwork();

    void setAutoJoinAdHocNetwork(boolean autoJoin);

    boolean getShowHelpButtons();

    void setShowHelpButtons(boolean showHelpButtons);
}
