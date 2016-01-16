package de.tudarmstadt.adtn.genericpreferences;

/**
 * Loads and stores preferences and notifies observers if preferences were modified.
 */
public interface IPreferences {

    /**
     * Resets the settings to their default values.
     */
    void reset();

    /**
     * Starts a new edit transaction. Needs to be called prior to modifying any preference.
     */
    void edit();

    /**
     * Commits the edit transaction started by {@link #edit()}.
     */
    void commit();

    /**
     * Registers an {@link IPreferences.OnCommitListener}.
     *
     * @param listener The listener to register.
     */
    void addOnCommitListenerListener(OnCommitListener listener);

    /**
     * Removes an {@link IPreferences.OnCommitListener}
     *
     * @param listener The listener to remove.
     */
    void removeOnCommitListener(OnCommitListener listener);

    /**
     * Removes all OnCommitListeners.
     */
    void clearOnCommitListeners();

    /**
     * A listener to get notifications when the preferences are committed.
     */
    interface OnCommitListener {
        /**
         * Gets called after new preferences were committed to the preferences object. It runs in
         * the same thread that called commit().
         */
        void onCommit();
    }
}
