package de.tu_darmstadt.timberdoodle.ui.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import de.tu_darmstadt.adtn.ui.SingleFragmentActivity;

/**
 * Same as SingleFragmentActivity, but does not have launch mode "singleTask" so multiple instances
 * of the same activity can be started.
 */
public class SingleFragmentMultiInstancesActivity extends SingleFragmentActivity {

    /**
     * Creates an intent for starting a SingleFragmentMultiInstancesActivity.
     *
     * @param context       The context to use for creating the event.
     * @param fragmentClass The fragment class to start in the activity.
     * @param fragmentArgs  The argument bundle to pass to the fragment.
     * @param start         If true, startActivity will be called on the Intent before returning.
     * @return The created intent.
     */
    public static Intent newIntent(Context context, Class<? extends Fragment> fragmentClass, Bundle fragmentArgs, boolean start) {
        return newIntent(context, SingleFragmentMultiInstancesActivity.class, fragmentClass, fragmentArgs, start);
    }
}
