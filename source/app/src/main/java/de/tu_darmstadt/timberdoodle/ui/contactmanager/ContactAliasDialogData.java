package de.tu_darmstadt.timberdoodle.ui.contactmanager;

import android.support.annotation.StringRes;

import de.tu_darmstadt.adtn.ui.groupmanager.IAliasDialogData;
import de.tu_darmstadt.timberdoodle.R;
import de.tu_darmstadt.timberdoodle.friendkeystore.IFriendKeyStore;

/**
 * Alias dialog data for a contact (re)naming alias dialog.
 */
public class ContactAliasDialogData implements IAliasDialogData {

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
        return IFriendKeyStore.MAX_LENGTH_FRIEND_NAME;
    }

    /**
     * @return The ID of the string to show if the new alias is too short.
     */
    @Override
    public
    @StringRes
    int getStringAliasTooShort() {
        return R.string.friendLengthWarning;
    }

    /**
     * @return The ID of the rename dialog title string.
     */
    @Override
    public
    @StringRes
    int getStringRenameDialogTitle() {
        return R.string.input_friendname;
    }
}
