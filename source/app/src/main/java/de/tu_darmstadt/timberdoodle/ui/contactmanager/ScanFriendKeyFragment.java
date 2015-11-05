package de.tu_darmstadt.timberdoodle.ui.contactmanager;

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

import java.security.PublicKey;

import de.tu_darmstadt.timberdoodle.IService;
import de.tu_darmstadt.timberdoodle.R;
import de.tu_darmstadt.timberdoodle.friendcipher.IFriendCipher;

public class ScanFriendKeyFragment extends ContactManagerFragment {

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
    protected void onViewAndServiceReady(View view, IService service) {
        super.onViewAndServiceReady(view, service);

        setHasOptionsMenu(service.getAdtnService().getPreferences().getShowHelpButtons());

        // Set up button handler for launching the ZXing QR code scanner
        ImageButton button = (ImageButton) view.findViewById(R.id.qrReadButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FriendQRReaderWriter().initiateScan(ScanFriendKeyFragment.this, getString(R.string.scan_groupKey));
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
        IService service = getService();

        // Service and QR data have to be available
        if (service == null || qrData == null) return;

        // Parse key
        IFriendCipher cipher = getService().getFriendCipher();
        PublicKey scanned = new FriendQRReaderWriter().parseCode(qrData, cipher);
        if (scanned == null) { // Cannot parse QR code?
            Toast.makeText(getActivity(), R.string.wrongFormat_toast, Toast.LENGTH_LONG).show();
            return;
        }

        byte[] key = cipher.publicKeyToByteArray(scanned);
        goToFragment(AcceptFriendKeyFragment.newInstance(key), false);
    }
}
