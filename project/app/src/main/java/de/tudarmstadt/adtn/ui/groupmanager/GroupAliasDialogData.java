package de.tudarmstadt.adtn.ui.groupmanager;

import android.support.annotation.StringRes;

import de.tudarmstadt.adtn.groupkeystore.IGroupKeyStore;
import de.tudarmstadt.timberdoodle.R;

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
    @StringRes
    public int getStringAliasTooShort() {
        return R.string.groupLengthWarning;
    }

    /**
     * @return The ID of the rename dialog title string.
     */
    @Override
    @StringRes
    public int getStringRenameDialogTitle() {
        return R.string.input_groupname;
    }
}
