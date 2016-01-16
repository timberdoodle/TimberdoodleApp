package de.tudarmstadt.adtn.ui.groupmanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import de.tudarmstadt.timberdoodle.R;

public class KeyManagementFragmentHelper {

    private KeyListAdapter listAdapter;
    private IKeyManagement keyManagement;
    private Context context;

    /**
     * Call this function from the key management fragment when the view is created an the ADTN
     * service is bound.
     *
     * @param view          The view of the fragment.
     * @param preselectId   The ID of the entry to scroll to or 0 if not needed.
     * @param keyManagement Configures the key management fragment.
     */
    public void onViewAndServiceReady(View view, long preselectId, IKeyManagement keyManagement) {
        context = view.getContext();
        this.keyManagement = keyManagement;

        // Create, populate and assign the adapter for the ListView
        listAdapter = new KeyListAdapter(view.getContext());
        refreshList();
        ListView listView = (ListView) view.findViewById(R.id.keyListView);
        listView.setAdapter(listAdapter);

        // Set up context menu and multi-select handler
        setUpContextMenu(listView, listAdapter);
        setUpMultipleChoiceListener(listView);

        // Preselect (i.e. scroll to) key entry (if specified)
        if (preselectId != 0) {
            int position = listAdapter.getPosition(preselectId);
            if (position != -1) listView.smoothScrollToPosition(position);
        }
    }

    /**
     * Inflates the ListView and the view to display when it is empty
     *
     * @param inflater    The LayoutInflater object that can be used to inflate
     *                    any views in the fragment,
     * @param container   If non-null, this is the parent view that the fragment's
     *                    UI should be attached to.  The fragment should not add the view itself,
     *                    but this can be used to generate the LayoutParams of the view.
     * @param view        The view for the fragment's UI.
     * @param emptyViewId The layout ID of the view that gets shown when the list view is empty.
     */
    public void inflateListView(LayoutInflater inflater, @Nullable ViewGroup container, View view, @LayoutRes int emptyViewId) {
        ListView listView = (ListView) view.findViewById(R.id.keyListView);
        View emptyView = inflater.inflate(emptyViewId, container, false);
        ((ViewGroup) listView.getParent()).addView(emptyView);
        listView.setEmptyView(emptyView);
    }

    private void setUpContextMenu(final ListView listView, final KeyListAdapter adapter) {
        // context menu
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, final long id) {
                // Create dialog items
                ArrayList<String> items = new ArrayList<>(3);
                final int ITEMINDEX_RENAME = items.size();
                items.add(context.getString(R.string.menu_rename));
                final int ITEMINDEX_DELETE = items.size();
                items.add(context.getString(R.string.menu_delete));
                final int ITEMINDEX_SHARE = items.size();
                if (keyManagement.allowSharing(id)) {
                    items.add(context.getString(R.string.menu_share));
                }

                // Create the
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(adapter.getAlias(position));
                builder.setItems(items.toArray(new String[items.size()]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == ITEMINDEX_RENAME) {
                            showRename(id);
                        } else if (which == ITEMINDEX_DELETE) {
                            showConfirmDeleteEntries(Collections.singleton(id), null);
                        } else if (which == ITEMINDEX_SHARE) {
                            keyManagement.shareKey(id);
                        }
                    }
                });
                builder.setCancelable(true).show();
            }
        });
    }

    private void setUpMultipleChoiceListener(final ListView listView) {
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            // List item selected or deselected
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int position, long id, boolean checked) {
                int selectedCount = listView.getCheckedItemCount();
                actionMode.setSubtitle(selectedCount == 0 ? null : Integer.toString(selectedCount));
            }

            // Get action bar from xml
            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                actionMode.getMenuInflater().inflate(R.menu.key_management_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            // handle custom action bar
            @Override
            public boolean onActionItemClicked(final ActionMode actionMode, MenuItem menuItem) {
                showConfirmDeleteEntries(getSelectedIds(), actionMode);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
            }

            private Collection<Long> getSelectedIds() {
                ArrayList<Long> selectedIds = new ArrayList<>(listView.getCheckedItemCount());

                SparseBooleanArray booleanArray = listView.getCheckedItemPositions();
                for (int i = 0; i < listView.getCount(); i++) {
                    if (booleanArray.get(i)) selectedIds.add(listView.getItemIdAtPosition(i));
                }

                return selectedIds;
            }
        });
    }

    // Shows the rename dialog for the key with the specified ID
    private void showRename(final long id) {
        Helper.showAliasInputDialog(context, keyManagement,
                new Helper.OnConfirmAliasListener() {
                    @Override
                    public boolean onConfirmAlias(String newAlias) {
                        // Try to change name and check if it is already in use
                        long renameResult = keyManagement.renameKey(id, newAlias);
                        if (renameResult == 0) { // ID is no longer valid?
                            Toast.makeText(context, keyManagement.getStringEntryIsGone(), Toast.LENGTH_SHORT).show();
                            return false;
                        } else if (renameResult != id) { // Alias already in use?
                            Toast.makeText(context, keyManagement.getStringAliasExists(), Toast.LENGTH_LONG).show();
                            return false;
                        }

                        refreshList();
                        return true;
                    }
                });
    }

    private void showConfirmDeleteEntries(final Collection<Long> ids, final ActionMode actionMode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        String alias = null; // Holds the alias of the key if only one key should be deleted

        // Show number of selected keys in confirmation dialog if more than one key is selected
        if (ids.size() == 1) {
            alias = listAdapter.getAliasById(ids.iterator().next());
            builder.setMessage(context.getString(keyManagement.getStringConfirmDeleteSingle(), alias));
        } else {
            builder.setMessage(context.getString(keyManagement.getStringConfirmDeleteMultiple(), ids.size()));
        }

        // Set up delete confirmation button
        final String finalAlias = alias;
        builder.setPositiveButton(R.string.delete_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                keyManagement.deleteKeys(ids);
                refreshList();

                // Display confirmation message in toast
                String confirmationMessage;
                if (ids.size() == 1) { // Show key name if only one key was deleted
                    confirmationMessage = context.getString(keyManagement.getStringDeletedSingle(), finalAlias);
                } else { // Show number of deleted keys if multiple keys were deleted
                    confirmationMessage = context.getString(keyManagement.getStringDeletedMultiple(), ids.size());
                }
                Toast.makeText(context, confirmationMessage, Toast.LENGTH_LONG).show();

                if (actionMode != null) actionMode.finish();
            }
        });

        builder.setNegativeButton(android.R.string.cancel, null).show();
    }

    private void refreshList() {
        listAdapter.refresh(keyManagement.getKeys());
    }
}
