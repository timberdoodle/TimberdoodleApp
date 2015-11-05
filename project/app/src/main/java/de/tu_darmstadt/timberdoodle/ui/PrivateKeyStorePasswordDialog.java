package de.tu_darmstadt.timberdoodle.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import de.tu_darmstadt.adtn.ui.passworddialog.GroupPasswordDialog;
import de.tu_darmstadt.adtn.ui.passworddialog.PasswordDialog;
import de.tu_darmstadt.timberdoodle.IService;
import de.tu_darmstadt.timberdoodle.R;

/**
 * A class for showing a password dialog for the private key store.
 */
public class PrivateKeyStorePasswordDialog extends PasswordDialog {

    private final IService service;
    private final GroupPasswordDialog groupDialog;

    /**
     * Creates a new PrivateKeyStorePasswordDialog object.
     *
     * @param context            The context to use.
     * @param onDoneListener     The listener that gets called when the dialog is done.
     * @param service            The Timberdoodle service object.
     * @param prependGroupDialog If true, a GroupPasswordDialog is shown before this dialog. If the
     *                           user created a new group store or entered the correct password for
     *                           an existing group store, the same password is used to try to unlock
     *                           the private key store. If the private key store does not exist yet,
     *                           the user will be asked if he wants to use the same password as the
     *                           one that he entered for the group key store.
     */
    public PrivateKeyStorePasswordDialog(final Context context, final OnDoneListener onDoneListener,
                                         final IService service, boolean prependGroupDialog) {
        super(context, onDoneListener);

        this.service = service;

        // If requested, the group dialog is shown first
        if (prependGroupDialog) {
            groupDialog = new GroupPasswordDialog(context, new OnDoneListener() {
                @Override
                public void onDone() {
                    PrivateKeyStorePasswordDialog.super.show();
                }
            }, service.getAdtnService());
        } else {
            groupDialog = null;
        }
    }

    /**
     * Shows the password dialog.
     */
    @Override
    public void show() {
        // Prepend group dialog if requested
        if (groupDialog == null) {
            super.show();
        } else {
            groupDialog.show();
        }
    }

    /**
     * Shows a dialog where to user has to enter the password for an existing key store.
     */
    @Override
    protected void showEnterPassword() {
        /* Try to use the group password to unlock the private key store if the group dialog
         * preceded the private key store password dialog */
        if (groupDialog != null && groupDialog.getPassword() != null) {
            if (loadKeyStore(groupDialog.getPassword())) {
                setPassword(groupDialog.getPassword());
                done();
            }
        } else {
            super.showEnterPassword();
        }
    }

    /**
     * Shows a dialog where the user has to specify the password for a new key store
     */
    @Override
    protected void showCreateNewStore() {
        /* Ask the user if he wants to use the same password for the new private key store that he
         * used for the group key store. */
        if (groupDialog != null && groupDialog.getPassword() != null) {
            showYesNoDialog(R.string.use_same_password,
                    new DialogInterface.OnClickListener() {
                        // Clicked "yes": Create private key store with group key store password
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            createNewStore(groupDialog.getPassword());
                            setPassword(groupDialog.getPassword());
                            done();
                        }
                    }, new DialogInterface.OnClickListener() {
                        // Clicked "no": Let user enter password for private key store
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PrivateKeyStorePasswordDialog.super.showCreateNewStore();
                        }
                    });
        } else {
            super.showCreateNewStore();
        }
    }

    /**
     * Tries to load the key store using the specified password.
     *
     * @param password The password to use for opening the key store.
     * @return true on success or false otherwise.
     */
    @Override
    protected boolean loadKeyStore(String password) {
        return service.openPrivateKeyStore(password);
    }

    /**
     * Creates a new key store using the specified password.
     *
     * @param password The password to use for the new key store.
     */
    @Override
    protected void createNewStore(String password) {
        service.createPrivateKeyStore(password);
    }

    /**
     * @return true if the key store is already loaded or false otherwise.
     */
    @Override
    protected boolean isKeyStoreLoaded() {
        return service.getPrivateKeyStore() != null;
    }

    /**
     * @return true if there is already a key store or false otherwise.
     */
    @Override
    protected boolean isKeyStorePresent() {
        return service.privateKeyStoreExists();
    }

    /**
     * @return The string resource ID for setting an initial password.
     */
    @Override
    protected int getSetPasswordString() {
        return R.string.set_private_password;
    }

    /**
     * @return The string resource ID for the first attempt of entering the password of an existing
     * key store.
     */
    @Override
    protected int getEnterPasswordString() {
        return R.string.enter_private_password;
    }

    /**
     * @return The string resource ID for asking the user if he wants to create a new key store
     * instead of opening the old one.
     */
    @Override
    protected int getResetPasswordString() {
        return R.string.reset_private_password;
    }

    /**
     * @return The string resource ID for asking the user if he really wants to continue with a
     * locked key store.
     */
    @Override
    protected int getContinueWithoutStoreWarningString() {
        return R.string.warn_no_private_password;
    }
}
