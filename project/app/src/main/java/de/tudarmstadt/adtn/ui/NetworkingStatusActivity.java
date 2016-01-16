package de.tudarmstadt.adtn.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import de.tudarmstadt.adtn.NetworkingStatus;
import de.tudarmstadt.adtn.Service;
import de.tudarmstadt.timberdoodle.R;

public class NetworkingStatusActivity extends AppCompatActivity {

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, final IBinder service) {
            final Service adtnService = ((Service.LocalBinder) service).getService();
            NetworkingStatus status = adtnService.getNetworkingStatus();

            // Set up button click handler
            findViewById(R.id.restart_networking).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adtnService.startNetworking();
                    finish();
                }
            });

            // Make restart button only visible on error and prepare text
            String text;
            if (status.getStatus() == NetworkingStatus.STATUS_ENABLED) {
                setRestartButtonVisibility(false);
                text = getString(R.string.networking_enabled);
            } else if (status.getStatus() == NetworkingStatus.STATUS_DISABLED) {
                setRestartButtonVisibility(false);
                text = getString(R.string.networking_disabled);
            } else {
                setRestartButtonVisibility(true);
                text = getString(R.string.networking_error) + "\n" +
                        status.getErrorMessage();
            }

            // Set text
            ((TextView) findViewById(R.id.status_text)).setText(text);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            throw new RuntimeException("aDTN service disconnected unexpectedly");
        }
    };

    // Shows or hides the restart button
    private void setRestartButtonVisibility(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        findViewById(R.id.restart_networking).setVisibility(visibility);
        findViewById(R.id.separator_line).setVisibility(visibility);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_networking_status);

        // Bind to aDTN service
        if (!bindService(new Intent(this, de.tudarmstadt.adtn.Service.class),
                serviceConnection, BIND_AUTO_CREATE)) {
            throw new RuntimeException("Could not bind to aDTN service");
        }
    }

    @Override
    protected void onDestroy() {
        unbindService(serviceConnection);

        super.onDestroy();
    }
}
