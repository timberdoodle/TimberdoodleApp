package de.tu_darmstadt.timberdoodle.ui.contactmanager;

import android.support.v4.app.Fragment;

import de.tu_darmstadt.timberdoodle.ui.Fragments.DoodleFragment;

public class ContactManagerFragment extends DoodleFragment {

    /**
     * @return true if this fragment should be stored in the back stack if a fragment transition
     * is caused from outside.
     */
    public boolean getAddToBackStack() {
        return true;
    }

    protected void goToFragment(Fragment newFragment, boolean addToBackStack) {
        ((ContactManagerActivity) getActivity()).goToFragment(newFragment, addToBackStack);
    }
}
