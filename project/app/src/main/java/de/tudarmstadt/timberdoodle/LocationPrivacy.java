package de.tudarmstadt.timberdoodle;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;

/**
 * Static class for enabling or disabling location privacy.
 */
public class LocationPrivacy {

    /**
     * @return true if location privacy is enabled or false otherwise.
     */
    public static boolean isEnabled(Context context) {
        return isAirplaneModeOn(context) && isWifiModeOn(context);
    }

    /**
     * Enables location privacy.
     *
     * @param context     The context to use.
     * @param runWhenDone This method returns immediately but enabling location privacy mode takes
     *                    probably longer. runWhenDone lets you specify a callback to execute when
     *                    enabling location privacy mode is done.
     */
    public static void enable(final Context context, final Runnable runWhenDone) {
        if (!isAirplaneModeOn(context)) executeCmd(true);

        // Wait some time after enable airplane mode
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // Enable Wi-Fi and wait
                setWifiEnabled(context, true);
                handler.postDelayed(runWhenDone, 4000);
            }
        }, 5000);
    }

    /**
     * Disable location privacy.
     *
     * @param context The context to use.
     */
    public static void disable(Context context) {
        if (!isEnabled(context)) return;

        setWifiEnabled(context, false);
        executeCmd(false);
    }

    // Enables or disables Wi-Fi
    private static void setWifiEnabled(Context context, boolean enabled) {
        ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).setWifiEnabled(enabled);
    }

    // Returns true if airplane mode is enabled or false otherwise
    private static boolean isAirplaneModeOn(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
    }

    // Returns true if Wi-Fi is enabled or false otherwise
    private static boolean isWifiModeOn(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(), Settings.System.WIFI_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(context.getContentResolver(), Settings.Global.WIFI_ON, 0) != 0;
        }
    }

    // Run command that enables or disables airplane mode
    private static void executeCmd(boolean enable) {
        final String commandEnable = "settings put global airplane_mode_on 1; am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true";
        final String commandDisable = "settings put global airplane_mode_on 0; am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false";
    }
}
