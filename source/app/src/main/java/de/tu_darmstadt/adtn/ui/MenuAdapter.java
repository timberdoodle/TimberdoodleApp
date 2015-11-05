package de.tu_darmstadt.adtn.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.tu_darmstadt.timberdoodle.R;

/**
 * An adapter for navigation menu entries.
 */
public class MenuAdapter extends BaseAdapter {

    private final Context context;
    private final LayoutInflater layoutInflater;
    private final List<MenuEntry> entries;
    private boolean showHelpButtons = true;

    /**
     * Creates a new MenuAdapter object.
     *
     * @param context A Context object to resolve string resources and to access the layout inflater
     *                service.
     * @param entries The entries contained in the menu.
     */
    public MenuAdapter(Context context, List<MenuEntry> entries) {
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.entries = entries;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView;
        MenuEntry entry = entries.get(position);

        if (entry == null) { // Empty separator entry?
            itemView = layoutInflater.inflate(R.layout.drawer_list_item, parent, false);
            ((ImageView) itemView.findViewById(R.id.icon_navigation)).setImageResource(0);
            ((TextView) itemView.findViewById(R.id.title_navigation)).setText("");
        } else if (entry.getIcon() == 0 && showHelpButtons) { // Header entry?
            itemView = layoutInflater.inflate(R.layout.drawler_list_header, parent, false);
            ((TextView) itemView.findViewById(R.id.header_navigation)).setText(entry.getCaption());
            ImageView imgIcon = (ImageView) itemView.findViewById(R.id.icon_help);
            imgIcon.setImageResource(R.drawable.ic_help_black_24dp);
        } else { // Normal entry?
            itemView = layoutInflater.inflate(R.layout.drawer_list_item, parent, false);
            ImageView imgIcon = (ImageView) itemView.findViewById(R.id.icon_navigation);
            imgIcon.setImageResource(entry.getIcon());
            ((TextView) itemView.findViewById(R.id.title_navigation)).setText(entry.getCaption());
        }

        return itemView;
    }

    @Override
    public int getCount() {
        return entries.size();
    }

    @Override
    public String getItem(int position) {
        return context.getString(entries.get(position).getCaption());
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public MenuEntry getEntry(int position) {
        return entries.get(position);
    }

    /**
     * Enable or Disable help buttons on navigation
     * @param showHelpButtons
     */
    public void setShowHelpButtons(boolean showHelpButtons) {
        this.showHelpButtons = showHelpButtons;
        notifyDataSetChanged();
    }
}