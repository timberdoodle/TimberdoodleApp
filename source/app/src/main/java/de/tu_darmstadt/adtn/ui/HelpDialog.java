package de.tu_darmstadt.adtn.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.StringRes;

public class HelpDialog {

    // Private constructor only to make class static
    private HelpDialog() {
    }

    /**
     * Shows a dialog containing the string specified by its resource ID.
     *
     * @param context The context to use.
     * @param resId   The resource ID of the string to display.
     */
    public static void show(Context context, @StringRes int resId) {
        new AlertDialog.Builder(context)
                .setMessage(resId)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
