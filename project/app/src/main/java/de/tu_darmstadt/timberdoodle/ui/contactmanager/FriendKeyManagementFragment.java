package de.tu_darmstadt.timberdoodle.ui.contactmanager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.security.PublicKey;
import java.util.Collection;

import de.tu_darmstadt.adtn.generickeystore.KeyStoreEntry;
import de.tu_darmstadt.adtn.ui.groupmanager.IKeyManagement;
import de.tu_darmstadt.adtn.ui.groupmanager.KeyManagementFragmentHelper;
import de.tu_darmstadt.timberdoodle.IService;
import de.tu_darmstadt.timberdoodle.R;

/**
 * Fragment for friend key management. Contains a ListView with all friend keys.
 */
public class FriendKeyManagementFragment extends ContactManagerFragment {

    // Needed for anonymous class implementation in onViewAndAdtnServiceReady.
    private abstract class FriendKeyManagement extends ContactAliasDialogData implements IKeyManagement {
    }

    private final static String ARGNAME_PRESELECT = "preselect";

    /**
     * Creates a new FriendKeyManagementFragment.
     *
     * @return A new FriendKeyManagementFragment.
     */
    public static FriendKeyManagementFragment newInstance() {
        return new FriendKeyManagementFragment();
    }

    /**
     * Creates a new FriendKeyManagementFragment which automatically scrolls to the specified key ID
     * when it is shown.
     *
     * @param preselectId The ID to preselect when the fragment is shown.
     * @return A new FriendKeyManagementFragment.
     */
    public static FriendKeyManagementFragment newInstance(long preselectId) {
        FriendKeyManagementFragment fragment = new FriendKeyManagementFragment();
        Bundle args = new Bundle(1);
        args.putLong(ARGNAME_PRESELECT, preselectId);
        fragment.setArguments(args);
        return fragment;
    }

    private KeyManagementFragmentHelper helper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_key_management, container, false);

        // Set up list view
        helper = new KeyManagementFragmentHelper();
        helper.inflateListView(inflater, container, view, R.layout.key_store_empty_view);

        setHelpString(R.string.help_8);
        setViewReady(view);
        return view;
    }

    @Override
    protected void onViewAndServiceReady(View view, IService service) {
        super.onViewAndServiceReady(view, service);

        setHasOptionsMenu(service.getAdtnService().getPreferences().getShowHelpButtons());

        // Get entry to preselect from arguments, if any
        Bundle args = getArguments();
        long preselectId = args == null ? 0 : getArguments().getLong(ARGNAME_PRESELECT);

        // Let the helper do most of the work
        helper.onViewAndServiceReady(view, preselectId, new FriendKeyManagement() {

            @Override
            public Collection<KeyStoreEntry<PublicKey>> getKeys() {
                return getService().getFriendKeyStore().getEntries();
            }

            @Override
            public long renameKey(long id, String newAlias) {
                return getService().getFriendKeyStore().renameEntry(id, newAlias);
            }

            @Override
            public void deleteKeys(Collection<Long> ids) {
                getService().getFriendKeyStore().deleteEntries(ids);
            }

            @Override
            public boolean allowSharing(long id) {
                return false; // Foreign friend keys can never be shared
            }

            @Override
            public void shareKey(long id) {
            }

            @Override
            public int getStringEntryIsGone() {
                return R.string.friend_key_gone;
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
}
