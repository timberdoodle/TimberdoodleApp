package de.tu_darmstadt.adtn.ui.groupmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import de.tu_darmstadt.adtn.generickeystore.KeyStoreEntry;
import de.tu_darmstadt.timberdoodle.R;

/**
 * A list view adapter for key store entries.
 */
public class KeyListAdapter extends BaseAdapter {

    private final LayoutInflater layoutInflater;

    private KeyStoreEntry<?>[] entries;

    /**
     * Creates a new key list adapter.
     *
     * @param context The context to use for obtaining the layout inflater for the ListView items.
     */
    public KeyListAdapter(Context context) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Updates the ListView entries.
     *
     * @param keyStoreEntries The new entries to show.
     */
    public void refresh(Collection<? extends KeyStoreEntry<?>> keyStoreEntries) {
        // Copy entries and sort them alphabetically
        entries = keyStoreEntries.toArray(new KeyStoreEntry<?>[keyStoreEntries.size()]);
        Arrays.sort(entries, new Comparator<KeyStoreEntry<?>>() {
            @Override
            public int compare(KeyStoreEntry<?> lhs, KeyStoreEntry<?> rhs) {
                return lhs.getAlias().compareTo(rhs.getAlias());
            }
        });

        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Store references to message and timestamp TextView in the tag field of the row view
        View view = convertView;

        // Inflate view and store reference to TextView in its tag if not already done
        if (view == null) {
            view = layoutInflater.inflate(R.layout.string_row_item, parent, false);
            view.setTag(view.findViewById(R.id.row_title));
        }

        // Populate row with key alias
        ((TextView) view.getTag()).setText(getAlias(position));

        return view;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public int getCount() {
        return entries.length;
    }

    @Override
    public long getItemId(int position) {
        return entries[position].getId();
    }

    /**
     * Obtains the alias of the ListView item at the specified position.
     *
     * @param position The position of the ListView item.
     * @return The associated alias.
     */
    public String getAlias(int position) {
        return entries[position].getAlias();
    }

    /**
     * Obtains the alias of the key with the specified ID. Note that this is not necessarily the
     * current alias associated with the ID in the key store, since it will use the entries passed
     * in {@link #refresh(Collection)}
     *
     * @param id The ID of the entry to get the alias of.
     * @return The alias of the entry or null if the ID is invalid.
     */
    public String getAliasById(long id) {
        for (KeyStoreEntry<?> entry : entries) {
            if (entry.getId() == id) return entry.getAlias();
        }
        return null;
    }

    /**
     * Obtains the position of the ListView item that displays the key with the specified ID.
     *
     * @param id The ID of the key whose ListView item's position should be obtained.
     * @return The position of the ListView item or -1 if the ID is invalid.
     */
    public int getPosition(long id) {
        for (int i = 0; i < entries.length; ++i) {
            if (entries[i].getId() == id) return i;
        }
        return -1;
    }
}
