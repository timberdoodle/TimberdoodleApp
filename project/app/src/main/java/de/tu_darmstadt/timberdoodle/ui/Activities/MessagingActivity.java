package de.tu_darmstadt.timberdoodle.ui.Activities;

import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import de.tu_darmstadt.adtn.Utilities;
import de.tu_darmstadt.adtn.ui.passworddialog.PasswordDialog;
import de.tu_darmstadt.timberdoodle.IService;
import de.tu_darmstadt.timberdoodle.LocationPrivacy;
import de.tu_darmstadt.timberdoodle.R;
import de.tu_darmstadt.timberdoodle.ui.PrivateKeyStorePasswordDialog;
import de.tu_darmstadt.timberdoodle.ui.TabsPagerAdapter;

/**
 * Contains the public and private messaging part.
 */
public class MessagingActivity extends TimberdoodleActivity {
    FragmentPagerAdapter adapterViewPager;

    @Override
    public void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        // Request root permissions when first shown
        if (savedInstanceState == null) {
            if (!Utilities.requestRootPermissions()) {
                Toast.makeText(MessagingActivity.this, R.string.warn_no_superuser, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onTimberdoodleServiceReady(final IService service) {
        super.onTimberdoodleServiceReady(service);

        // Ask user for group and private key store passwords
        new PrivateKeyStorePasswordDialog(this, new PasswordDialog.OnDoneListener() {
            @Override
            public void onDone() {
                // Start networking on privacy mode
                if (LocationPrivacy.isEnabled(MessagingActivity.this)) {
                    service.getAdtnService().startNetworking();
                }

                // Show messaging fragments in ViewPager
                ViewPager vpPager = (ViewPager) findViewById(R.id.vpPager);
                adapterViewPager = new TabsPagerAdapter(getSupportFragmentManager(), MessagingActivity.this);
                vpPager.setAdapter(adapterViewPager);
            }
        }, service, true).show();
    }
}