package de.tudarmstadt.adtn.ui.passworddialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.TextView;

import de.tudarmstadt.timberdoodle.R;

/**
 * An abstract class for showing a password dialog.
 */
public abstract class PasswordDialog {

    /**
     * A listener for notifying the password dialog user that everything is done.
     */
    public interface OnDoneListener {

        /**
         * Gets called when the whole password dialog operation is done.
         */
        void onDone();
    }

    /**
     * A listener for passing the password that was entered when showPasswordDialog was called.
     */
    interface OnPasswordEnteredListener {

        /**
         * Gets called when the user entered a password and clicked the ok button.
         *
         * @param password The password the user entered.
         */
        void onPasswordEntered(DialogInterface dialog, int which, String password);
    }

    private final Context context;
    private final OnDoneListener onDoneListener;

    private String password;

    /**
     * Creates a new PasswordDialog object.
     *
     * @param context        The context to use.
     * @param onDoneListener The listener that gets called when the dialog is done.
     */
    public PasswordDialog(Context context, OnDoneListener onDoneListener) {
        this.context = context;
        this.onDoneListener = onDoneListener;
    }

    // Shows the dialog described in the specified builder
    private void showDialog(AlertDialog.Builder builder) {
        // Create and show dialog
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Allow multi line title
        int titleId = context.getResources().getIdentifier("alertTitle", "id", "android");
        TextView titleView = (TextView) dialog.findViewById(titleId);
        titleView.setSingleLine(false);
    }

