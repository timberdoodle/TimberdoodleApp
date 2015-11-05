package de.tu_darmstadt.adtn.ui;

import android.content.Context;
import android.support.annotation.StringRes;

/**
 * A menu entry.
 */
public class MenuEntry {

    /**
     * A listener for handling clicks on menu entries.
     */
    public interface OnClickListener {

        /**
         * Gets called when a menu entry is clicked.
         *
         * @param context The context containing the menu entry.
         * @return true if the menu should be closed or false to keep it open.
         */
        boolean onClick(Context context);
    }

    private final
    @StringRes
    int caption;
    private final int icon;
    private final OnClickListener listener;

    /**
     * Creates a new menu entry without icon.
     *
     * @param caption The string ID of the entry's caption.
     */
    public MenuEntry(@StringRes int caption) {
        this(caption, 0);
    }

    /**
     * Creates a new menu entry.
     *
     * @param caption The string ID of the entry's caption.
     * @param icon    The resource ID of the entry's icon.
     */
    public MenuEntry(@StringRes int caption, int icon) {
        this(caption, icon, null);
    }

    /**
     * @param caption  The string ID of the entry's caption.
     * @param icon     The resource ID of the entry's icon
     * @param listener An optional listener object that handles clicks to this menu entry.
     */
    public MenuEntry(@StringRes int caption, int icon, OnClickListener listener) {
        this.caption = caption;
        this.icon = icon;
        this.listener = listener;
    }

    /**
     * @return The string ID of the entry's caption.
     */
    public
    @StringRes
    int getCaption() {
        return caption;
    }

    /**
     * @return The resource ID of the entry's icon or 0 if it has no icon.
     */
    public int getIcon() {
        return icon;
    }

    public OnClickListener getListener() {
        return listener;
    }

    public static MenuEntry createHelpEntry(@StringRes int caption, @StringRes final int helpString) {
        return new MenuEntry(caption, 0, new OnClickListener() {
            @Override
            public boolean onClick(Context context) {
                HelpDialog.show(context, helpString);
                return false; // Keep menu open
            }
        });
    }
}