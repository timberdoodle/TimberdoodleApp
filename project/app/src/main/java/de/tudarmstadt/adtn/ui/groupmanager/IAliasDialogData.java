package de.tudarmstadt.adtn.ui.groupmanager;

import android.support.annotation.StringRes;

/**
 * Defines getters for string resource IDs which are needed to use a key alias (re)naming dialog.
 */
public interface IAliasDialogData {

    /**
     * @return The minimum length of a valid key alias.
     */
    int getMinAliasLength();

    /**
     * @return The maximum length of a valid key alias.
     */
    int getMaxAliasLength();

    /**
     * @return The ID of the string to show if the new alias is too short.
     */
    @StringRes
    int getStringAliasTooShort();

    /**
     * @return The ID of the rename dialog title string.
     */
    @StringRes
    int getStringRenameDialogTitle();
}
