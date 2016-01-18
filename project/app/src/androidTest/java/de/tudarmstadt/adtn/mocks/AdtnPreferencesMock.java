package de.tudarmstadt.adtn.mocks;

import de.tudarmstadt.adtn.preferences.IAdtnPreferences;

/**
 * Mocks a AdtnPreferences object.
 */
public class AdtnPreferencesMock implements IAdtnPreferences {

    private int sendInterval = 1;
    private int refill = 10;
    private int batchSize = 10;

    /**
     * Resets the settings to their default values.
     */
    @Override
    public void reset() {

    }

    /**
     * Starts a new edit transaction. Needs to be called prior to modifying any preference.
     */
    @Override
    public void edit() {

    }

    /**
     * Commits the edit transaction started by {@link #edit()}.
     */
    @Override
    public void commit() {

    }

    /**
     * Registers an {@link OnCommitListener}.
     *
     * @param listener The listener to register.
     */
    @Override
    public void addOnCommitListenerListener(OnCommitListener listener) {

    }

    /**
     * Removes an {@link OnCommitListener}
     *
     * @param listener The listener to remove.
     */
    @Override
    public void removeOnCommitListener(OnCommitListener listener) {

    }

    /**
     * Removes all OnCommitListeners.
     */
    @Override
    public void clearOnCommitListeners() {

    }

    @Override
    public void setSendingPoolBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public int getSendingPoolBatchSize() {
        return batchSize;
    }

    @Override
    public void setSendingPoolRefillThreshold(int refillThreshold) {
        this.refill = refillThreshold;
    }

    @Override
    public void setAutoJoinAdHocNetwork(boolean autoJoin) {

    }

    @Override
    public boolean getShowHelpButtons() {
        return false;
    }

    @Override
    public void setShowHelpButtons(boolean showHelpButtons) {
    }

    @Override
    public boolean getAutoJoinAdHocNetwork() {
        return false;
    }

    @Override
    public void setSendingPoolSendInterval(int sendInterval) {
        this.sendInterval = sendInterval;
    }

    @Override
    public int getSendingPoolRefillThreshold() {
        return refill;
    }

    @Override
    public int getSendingPoolSendInterval() {
        return sendInterval;
    }
}
