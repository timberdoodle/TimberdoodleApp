package de.tu_darmstadt.adtn.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v4.content.LocalBroadcastManager;

import com.thinktube.adhocconfigdemo.WifiConfigurationNew;
import com.thinktube.adhocconfigdemo.WifiManagerNew;

import java.net.InetAddress;
import java.util.List;
import java.util.Random;

/**
 * Keeps the connection to the IBSS ad-hoc network alive.
 */
public class IbssNetwork {

    /**
     * The action which gets broadcasted if the IBSS network could not be established.
     */
    public final static String ACTION_HANDLE_IBSS_ERROR = "de.tu_darmstadt.adtn.adhoc.IIbssNetwork.ACTION_HANDLE_IBSS_ERROR";

    private final static String SSID = "\"TimberdoodleIBSS\"";
    private final static int FREQUENCY = 2442;
    private final static IntentFilter INTENT_FILTER = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);

    private final Context context;
    private final BroadcastReceiver broadcastReceiver; // Listens for Wi-fi status change
    private final LocalBroadcastManager localBroadcastManager; // For sending failure broadcast
    private final WifiManager wifiManager; // The classic Wi-Fi manager
    private final WifiManagerNew wifiManagerNew; // The IBSS Wi-Fi manager

    // If true,
    private boolean ibssEnabled = false;
    private boolean failed = false;

    /**
     * Creates a new IBSS connection manager.
     *
     * @param context The context to use.
     */
    public IbssNetwork(Context context) {
        this.context = context;

        localBroadcastManager = LocalBroadcastManager.getInstance(context);

        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        /* Wrap WifiManager to access new methods */
        wifiManagerNew = new WifiManagerNew(wifiManager);

        /* Register broadcast receiver to get notified when Wifi has been enabled */
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                    int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                    if (state == WifiManager.WIFI_STATE_ENABLED) onWifiEnabled();
                }
            }
        };
    }

    /**
     * Starts trying keeping the ad-hoc network alive
     */
    public synchronized void start() {
        // If already enabled, to nothing except sending a broadcast indicating failure on error
        if (ibssEnabled) {
            if (failed) sendFailureBroadcast();
            return;
        }

        // Register Wi-Fi status broadcast receiver and set up ad-hoc network
        ibssEnabled = true;
        context.registerReceiver(broadcastReceiver, INTENT_FILTER);
        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) configureAdHocNetwork();
    }

    /**
     * Stops trying keeping the ad-hoc network alive
     */
    public synchronized void stop() {
        if (!ibssEnabled) return;
        ibssEnabled = false;
        context.unregisterReceiver(broadcastReceiver);
        removeNetwork();
    }

    // Will be called by the broadcast receiver if Wi-Fi gets enabled
    private synchronized void onWifiEnabled() {
        if (ibssEnabled) configureAdHocNetwork();
    }

    // Removes any existing Timberdoodle wifi configurations
    private void removeNetwork() {
        List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
        if (configs == null) return;

        for (WifiConfiguration config : configs) {
            if (config.SSID.equals(SSID)) wifiManager.removeNetwork(config.networkId);
        }
    }

    // Config it
    private synchronized void configureAdHocNetwork() {
        // Delete old network configuration
        removeNetwork();

        try {
            if (!wifiManagerNew.isIbssSupported()) throw new Exception("IBSS not supported");

            /* We use WifiConfigurationNew which provides a way to access
             * the Ad-hoc mode and static IP configuration options which are
             * not part of the standard API yet */
            WifiConfigurationNew wifiConfig = new WifiConfigurationNew();

            // Set the SSID and security as normal
            wifiConfig.SSID = SSID;
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

            // Use reflection until API is official
            wifiConfig.setIsIBSS(true);
            wifiConfig.setFrequency(FREQUENCY);

            // Use reflection to configure static IP addresses
            wifiConfig.setIpAssignment("STATIC");
            Random rnd = new Random();
            String ip = "10." + rnd.nextInt(256) + "." + rnd.nextInt(256) + "." + rnd.nextInt(255);
            wifiConfig.setIpAddress(InetAddress.getByName(ip), 8);
            wifiConfig.setGateway(InetAddress.getByName("0.0.0.0"));
            wifiConfig.setDNS(InetAddress.getByName("0.0.0.0"));

            // Add, enable and save network as normal
            int id = wifiManager.addNetwork(wifiConfig);
            if (id < 0) {
                throw new Exception("Could not register network configuration");
            } else {
                if (!wifiManager.enableNetwork(id, true)) {
                    throw new Exception("Could not enable network");
                }
                wifiManager.saveConfiguration();
            }
        } catch (Exception e) {
            failed = true;
            sendFailureBroadcast();
        }

        failed = false;
    }

    // Send a local broadcast that indicates there was an error while establishing the IBSS network
    private void sendFailureBroadcast() {
        localBroadcastManager.sendBroadcast(new Intent(ACTION_HANDLE_IBSS_ERROR));
    }
}
