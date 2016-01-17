package de.tudarmstadt.adtn.ui.groupmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import de.tudarmstadt.adtn.IService;
import de.tudarmstadt.adtn.ciphersuite.IGroupCipher;
import de.tudarmstadt.timberdoodle.R;

public class ScanGroupKeyFragment extends GroupManagerFragment {

    private String qrData;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan_key, container, false);
        setHelpString(R.string.help_5);
        setViewReady(view);
        return view;
    }

    @Override
    protected void onViewAndAdtnServiceReady(View view, IService service) {
        super.onViewAndAdtnServiceReady(view, service);

        setHasOptionsMenu(service.getPreferences().getShowHelpButtons());

        // Set up button handler for launching the ZXing QR code scanner
        ImageButton button = (ImageButton) view.findViewById(R.id.qrReadButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GroupQRReaderWriter().initiateScan(ScanGroupKeyFragment.this, getString(R.string.scan_groupKey));
            }
        });

        tryHandleQRData();
    }

    /**
     * Gets called when the QR code was read is provided by zxing
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // Cancel if activity result is not a QR code scan result
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (result == null) {
            super.onActivityResult(requestCode, resultCode, intent);
            return;
        }

        // User cancelled QR code scanning?
        if (result.getContents() == null) {
            Toast.makeText(getActivity(), R.string.canceled_toast, Toast.LENGTH_LONG).show();
            return;
        }

        qrData = result.getContents();
        tryHandleQRData();
    }

    private void tryHandleQRData() {
        IService service = getAdtnService();

        // Service and QR data have to be available
        if (service == null || qrData == null) {
            return;
        }

        // Parse key
        IGroupCipher cipher = getAdtnService().getGroupCipher();
        GroupQRReaderWriter.ScannedGroupKey scanned =
                new GroupQRReaderWriter().parseCode(qrData, cipher);
        if (scanned == null) { // Cannot parse QR code?
            Toast.makeText(getActivity(), R.string.wrongFormat_toast, Toast.LENGTH_LONG).show();
            return;
        }

        // Show key accept fragment only if the key has not expired yet
        if (getAdtnService().getExpirationManager().isExpired(scanned.getTimestamp())) {
            goToFragment(GroupKeyExpiredFragment.newInstance(), false);
        } else {
            byte[] key = cipher.secretKeyToByteArray(scanned.getKey());
            goToFragment(AcceptGroupKeyFragment.newInstance(key, scanned.getTimestamp()), false);
        }
    }
}
