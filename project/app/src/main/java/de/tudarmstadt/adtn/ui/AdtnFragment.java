package de.tudarmstadt.adtn.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.View;

import de.tudarmstadt.adtn.IService;
import de.tudarmstadt.adtn.Service;

/**
 * A fragment which automatically binds to the ADTN service when it is created.
 */
public abstract class AdtnFragment extends BaseFragment {

    // The service connection that gets notified when the service is bound
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // Store reference to service object and notify callbacks that service is ready
            AdtnFragment.this.adtnService = ((Service.LocalBinder) service).getService();
            onAdtnServiceReady(adtnService);
            View view = getView();
            if (view != null) {
                onViewAndAdtnServiceReady(view, adtnService);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            throw new RuntimeException("ADTN service disconnected unexpectedly");
        }
    };

    // Reference to the ADTN service. null if not ready yet.
    private IService adtnService;
    boolean wasPaused = false;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!activity.bindService(new Intent(activity, Service.class), serviceConnection, Context.BIND_AUTO_CREATE)) {
            throw new RuntimeException("Could not bind to ADTN service");
        }
    }

    @Override
    public void onDetach() {
        getActivity().unbindService(serviceConnection);

        super.onDetach();
    }

    /**
     * Call this from onCreateView to indicate that the View was created.
     *
     * @param view The created view.
     */
    public void setViewReady(View view) {
        if (adtnService != null) {
            onViewAndAdtnServiceReady(view, adtnService);
        }
    }

    /**
     * Override to get notified when the ADTN service is ready.
     *
     * @param service The ADTN service object.
     */
    protected void onAdtnServiceReady(IService service) {
    }

    /**
     * Override to get notified when the View is created and the ADTN service is ready.
     * Note that this function gets called from the service callback if onCreateView finished
     * before the service was bound or from onCreateView if the service callback finished before
     * onCreateView was called.
     *
     * @param view    The View of the fragment.
     * @param service The ADTN service object.
     */
    protected void onViewAndAdtnServiceReady(View view, IService service) {
    }

    /**
     * @return A reference to the ADTN service object if already bound or null otherwise.
     */
    protected IService getAdtnService() {
        return adtnService;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (wasPaused) {
            setHasOptionsMenu(getAdtnService().getPreferences().getShowHelpButtons());
            wasPaused = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        wasPaused = true;
    }
}
