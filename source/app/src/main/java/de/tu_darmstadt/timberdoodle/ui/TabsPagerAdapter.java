package de.tu_darmstadt.timberdoodle.ui;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import de.tu_darmstadt.timberdoodle.R;
import de.tu_darmstadt.timberdoodle.ui.Fragments.PrivateMessagesFragment;
import de.tu_darmstadt.timberdoodle.ui.Fragments.PublicMessagesFragment;

/*
* An adapter used to create tabs
 */
public class TabsPagerAdapter extends FragmentPagerAdapter {

    private final int TAB_INDEX_PUBLIC_MESSAGES = 0, TAB_INDEX_PRIVATE_MESSAGES = 1;

    private Context context;

    public TabsPagerAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager);
        this.context = context;
    }

    @Override
    public Fragment getItem(int index) {
        if (index == TAB_INDEX_PUBLIC_MESSAGES) return new PublicMessagesFragment();
        if (index == TAB_INDEX_PRIVATE_MESSAGES) return new PrivateMessagesFragment();
        return null;
    }

    // Number of tabs
    @Override
    public int getCount() {
        return 2;
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        if (position == TAB_INDEX_PUBLIC_MESSAGES) return context.getString(R.string.tab_public);
        if (position == TAB_INDEX_PRIVATE_MESSAGES) return context.getString(R.string.tab_private);
        return null;
    }
}
