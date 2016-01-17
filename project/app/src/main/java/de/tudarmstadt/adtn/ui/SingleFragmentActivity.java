package de.tudarmstadt.adtn.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import de.tudarmstadt.timberdoodle.R;

/**
 * An activity that is just a container for single fragment.
 */
public class SingleFragmentActivity extends NavigationActivity {

    private static final String EXTRA_FRAGMENT_CLASS = "fragment";
    private static final String EXTRA_FRAGMENT_ARGS = "fragmentargs";

    /**
     * Creates an intent for starting a SingleFragmentActivity or a derived activity.
     *
     * @param context       The context to use for creating the event.
     * @param activityClass The activity class to start.
     * @param fragmentClass The fragment class to start in the activity.
     * @param fragmentArgs  The argument bundle to pass to the fragment.
     * @param start         If true, startActivity will be called on the Intent before returning.
     * @return The created intent.
     */
    public static Intent newIntent(Context context, Class<? extends SingleFragmentActivity> activityClass,
                                   Class<? extends Fragment> fragmentClass, Bundle fragmentArgs, boolean start) {
        Intent intent = new Intent(context, activityClass);
        intent.putExtra(EXTRA_FRAGMENT_CLASS, fragmentClass);
        intent.putExtra(EXTRA_FRAGMENT_ARGS, fragmentArgs);
        if (start) {
            context.startActivity(intent);
        }
        return intent;
    }

    /**
     * Creates an intent for starting a SingleFragmentActivity.
     *
     * @param context       The context to use for creating the event.
     * @param fragmentClass The fragment class to start in the activity.
     * @param fragmentArgs  The argument bundle to pass to the fragment.
     * @param start         If true, startActivity will be called on the Intent before returning.
     * @return The created intent.
     */
    public static Intent newIntent(Context context, Class<? extends Fragment> fragmentClass, Bundle fragmentArgs, boolean start) {
        return newIntent(context, SingleFragmentActivity.class, fragmentClass, fragmentArgs, start);
    }

    @Override
    public void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);

        setContentView(R.layout.activity_single_fragment);

        if (savedInstanceState != null) {
            return;
        }

        // Instantiate the fragment class given in the intent
        Intent intent = getIntent();
        @SuppressWarnings("unchecked")
        Class<Fragment> fragmentClass = (Class<Fragment>) intent.getSerializableExtra(EXTRA_FRAGMENT_CLASS);
        Bundle fragmentArgs = intent.getBundleExtra(EXTRA_FRAGMENT_ARGS);
        Fragment fragment = Fragment.instantiate(this, fragmentClass.getName(), fragmentArgs);
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();
    }

    /**
     * @return The single fragment hosted by this activity.
     */
    protected Fragment getFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }
}