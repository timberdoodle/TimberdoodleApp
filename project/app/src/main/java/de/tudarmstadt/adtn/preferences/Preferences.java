package de.tudarmstadt.adtn.preferences;

import android.content.Context;

public class Preferences extends de.tudarmstadt.adtn.genericpreferences.Preferences implements IPreferences {

    private static final String PREFKEY_SENDING_POOL_SEND_INTERVAL = "SendingPoolSendInterval";
    private static final String PREFKEY_SENDING_POOL_REFILL_THRESHOLD = "SendingPoolRefillThreshold";
    private static final String PREFKEY_SENDING_POOL_BATCH_SIZE = "SendingPoolBatchSize";
    private static final String PREFKEY_AUTO_JOIN_AD_HOC_NETWORK = "AutoJoinAdHocNetwork";
    private static final String PREFKEY_SHOW_HELP_BUTTONS = "ShowHelpButtons";

    public Preferences(Context context) {
        super(context, "adtn.preferences");
    }

    @Override
    public int getSendingPoolSendInterval() {
        return getPrefs().getInt(PREFKEY_SENDING_POOL_SEND_INTERVAL, DEFAULT_SENDING_POOL_SEND_INTERVAL);
    }


    @Override
    public void setSendingPoolSendInterval(int sendInterval) {
        getEditor().putInt(PREFKEY_SENDING_POOL_SEND_INTERVAL, sendInterval);
    }

    @Override
    public int getSendingPoolRefillThreshold() {
        return getPrefs().getInt(PREFKEY_SENDING_POOL_REFILL_THRESHOLD, DEFAULT_SENDING_POOL_REFILL_THRESHOLD);
    }

    @Override
    public void setSendingPoolRefillThreshold(int refillThreshold) {
        getEditor().putInt(PREFKEY_SENDING_POOL_REFILL_THRESHOLD, refillThreshold);
    }

    @Override
    public int getSendingPoolBatchSize() {
        return getPrefs().getInt(PREFKEY_SENDING_POOL_BATCH_SIZE, DEFAULT_SENDING_POOL_BATCH_SIZE);
    }

    @Override
    public void setSendingPoolBatchSize(int batchSize) {
        getEditor().putInt(PREFKEY_SENDING_POOL_BATCH_SIZE, batchSize);
    }

    @Override
    public boolean getAutoJoinAdHocNetwork() {
        return getPrefs().getBoolean(PREFKEY_AUTO_JOIN_AD_HOC_NETWORK, DEFAULT_AUTO_JOIN_AD_HOC_NETWORK);
    }

    @Override
    public void setAutoJoinAdHocNetwork(boolean autoJoin) {
        getEditor().putBoolean(PREFKEY_AUTO_JOIN_AD_HOC_NETWORK, autoJoin);
    }

    @Override
    public boolean getShowHelpButtons() {
        return getPrefs().getBoolean(PREFKEY_SHOW_HELP_BUTTONS, DEFAULT_SHOW_HELP_BUTTONS);
    }

    @Override
    public void setShowHelpButtons(boolean showHelpButtons) {
        getEditor().putBoolean(PREFKEY_SHOW_HELP_BUTTONS, showHelpButtons);
    }
}
