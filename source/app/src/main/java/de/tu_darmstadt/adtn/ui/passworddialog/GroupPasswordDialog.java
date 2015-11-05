package de.tu_darmstadt.adtn.ui.passworddialog;

import android.content.Context;

import de.tu_darmstadt.adtn.IService;
import de.tu_darmstadt.timberdoodle.R;

/**
 * A class for showing a password dialog for the group key store.
 */
public class GroupPasswordDialog extends PasswordDialog {

    private final IService service;

    /**
     * Creates a new GroupPasswordDialog object.
     *
     * @param context        The context to use.
     * @param onDoneListener The listener that gets called when the dialog is done.
     * @param service        The aDTN service object.
     */
    public GroupPasswordDialog(Context context, OnDoneListener onDoneListener, IService service) {
        super(context, onDoneListener);

        this.service = service;
    }

    /**
     * Tries to load the key store using the specified password.
     *
     * @param password The password to use for opening the key store.
     * @return true on success or false otherwise.
     */
    @Override
    protected boolean loadKeyStore(String password) {
        return service.openGroupKeyStore(password);
    }

    /**
     * Creates a new key store using the specified password.
     *
     * @param password The password to use for the new key store.
     */
    @Override
    protected void createNewStore(String password) {
        service.createGroupKeyStore(password);
    }

    /**
     * @return true if the key store is already loaded or false otherwise.
     */
    @Override
    protected boolean isKeyStoreLoaded() {
        return service.getGroupKeyStore() != null;
    }

    /**
     * @return true if there is already a key store or false otherwise.
     */
    @Override
    protected boolean isKeyStorePresent() {
        return service.groupKeyStoreExists();
    }

    /**
     * @return The string resource ID for setting an initial password.
     */
    @Override
    protected int getSetPasswordString() {
        return R.string.set_group_password;
    }

    /**
     * @return The string resource ID for the first attempt of entering the password of an existing
     * key store.
     */
    @Override
    protected int getEnterPasswordString() {
        return R.string.enter_group_password;
    }

    /**
     * @return The string resource ID for asking the user if he wants to create a new key store
     * instead of opening the old one.
     */
    @Override
    protected int getResetPasswordString() {
        return R.string.reset_group_password;
    }

    /**
     * @return The string resource ID for asking the user if he really wants to continue with a
     * locked key store.
     */
    @Override
    protected int getContinueWithoutStoreWarningString() {
        return R.string.warn_no_group_password;
    }
}
