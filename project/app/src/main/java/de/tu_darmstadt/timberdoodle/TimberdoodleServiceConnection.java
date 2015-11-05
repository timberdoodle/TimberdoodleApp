package de.tu_darmstadt.timberdoodle;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * Specify an implementation of this class to onBind if you need to bind to the Timberdoodle
 * service. Do not override onServiceConnected. Instead, the onTimberdoodleServiceReady should be
 * overridden since in some cases the Timberdoodle service is not ready when onServiceConnected gets
 * called.
 */
public abstract class TimberdoodleServiceConnection implements ServiceConnection, Service.OnServiceReadyListener {

    private Service service;

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        this.service = ((Service.LocalBinder) service).getService();
        this.service.addOnServiceReadyListener(this);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        throw new RuntimeException("Timberdoodle service disconnected unexpectedly");
    }

    // Callback for listener
    @Override
    public void onTimberdoodleServiceReady() {
        onTimberdoodleServiceReady(service);
        service = null;
    }

    /**
     * Override this method to get notified when the Timberdoodle service is ready to use.
     *
     * @param service The Timberdoodle service object.
     */
    public abstract void onTimberdoodleServiceReady(Service service);
}
