package de.tudarmstadt.adtn.ui.groupmanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import de.tudarmstadt.adtn.generickeystore.KeyStoreEntry;

/**
 * Static UI helper class for key manager.
 */
public final class Helper {

    // Private dummy constructor to prevent instantiation of static class
    private Helper() {
    }

    /**
     * Configures an EditText that will be used to let the user enter a key alias.
     *
     * @param editText  The EditText to configure.
     * @param maxLength The maximum number of characters the user can write in the EditText.
     */
    public static void setUpAliasEditText(EditText editText, int maxLength) {
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        editText.setSingleLine();
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
    }

    /**
     * Checks the result of KeyStore.addEntry and shows a toast that displays an the appropriate
     * error message.
     *
     * @param context           The context to use for showing the toast.
     * @param entry             The KeyStore.addEntry return value to check.
     * @param aliasExistsString The string ID to show if the alias already exists.
     * @param keyExistsString   The string ID to show if the key is already known. Use %s inside the
     *                          string where the alias that uses the string should be displayed.
     * @return true if entry indicates success or false if the alias or key already exist.
     */
    public static boolean checkAndHandleAddKey(Context context, KeyStoreEntry<?> entry,
                                               @StringRes int aliasExistsString,
                                               @StringRes int keyExistsString) {
        if (entry.getAlias() == null) { // Alias already in use
            showToast(context, aliasExistsString);
            return false;
        } else if (entry.getKey() == null) { // Key already known
            showToast(context, context.getString(keyExistsString, entry.getAlias()));
            return false;
        }

        return true; // Success
    }

    /**
     * Shows a dialog for (re)naming a key.
     *
     * @param context             The context to use.
     * @param dialogData          Text to show in the dialog.
     * @param listener            The listener to notify the caller that the length of the alias is
     *                            within range and the user pressed ok.
     */
    public static void showAliasInputDialog(final Context context,
                                            final IAliasDialogData dialogData,
                                            final OnConfirmAliasListener listener) {
        // Create group name EditText
        final EditText input = new EditText(context);
        setUpAliasEditText(input, dialogData.getMaxAliasLength());

        // Create DialogBox with clickHandler and show it afterwards
        final AlertDialog alert = new AlertDialog.Builder(context)
                .setView(input)
                .setTitle(dialogData.getStringRenameDialogTitle())
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button = alert.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String alias = input.getText().toString();
                        if (alias.length() < dialogData.getMinAliasLength()) {
                            showToast(context, dialogData.getStringAliasTooShort());
                            return;
                        }
                        // Notify listener and close dialog if requested by return value
                        if (listener.onConfirmAlias(alias)) {
                            alert.dismiss();
                        }
                    }
                });
            }
        });

        alert.show();
    }

    private static void showToast(Context context, @StringRes int resId) {
        Toast.makeText(context, resId, Toast.LENGTH_LONG).show();
    }

    private static void showToast(Context context, CharSequence text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    /**
     * Listener for handling clicks to the ok button of the alias (re)name dialog.
     */
    public interface OnConfirmAliasListener {
        /**
         * Gets called when the entered alias has the correct length and the ok button was pressed.
         *
         * @param newAlias The (new) alias the user entered.
         * @return true if you want the dialog to close or false to let it stay open.
         */
        boolean onConfirmAlias(String newAlias);
    }
}