package de.tudarmstadt.timberdoodle.ui.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import de.tudarmstadt.adtn.ui.BaseFragment;
import de.tudarmstadt.timberdoodle.IService;
import de.tudarmstadt.timberdoodle.Service;
import de.tudarmstadt.timberdoodle.TimberdoodleServiceConnection;

public abstract class DoodleFragment extends BaseFragment {

    // The service connection that gets notified when the service is bound
    private final TimberdoodleServiceConnection serviceConnection = new TimberdoodleServiceConnection() {
        @Override
        public void onTimberdoodleServiceReady(Service service) {
            // Store reference to service object and notify callbacks that service is ready
            DoodleFragment.this.service = service;
            DoodleFragment.this.onTimberdoodleServiceReady(service);
            View view = getView();
            if (view != null) onViewAndServiceReady(view, service);
        }
    };

    // Reference to the Timberdoodle service. null if not ready yet.
    private IService service;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!activity.bindService(new Intent(activity, Service.class), serviceConnection, Context.BIND_AUTO_CREATE)) {
            throw new RuntimeException("Could not bind to Timberdoodle service");
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
        if (service != null) onViewAndServiceReady(view, service);
    }

    /**
     * Override to get notified when the Timberdoodle service is ready.
     */
    protected void onTimberdoodleServiceReady(IService service) {
    }

    /**
     * Override to get notified when the View is created and the Timberdoodle service is ready.
     * Note that this function gets called from the service callback if onCreateView finished
     * before the service was bound or from onCreateView if the service callback finished before
     * onCreateView was called.
     *
     * @param view    The View of the fragment.
     * @param service The Timberdoodle service object.
     */
    protected void onViewAndServiceReady(View view, IService service) {
    }

    /**
     * @return A reference to the Timberdoodle service object if already bound or null otherwise.
     */
    public IService getService() {
        return service;
    }

    boolean wasPaused = false;

    @Override
    public void onResume() {
        super.onResume();
        if (wasPaused) {
            setHasOptionsMenu(getService().getAdtnService().getPreferences().getShowHelpButtons());
            wasPaused = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        wasPaused = true;
    }
}
