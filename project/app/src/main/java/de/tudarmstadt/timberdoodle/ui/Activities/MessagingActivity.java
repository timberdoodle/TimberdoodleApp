package de.tudarmstadt.timberdoodle.ui.Activities;

import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import de.tudarmstadt.adtn.ui.passworddialog.PasswordDialog;
import de.tudarmstadt.timberdoodle.IService;
import de.tudarmstadt.timberdoodle.LocationPrivacy;
import de.tudarmstadt.timberdoodle.R;
import de.tudarmstadt.timberdoodle.ui.PrivateKeyStorePasswordDialog;
import de.tudarmstadt.timberdoodle.ui.TabsPagerAdapter;

/**
 * Contains the public and private messaging part.
 */
public class MessagingActivity extends TimberdoodleActivity {
    FragmentPagerAdapter adapterViewPager;

    @Override
    public void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);
        setContentView(R.layout.activity_messaging);
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