    // Shows a dialog containing a password input field and ok/cancel buttons
    private void showPasswordDialog(@StringRes int title,
                                    final OnPasswordEnteredListener okListener,
                                    final DialogInterface.OnClickListener cancelListener) {
        // Create EditText for entering the password
        final EditText input = new EditText(context);
        input.setSingleLine();
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setTransformationMethod(PasswordTransformationMethod.getInstance());

        // Create and show dialog
        showDialog(new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(input)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        okListener.onPasswordEntered(dialog, which, input.getText().toString());
                    }
                })
                .setNegativeButton(android.R.string.cancel, cancelListener));
    }

    /**
     * Shows an AlertDialog with a yes and a no button.
     *
     * @param title       The string resource ID of the title to use.
     * @param yesListener The listener that handles the click to the "yes" button.
     * @param noListener  The listener that handles the click to the "no" button.
     */
    protected void showYesNoDialog(@StringRes int title,
                                   final DialogInterface.OnClickListener yesListener,
                                   final DialogInterface.OnClickListener noListener) {
        showDialog(new AlertDialog.Builder(context)
                .setTitle(title)
                .setPositiveButton(android.R.string.yes, yesListener)
                .setNegativeButton(android.R.string.no, noListener));
    }

    /**
     * Shows an AlertDialog with only an ok button.
     *
     * @param title      The string resource ID of the title to use.
     * @param okListener The listener that handles the click to the "ok" button.
     */
    protected void showOkDialog(@StringRes int title, DialogInterface.OnClickListener okListener) {
        showDialog(new AlertDialog.Builder(context)
                .setTitle(title)
                .setNeutralButton(android.R.string.ok, okListener));
    }

    /**
     * Shows a dialog where the user has to specify the password for a new key store
     */
    protected void showCreateNewStore() {
        showPasswordDialog(getSetPasswordString(), new OnPasswordEnteredListener() {
            // Clicked "ok": Create new store and notify password dialog user
            @Override
            public void onPasswordEntered(DialogInterface dialog, int which, String password) {
                if (password.isEmpty()) { // Show "empty password not allowed" dialog
                    showOkDialog(R.string.empty_password_not_allowed,
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showCreateNewStore();
                        }
                    });
                } else {
                    showConfirmNewStorePassword(password);
                }
            }
        }, new DialogInterface.OnClickListener() {
            // Clicked "cancel": Warn user that there will be no access to store
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showAskContinueWithoutStore();
            }
        });
    }

    // Shows a dialog that asks the user to confirm the password for the new store
    private void showConfirmNewStorePassword(final String password) {
        showPasswordDialog(R.string.repeat_password, new OnPasswordEnteredListener() {
            // Clicked "ok": Create store if passwords are equal
            @Override
            public void onPasswordEntered(DialogInterface dialog, int which, String confirmPassword) {
                if (!confirmPassword.equals(password)) { // Inform user if passwords do not match
                    showConfirmPasswordFailed();
                } else {
                    createNewStore(password);
                    PasswordDialog.this.password = password;
                    done();
                }
            }
        }, new DialogInterface.OnClickListener() {
            // Clicked "cancel": Show "create new store" dialog again
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showCreateNewStore();
            }
        });
    }

    // Show a dialog that informs the user that the entered passwords do not match
    private void showConfirmPasswordFailed() {
        showOkDialog(R.string.repeat_password_failed, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showCreateNewStore();
            }
        });
    }

    /**
     * Shows a dialog where to user has to enter the password for an existing key store.
     */
    protected void showEnterPassword() {
        showPasswordDialog(getEnterPasswordString(), new OnPasswordEnteredListener() {
            @Override
            public void onPasswordEntered(DialogInterface dialog, int which, String password) {
                if (!password.isEmpty() && loadKeyStore(password)) {
                    // Clicked "ok" and password is correct: Done
                    PasswordDialog.this.password = password;
                    done();
                } else {
                    // Clicked "ok" and password is wrong: Show dialog
                    showOkDialog(R.string.wrong_password, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showEnterPassword();
                        }
                    });
                }
            }
        }, new DialogInterface.OnClickListener() {
            // Clicked "cancel": Ask user if he wants to reset the key store
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showAskReset();
            }
        });
    }

    /* Shows a dialog asking the user if he wants to create a new key store instead of opening the
     * existing one */
    private void showAskReset() {
        showYesNoDialog(getResetPasswordString(),
                new DialogInterface.OnClickListener() {
                    // Clicked "yes": Show dialog setting password of new store
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showCreateNewStore();
                    }
                }, new DialogInterface.OnClickListener() {
                    // Clicked "no": Warn user that there will be no access to store
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showAskContinueWithoutStore();
                    }
                });
    }

    private void showAskContinueWithoutStore() {
        showYesNoDialog(getContinueWithoutStoreWarningString(),
                new DialogInterface.OnClickListener() {
                    // Clicked "yes": Done
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        done();
                    }
                }, new DialogInterface.OnClickListener() {
                    // Clicked no: Restart password dialog
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        show();
                    }
                }
        );
    }

    /**
     * Shows the password dialog.
     */
    public void show() {
        if (isKeyStoreLoaded()) { // Nothing to do if key store already loaded
            done();
        } else if (isKeyStorePresent()) { // Ask for password if key store is present but not loaded
            showEnterPassword();
        } else { // Ask for initial password if key store does not exist yet
            showCreateNewStore();
        }
    }

    /**
     * Calls the
     */
    protected void done() {
        onDoneListener.onDone();
    }

    /**
     * @return The entered password if it was correct or the password of a newly created store or
     * null otherwise.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Use this method to set the password if it was correct or a new store was created.
     *
     * @param password The password that was  used.
     */
    protected void setPassword(String password) {
        this.password = password;
    }

    /**
     * Tries to load the key store using the specified password.
     *
     * @param password The password to use for opening the key store.
     * @return true on success or false otherwise.
     */
    protected abstract boolean loadKeyStore(String password);

    /**
     * Creates a new key store using the specified password.
     *
     * @param password The password to use for the new key store.
     */
    protected abstract void createNewStore(String password);

    /**
     * @return true if the key store is already loaded or false otherwise.
     */
    protected abstract boolean isKeyStoreLoaded();

    /**
     * @return true if there is already a key store or false otherwise.
     */
    protected abstract boolean isKeyStorePresent();

    /**
     * @return The string resource ID for setting an initial password.
     */
    protected abstract
    @StringRes
    int getSetPasswordString();

    /**
     * @return The string resource ID for the first attempt of entering the password of an existing
     * key store.
     */
    protected abstract
    @StringRes
    int getEnterPasswordString();

    /**
     * @return The string resource ID for asking the user if he wants to create a new key store
     * instead of opening the old one.
     */
    protected abstract
    @StringRes
    int getResetPasswordString();

    /**
     * @return The string resource ID for asking the user if he really wants to continue with a
     * locked key store.
     */
    protected abstract
    @StringRes
    int getContinueWithoutStoreWarningString();
}
