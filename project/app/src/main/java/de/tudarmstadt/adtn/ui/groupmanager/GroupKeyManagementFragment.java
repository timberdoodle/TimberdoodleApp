package de.tudarmstadt.adtn.ui.groupmanager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.joda.time.Instant;

import java.util.Collection;

import javax.crypto.SecretKey;

import de.tudarmstadt.adtn.IService;
import de.tudarmstadt.adtn.generickeystore.KeyStoreEntry;
import de.tudarmstadt.adtn.groupkeystore.IGroupKeyStore;
import de.tudarmstadt.adtn.ui.passworddialog.GroupPasswordDialog;
import de.tudarmstadt.adtn.ui.passworddialog.PasswordDialog;
import de.tudarmstadt.timberdoodle.R;

/**
 * Fragment for group key management. Contains a ListView with all friend keys.
 */
public class GroupKeyManagementFragment extends GroupManagerFragment {

    private static final String ARGNAME_PRESELECT = "preselect";
    private KeyManagementFragmentHelper helper;

    /**
     * Creates a new GroupKeyManagementFragment.
     *
     * @return A new GroupKeyManagementFragment.
     */
    public static GroupKeyManagementFragment newInstance() {
        return new GroupKeyManagementFragment();
    }

    /**
     * Creates a new GroupKeyManagementFragment which automatically scrolls to the specified key ID
     * when it is shown.
     *
     * @param preselectId The ID to preselect when the fragment is shown.
     * @return A new GroupKeyManagementFragment.
     */
    public static GroupKeyManagementFragment newInstance(long preselectId) {
        GroupKeyManagementFragment fragment = new GroupKeyManagementFragment();
        Bundle args = new Bundle(1);
        args.putLong(ARGNAME_PRESELECT, preselectId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_key_management, container, false);

        // Set up list view
        helper = new KeyManagementFragmentHelper();
        helper.inflateListView(inflater, container, view, R.layout.key_store_empty_view);

        setHelpString(R.string.help_4);
        setViewReady(view);
        return view;
    }

    @Override
    protected void onViewAndAdtnServiceReady(final View view, final IService service) {
        super.onViewAndAdtnServiceReady(view, service);

        setHasOptionsMenu(service.getPreferences().getShowHelpButtons());

        // Get entry to preselect from arguments, if any
        Bundle args = getArguments();
        final long preselectId = args == null ? 0 : getArguments().getLong(ARGNAME_PRESELECT);

        new GroupPasswordDialog(getActivity(), new PasswordDialog.OnDoneListener() {
            @Override
            public void onDone() {
                // Cancel if group key store is not loaded
                if (service.getGroupKeyStore() == null) {
                    getFragmentManager().popBackStack();
                    return;
                }

                final IGroupKeyStore keyStore = service.getGroupKeyStore();

                // Let the helper do most of the work
                helper.onViewAndServiceReady(view, preselectId, new GroupKeyManagement() {

                    @Override
                    public Collection<KeyStoreEntry<SecretKey>> getKeys() {
                        return keyStore.getEntries();
                    }

                    @Override
                    public long renameKey(long id, String newAlias) {
                        return keyStore.renameEntry(id, newAlias);
                    }

                    @Override
                    public void deleteKeys(Collection<Long> ids) {
                        keyStore.deleteEntries(ids);
                    }

                    @Override
                    public boolean allowSharing(long id) {
                        return getAdtnService().getExpirationManager().getTimestamp(id) != null;
                    }

                    @Override
                    public void shareKey(long id) {
                        // Load key
                        KeyStoreEntry<SecretKey> entry = keyStore.getEntry(id);
                        if (entry == null) { // Group was removed while the menu was shown?
                            Toast.makeText(getActivity(), "Group does not exist any more", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Check if key is too old to share (can happen if user stays in context menu for long time)
                        Instant timestamp = getAdtnService().getExpirationManager().getTimestamp(id);
                        if (timestamp == null) {
                            Toast.makeText(getActivity(), "Group expired for sharing", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Show QR code in new fragment
                        goToFragment(ShareGroupKeyFragment.newInstance(entry.getKey(), timestamp), true);
                    }

                    @Override
                    public int getStringEntryIsGone() {
                        return R.string.group_key_gone;
                    }

                    @Override
                    public int getStringAliasExists() {
                        return R.string.name_already_exist;
                    }

                    @Override
                    public int getStringConfirmDeleteSingle() {
                        return R.string.key_delete_dialog;
                    }

                    @Override
                    public int getStringConfirmDeleteMultiple() {
                        return R.string.more_keys_delete_dialog;
                    }

                    @Override
                    public int getStringDeletedSingle() {
                        return R.string.deleted_toast;
                    }

                    @Override
                    public int getStringDeletedMultiple() {
                        return R.string.multiple_keys_deleted_toast;
                    }
                });
            }
        }, service).show();
    }

    // Needed for anonymous class implementation in onViewAndAdtnServiceReady.
    private abstract class GroupKeyManagement extends GroupAliasDialogData implements IKeyManagement {
    }
}
