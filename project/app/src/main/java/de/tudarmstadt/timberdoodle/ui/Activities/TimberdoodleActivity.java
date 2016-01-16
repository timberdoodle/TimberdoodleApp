package de.tudarmstadt.timberdoodle.ui.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

import de.tudarmstadt.adtn.ui.MenuEntry;
import de.tudarmstadt.adtn.ui.NavigationActivity;
import de.tudarmstadt.adtn.ui.groupmanager.GroupManagerActivity;
import de.tudarmstadt.timberdoodle.IService;
import de.tudarmstadt.timberdoodle.R;
import de.tudarmstadt.timberdoodle.Service;
import de.tudarmstadt.timberdoodle.TimberdoodleServiceConnection;
import de.tudarmstadt.timberdoodle.ui.contactmanager.ContactManagerActivity;

/**
 * Base class for activities which automatically binds to the Timberdoodle service.
 */
public abstract class TimberdoodleActivity extends NavigationActivity {

    @Override
    protected void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);

        // Create navigation menu entries
        ArrayList<MenuEntry> menuEntries = new ArrayList<>();
        menuEntries.add(new MenuEntry(R.string.nav_messaging, R.drawable.ic_message_black_24dp, new MenuEntry.OnClickListener() {
            @Override
            public boolean onClick(Context context) {
                context.startActivity(new Intent(context, MessagingActivity.class));
                return true;
            }
        }));
        menuEntries.addAll(GroupManagerActivity.NAV_ENTRIES);
        menuEntries.addAll(ContactManagerActivity.NAV_ENTRIES);
        menuEntries.add(null);
        menuEntries.add(new MenuEntry(R.string.nav_settings, R.drawable.ic_settings_black_24dp, new MenuEntry.OnClickListener() {
            @Override
            public boolean onClick(Context context) {
                context.startActivity(new Intent(context, SettingsActivity.class));
                return true;
            }
        }));
        menuEntries.trimToSize();
        setMenuEntries(menuEntries);

        // Bind to Timberdoodle service
        bindTimberdoodleService(serviceConnection);
    }

    @Override
    protected void onDestroy() {
        unbindService(serviceConnection);

        super.onDestroy();
    }

    //region Service binding

    private final TimberdoodleServiceConnection serviceConnection = new TimberdoodleServiceConnection() {
        @Override
        public void onTimberdoodleServiceReady(Service service) {
            TimberdoodleActivity.this.service = service;
            TimberdoodleActivity.this.onTimberdoodleServiceReady(service);
        }
    };

    private IService service;

    /**
     * Binds to the Timberdoodle service.
     *
     * @param serviceConnection The service connection to use in the call to bindService.
     */
    public void bindTimberdoodleService(TimberdoodleServiceConnection serviceConnection) {
        if (!bindService(new Intent(this, Service.class), serviceConnection, BIND_AUTO_CREATE)) {
            throw new RuntimeException("Could not bind to service");
        }
    }

    /**
     * * Override to get notified when the Timberdoodle service is ready.
     *
     * @param service The Timberdoodle service object.
     */
    protected void onTimberdoodleServiceReady(IService service) {
    }

    /**
     * @return A reference to the Timberdoodle service object if already bound or null otherwise.
     */
    public IService getService() {
        return service;
    }

    //endregion
}
