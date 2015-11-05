package de.tu_darmstadt.adtn.ibssnetworktests;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.LargeTest;

import de.tu_darmstadt.adtn.wifi.IbssNetwork;
import de.tu_darmstadt.adtn.errorlogger.ErrorLoggingSingleton;
import de.tu_darmstadt.adtn.preferences.Preferences;

public class IbssNetworkTests extends AndroidTestCase {

    private Preferences netPrefs;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
        log.setContext(new RenamingDelegatingContext(getContext(), "test."));
        netPrefs = new Preferences(new RenamingDelegatingContext(getContext(), "test."));
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
