package de.tu_darmstadt.timberdoodle.ui.contactmanager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.tu_darmstadt.adtn.ui.MenuEntry;
import de.tu_darmstadt.adtn.ui.NavigationActivity;
import de.tu_darmstadt.timberdoodle.R;

public class ContactManagerActivity extends NavigationActivity {

    private final static String EXTRA_FRAGMENT_CLASS = "fragment";

    public final static List<MenuEntry> NAV_ENTRIES = Collections.unmodifiableList(Arrays.asList(
            MenuEntry.createHelpEntry(R.string.nav_friend_management, R.string.help_7),
            new MenuEntry(R.string.nav_friend_list, R.drawable.ic_vpn_key_black_24dp, createMenuHandler(FriendKeyManagementFragment.class)),
            new MenuEntry(R.string.nav_add_friend, R.drawable.ic_group_add_black_24dp, createMenuHandler(ScanFriendKeyFragment.class)),
            new MenuEntry(R.string.nav_send_public_key, R.drawable.ic_add_to_photos_black_24dp, createMenuHandler(ShareFriendKeyFragment.class))));

    private static MenuEntry.OnClickListener createMenuHandler(final Class<? extends ContactManagerFragment> fragmentClass) {
        return new MenuEntry.OnClickListener() {
            @Override
            public boolean onClick(Context context) {
                // Clicked from the menu of this activity?
                if (context instanceof ContactManagerActivity) {
                    ((ContactManagerActivity) context).goToFragmentFromMenu(fragmentClass);
                } else { // Clicked from a foreign activity?
                    Intent intent = new Intent(context, ContactManagerActivity.class);
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
        if (savedInstanceState == null) handleIntent();
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
        Class<? extends ContactManagerFragment> fragmentClass =
                (Class<? extends ContactManagerFragment>) getIntent().getSerializableExtra(EXTRA_FRAGMENT_CLASS);

        // Go to specified fragment or to friend key management if not specified
        if (fragmentClass == null) fragmentClass = FriendKeyManagementFragment.class;
        goToFragmentFromMenu(fragmentClass);
    }

    private void goToFragmentFromMenu(Class<? extends ContactManagerFragment> fragmentClass) {
        // No fragments created yet?
        ContactManagerFragment currentFragment = (ContactManagerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment == null) {
            Fragment fragment = Fragment.instantiate(this, fragmentClass.getName());
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();
            return;
        }

        // Do nothing if fragment to go to is the same as the current one
        if (currentFragment.getClass().equals(fragmentClass)) return;

        // Instantiate new fragment and show it
        goToFragment(Fragment.instantiate(this, fragmentClass.getName()), currentFragment.getAddToBackStack());
    }

    void goToFragment(Fragment newFragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newFragment);
        if (addToBackStack) transaction.addToBackStack(null);
        transaction.commit();
    }
}
