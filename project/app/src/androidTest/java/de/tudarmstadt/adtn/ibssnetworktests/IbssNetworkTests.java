package de.tudarmstadt.adtn.ibssnetworktests;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.LargeTest;

import de.tudarmstadt.adtn.preferences.AdtnPreferences;
import de.tudarmstadt.adtn.wifi.IbssNetwork;
import de.tudarmstadt.adtn.errorlogger.ErrorLoggingSingleton;

public class IbssNetworkTests extends AndroidTestCase {

    private AdtnPreferences netPrefs;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
        log.setContext(new RenamingDelegatingContext(getContext(), "test."));
        netPrefs = new AdtnPreferences(new RenamingDelegatingContext(getContext(), "test."));
        netPrefs.reset();
    }

    @LargeTest
    public void testStartStop() throws InterruptedException {
        IbssNetwork uut = new IbssNetwork(getContext());
        uut.start();
        Thread.sleep(5000);
        setAutoIbssPref(false);
        Thread.sleep(5000);
        setAutoIbssPref(true);
        Thread.sleep(5000);
        uut.stop();
    }

    private void setAutoIbssPref(boolean enabled) {
        netPrefs.edit();
        netPrefs.setAutoJoinAdHocNetwork(enabled);
        netPrefs.commit();
    }
}
