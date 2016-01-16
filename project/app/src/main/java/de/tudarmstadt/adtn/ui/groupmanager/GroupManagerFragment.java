package de.tudarmstadt.adtn.ui.groupmanager;

import android.support.v4.app.Fragment;

import de.tudarmstadt.adtn.ui.AdtnFragment;

/**
 * Base class of all group manager fragments.
 */
public abstract class GroupManagerFragment extends AdtnFragment {

    /**
     * @return true if this fragment should be stored in the back stack if a fragment transition
     * is caused from outside.
     */
    public boolean getAddToBackStack() {
        return true;
    }

    protected void goToFragment(Fragment newFragment, boolean addToBackStack) {
        ((GroupManagerActivity) getActivity()).goToFragment(newFragment, addToBackStack);
    }
}
