package de.tudarmstadt.adtn.ui.groupmanager;

import android.support.annotation.StringRes;

import java.util.Collection;

import de.tudarmstadt.adtn.generickeystore.KeyStoreEntry;

/**
 * Defines the callbacks needed by the KeyManagementFragmentHelper.
 */
public interface IKeyManagement extends IAliasDialogData {

    /**
     * @return The collection of keys the fragment should display.
     */
    Collection<? extends KeyStoreEntry<?>> getKeys();

    /**
     * Will be called after the user entered an alias with correct length and pressed ok in the
     * rename dialog.
     *
     * @param id       The ID of the entry to rename.
     * @param newAlias The new alias the user wants to assign.
     * @return The specified ID if renaming was successful, 0 if the ID is invalid or an ID that is
     * neither the specified ID nor 0 if the new alias is already in use.
     */
    long renameKey(long id, String newAlias);

    /**
     * Will be called when the user wants to delete one or more keys.
     *
     * @param ids The IDs to delete.
     */
    void deleteKeys(Collection<Long> ids);

    /**
     * Determines if the "share" context menu entry for a key should be shown.
     *
     * @param id The ID of the key.
     * @return true if this key can be shared or false otherwise.
     */
    boolean allowSharing(long id);

    /**
     * Gets called when the user wants to share a key.
     *
     * @param id The ID of the key the user wants to share.
     */
    void shareKey(long id);

    /**
     * @return The ID of the string to show if the new name is already in use when renaming.
     */
    @StringRes
    int getStringAliasExists();

    /**
     * @return The ID of the string to show when the entry to rename is gone.
     */
    @StringRes
    int getStringEntryIsGone();

    /**
     * @return The ID of the string shown in the message where a the user should confirm that he
     * wants to delete a single key entry. %s will be replaced by the alias of the to delete.
     */
    @StringRes
    int getStringConfirmDeleteSingle();

    /**
     * @return The ID of the string shown in the message where a the user should confirm that he
     * wants to delete multiple keys entries. %d will be replaced by the number of keys to delete.
     */
    @StringRes
    int getStringConfirmDeleteMultiple();

    /**
     * @return The ID of the string shown after a single key entry was deleted. %s will be replaced
     * by the alias of deleted key.
     */
    @StringRes
    int getStringDeletedSingle();

    /**
     * @return The ID of the string shown after multiple key entries were deleted. %d will be
     * replaced by the number of deleted keys.
     */
    @StringRes
    int getStringDeletedMultiple();
}
