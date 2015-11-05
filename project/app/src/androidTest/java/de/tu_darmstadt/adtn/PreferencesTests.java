package de.tu_darmstadt.adtn;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.MediumTest;

import de.tu_darmstadt.adtn.errorlogger.ErrorLoggingSingleton;
import de.tu_darmstadt.adtn.preferences.IPreferences;
import de.tu_darmstadt.adtn.preferences.Preferences;

public class PreferencesTests extends AndroidTestCase {

    private Context context;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        context = new RenamingDelegatingContext(getContext(), "test.");
        ErrorLoggingSingleton log = ErrorLoggingSingleton.getInstance();
        log.setContext(context);
    }

    private IPreferences loadPreferences() {
        return new Preferences(context);
    }

    private void compareAllPreferences(IPreferences uut, int sendInterval, int refillThreshold, int batchSize) {
        assertEquals(sendInterval, uut.getSendingPoolSendInterval());
        assertEquals(refillThreshold, uut.getSendingPoolRefillThreshold());
        assertEquals(batchSize, uut.getSendingPoolBatchSize());
    }

    private void setAllPreferences(IPreferences uut, int sendInterval, int refillThreshold, int batchSize) {
        uut.edit();
        uut.setSendingPoolSendInterval(sendInterval);
        uut.setSendingPoolRefillThreshold(refillThreshold);
        uut.setSendingPoolBatchSize(batchSize);
        uut.commit();
    }

    @MediumTest
    public void test() {
        final int
                sendInterval1 = 4351, sendInterval2 = 8448,
                refillThreshold1 = 234, refillThreshold2 = 920,
                batchSize1 = 8722, batchSize2 = 208;

        // Load preferences, set all preferences, verify, reload and verify again
        IPreferences uut = loadPreferences();
        setAllPreferences(uut, sendInterval1, refillThreshold1, batchSize1);
        compareAllPreferences(uut, sendInterval1, refillThreshold1, batchSize1);
        uut = loadPreferences();
        compareAllPreferences(uut, sendInterval1, refillThreshold1, batchSize1);

        // Do the same with different values
        setAllPreferences(uut, sendInterval2, refillThreshold2, batchSize2);
        compareAllPreferences(uut, sendInterval2, refillThreshold2, batchSize2);
        uut = loadPreferences();
        compareAllPreferences(uut, sendInterval2, refillThreshold2, batchSize2);
    }
}
