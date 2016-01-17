package de.tudarmstadt.adtn.genericpreferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Loads and stores sharedPreferences and notifies observers if sharedPreferences were modified.
 */
public abstract class Preferences implements IPreferences {

    // The SharedPreferences object that holds all the sharedPreferences
    private final SharedPreferences sharedPreferences;
    // Makes sure only one thread at a time is editing the sharedPreferences
    private final ReentrantLock editorLock = new ReentrantLock();
    // The listener which get notified when an edit transaction is done
    private final ArrayList<OnCommitListener> listeners = new ArrayList<>();
    // Will be used to edit the sharedPreferences
    private volatile SharedPreferences.Editor editor;

    /**
     * Creates a new Preferences object.
     *
     * @param context  The context to use.
     * @param filename The filename where the sharedPreferences are stored.
     */
    public Preferences(Context context, String filename) {
        sharedPreferences = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
    }

    /**
     * Resets the settings to their default values.
     */
    @Override
    public void reset() {
        edit();
        editor.clear();
        commit();
    }

    /**
     * Starts a new edit transaction. Needs to be called prior to modifying any preference.
     */
    @SuppressLint("CommitPrefEdits")
    @Override
    public void edit() {
        editorLock.lock();
        editor = sharedPreferences.edit();
    }

    /**
     * Commits the edit transaction started by {@link #edit()}.
     */
    @Override
    public void commit() {
        // Apply new sharedPreferences
        editor.apply();
        editor = null;
        editorLock.unlock();

        // Notify listeners
        for (OnCommitListener listener : listeners) {
            listener.onCommit();
        }
    }

    /**
     * @return The SharedPreferences object containing the sharedPreferences managed by this object.
     */
    protected SharedPreferences getPrefs() {
        return sharedPreferences;
    }

    /**
     * @return A {@link android.content.SharedPreferences.Editor} object to edit sharedPreferences if an
     * edit transaction has been started.
     */
    protected SharedPreferences.Editor getEditor() {
        return editor;
    }

    /**
     * Registers an {@link IPreferences.OnCommitListener}.
     *
     * @param listener The listener to register.
     */
    @Override
    public void addOnCommitListenerListener(OnCommitListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes an {@link IPreferences.OnCommitListener}
     *
     * @param listener The listener to remove.
     */
    @Override
    public void removeOnCommitListener(OnCommitListener listener) {
        listeners.remove(listener);
    }

    /**
     * Removes all OnCommitListeners.
     */
    @Override
    public void clearOnCommitListeners() {
        listeners.clear();
    }
}
