package de.tudarmstadt.adtn.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import de.tudarmstadt.adtn.errorlogger.ErrorLoggingSingleton;
import de.tudarmstadt.adtn.preferences.Preferences;
import de.tudarmstadt.timberdoodle.R;

/**
 * Inherit from this activity if it should have a navigation menu.
 */
public abstract class NavigationActivity extends AppCompatActivity {

    private static final String TAG = "NavigationActivity";


    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private MenuAdapter menuAdapter;
    private Preferences preferences;
    private boolean wasPaused = false;
    /**
     * Will be called on creation of activity. Override this instead of onCreate.
     *
     * @param savedInstanceState Same as in onCreate.
     */
    protected void init(Bundle savedInstanceState) {
        preferences = new Preferences(this);
    }

    public void setMenuEntries(List<MenuEntry> entries) {
        menuAdapter = new MenuAdapter(this, entries);
        menuAdapter.setShowHelpButtons(preferences.getShowHelpButtons());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init(savedInstanceState);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // Link navigation menu adapter to the ListView and set up its click handler
        drawerList.setAdapter(menuAdapter);
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Do nothing if this is only a separator
                MenuEntry entry = menuAdapter.getEntry(position);
                if (entry == null) {
                    return;
                }
                // Call click handler and close menu if requested
                if (entry.getListener().onClick(view.getContext())) {
                    drawerList.setItemChecked(position, true);
                    drawerLayout.closeDrawer(drawerList);
                }
            }
        });

        // Prepare action bar
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Set NavigationDrawer for ActionBar
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(R.string.app_name);
                supportInvalidateOptionsMenu();
            }
            @Override
            public void onDrawerOpened(View drawerView) {
                menuAdapter.setShowHelpButtons(preferences.getShowHelpButtons());
                getSupportActionBar().setTitle(R.string.app_name);
                supportInvalidateOptionsMenu();
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);

        sendErrorMessage();
    }

    private void sendErrorMessage() {
        final ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
        log.setContext(getApplicationContext());
        if (log.hasError()) {
            new AlertDialog.Builder(this).setMessage(R.string.report_to_devs).
                    setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //get the mail intent from the logger
                            Intent i = log.getErrorMail();
                            log.clearLog();
                            //open app chooser
                            try {
                                //and start mailing app
                                startActivity(Intent.createChooser(i, getResources().getString(R.string.send_mail)));
                            } catch (android.content.ActivityNotFoundException ex) {
                                Log.w(TAG, ex.getMessage(), ex);
                                Toast.makeText(NavigationActivity.this, getResources().getString(R.string.no_mail_clients), Toast.LENGTH_SHORT).show();
                            }
                            dialog.dismiss();
                        }
                    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    log.clearLog();
                    dialog.dismiss();
                }
            }).show();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Open and close NavigationDrawer
        if (item.getItemId() == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(drawerList)) {
                drawerLayout.closeDrawer(drawerList);
            } else {
                drawerLayout.openDrawer(drawerList);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            try { // Workaround android bug
                super.onBackPressed();
            } catch (Exception e) {
                Log.w(TAG, e.getMessage(), e);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (wasPaused) {
            menuAdapter.setShowHelpButtons(preferences.getShowHelpButtons());
            wasPaused = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        wasPaused = true;
    }
}
