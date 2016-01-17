package de.tudarmstadt.adtn.ui;

import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import de.tudarmstadt.timberdoodle.R;

/**
 * Creates the hint button (if a help string is set) and provides support for fragment transition.
 */
public class BaseFragment extends Fragment {

    // The string table ID of the help string
    @StringRes
    private int helpStringResId;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.help_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != R.id.menu_help) {
            return super.onOptionsItemSelected(item);
        }

        HelpDialog.show(getActivity(), helpStringResId);
        return true;
    }

    /**
     * Enables the "help" button in the fragment's option menu and sets its text.
     *
     * @param resId The string resource ID of the help string.
     */
    protected void setHelpString(@StringRes int resId) {
        helpStringResId = resId;
        setHasOptionsMenu(false);
    }
}
