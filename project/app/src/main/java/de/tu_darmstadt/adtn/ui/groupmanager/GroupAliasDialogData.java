package de.tu_darmstadt.adtn.ui.groupmanager;

import android.support.annotation.StringRes;

import de.tu_darmstadt.adtn.groupkeystore.IGroupKeyStore;
import de.tu_darmstadt.timberdoodle.R;

/**
 * Alias dialog data for a group (re)naming alias dialog.
 */
public class GroupAliasDialogData implements IAliasDialogData {

    /**
     * @return The minimum length of a valid key alias.
     */
    @Override
    public int getMinAliasLength() {
        return 1;
    }

    /**
     * @return The maximum length of a valid key alias.
     */
    @Override
    public int getMaxAliasLength() {
        return IGroupKeyStore.MAX_LENGTH_GROUP_NAME;
    }

    /**
     * @return The ID of the string to show if the new alias is too short.
     */
    @Override
    public
    @StringRes
    int getStringAliasTooShort() {
        return R.string.groupLengthWarning;
    }

    /**
     * @return The ID of the rename dialog title string.
     */
    @Override
    public
    @StringRes
    int getStringRenameDialogTitle() {
        return R.string.input_groupname;
    }
}
