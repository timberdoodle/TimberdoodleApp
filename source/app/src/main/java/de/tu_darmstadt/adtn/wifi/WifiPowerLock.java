package de.tu_darmstadt.adtn.wifi;

import android.content.Context;
import android.net.wifi.WifiManager;

/**
 * A class that can be used to prevent Wi-Fi going into power saving mode.
 */
public class WifiPowerLock {

    private final WifiManager.WifiLock wifiLock;

    /**
     * Creates a new WifiPowerLock object.
     *
     * @param context The context to use.
     */
    public WifiPowerLock(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "WifiLock");
    }

    /**
     * Disables Wifi power saving until unlockPowerSaving gets called.
     */
    public void lockPowerSaving() {
        wifiLock.acquire();
    }

    /**
     * Reverts lockPowerSaving.
     */
    public void unlockPowerSaving() {
        wifiLock.release();
    }
}
