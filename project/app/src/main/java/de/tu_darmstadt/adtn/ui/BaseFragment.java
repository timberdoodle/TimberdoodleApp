package de.tu_darmstadt.adtn.ui;

import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import de.tu_darmstadt.timberdoodle.R;

/**
 * Creates the hint button (if a help string is set) and provides support for fragment transition.
 */
public class BaseFragment extends Fragment {

    // The string table ID of the help string
    private
    @StringRes
    int helpStringResId;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.help_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != R.id.menu_help) return super.onOptionsItemSelected(item);

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

    /**
     * Transition between fragments.
     *
     * @param fragment The new fragment.
     * @param replace  true to replace the current fragment with the new one or false to keep the
     *                 old fragment and push the new one.
     */
    /*public void fragmentTransition(Fragment fragment, boolean replace) {
        // Obtain ID of container view
        View view = getView();
        assert view != null;
        @IdRes int containerViewId = ((ViewGroup) view.getParent()).getId();

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.getBackStackEntryAt()

        // Transition
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (replace) {
            transaction.replace(containerViewId, fragment);
        } else {
            transaction.add(containerViewId, fragment);
        }
        transaction.addToBackStack(null);
        transaction.commit();
    }*/
}
