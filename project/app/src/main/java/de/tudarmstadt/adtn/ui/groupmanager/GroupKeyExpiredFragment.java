package de.tudarmstadt.adtn.ui.groupmanager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.tudarmstadt.timberdoodle.R;

public class GroupKeyExpiredFragment extends GroupManagerFragment {

    /**
     * @return A new GroupKeyExpiredFragment.
     */
    public static GroupKeyExpiredFragment newInstance() {
        return new GroupKeyExpiredFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_expire, container, false);
    }

    @Override
    public boolean getAddToBackStack() {
        return false;
    }
}
