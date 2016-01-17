package de.tudarmstadt.adtn.ui.groupmanager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.tudarmstadt.adtn.ui.MenuEntry;
import de.tudarmstadt.adtn.ui.NavigationActivity;
import de.tudarmstadt.timberdoodle.R;

public class GroupManagerActivity extends NavigationActivity {

    private static final String EXTRA_FRAGMENT_CLASS = "fragment";

    public static final List<MenuEntry> NAV_ENTRIES = Collections.unmodifiableList(Arrays.asList(
            MenuEntry.createHelpEntry(R.string.nav_group_management, R.string.help_3),
            new MenuEntry(R.string.nav_group_list, R.drawable.ic_vpn_key_black_24dp, createMenuHandler(GroupKeyManagementFragment.class)),
            new MenuEntry(R.string.nav_join_group, R.drawable.ic_group_add_black_24dp, createMenuHandler(ScanGroupKeyFragment.class)),
            new MenuEntry(R.string.nav_create_group, R.drawable.ic_add_to_photos_black_24dp, createMenuHandler(NewGroupFragment.class))));

    private static MenuEntry.OnClickListener createMenuHandler(final Class<? extends GroupManagerFragment> fragmentClass) {
        return new MenuEntry.OnClickListener() {
            @Override
            public boolean onClick(Context context) {
                // Clicked from the menu of this activity?
                if (context instanceof GroupManagerActivity) {
                    ((GroupManagerActivity) context).goToFragmentFromMenu(fragmentClass);
                } else { // Clicked from a foreign activity?
                    Intent intent = new Intent(context, GroupManagerActivity.class);
                    intent.putExtra(EXTRA_FRAGMENT_CLASS, fragmentClass);
                    context.startActivity(intent);
                }

                return true; // Close menu
            }
        };
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);
        setMenuEntries(NAV_ENTRIES);
        if (savedInstanceState == null) {
            handleIntent();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent();
    }

    private void handleIntent() {
        // Get fragment class to instantiate from intent extra data
        @SuppressWarnings("unchecked")
        Class<? extends GroupManagerFragment> fragmentClass =
                (Class<? extends GroupManagerFragment>) getIntent().getSerializableExtra(EXTRA_FRAGMENT_CLASS);

        // Go to specified fragment or to group management if not specified
        if (fragmentClass == null) {
            fragmentClass = GroupKeyManagementFragment.class;
        }
        goToFragmentFromMenu(fragmentClass);
    }

    private void goToFragmentFromMenu(Class<? extends GroupManagerFragment> fragmentClass) {
        // No fragments created yet?
        GroupManagerFragment currentFragment = (GroupManagerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment == null) {
            Fragment fragment = Fragment.instantiate(this, fragmentClass.getName());
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();
            return;
        }

        // Do nothing if fragment to go to is the same as the current one
        if (currentFragment.getClass().equals(fragmentClass)) {
            return;
        }

        // Instantiate new fragment and show it
        goToFragment(Fragment.instantiate(this, fragmentClass.getName()), currentFragment.getAddToBackStack());
    }

    void goToFragment(Fragment newFragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newFragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }
}
