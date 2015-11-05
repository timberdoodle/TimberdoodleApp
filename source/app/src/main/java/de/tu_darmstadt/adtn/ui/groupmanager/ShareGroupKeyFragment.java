package de.tu_darmstadt.adtn.ui.groupmanager;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.joda.time.Instant;

import javax.crypto.SecretKey;

import de.tu_darmstadt.adtn.IService;
import de.tu_darmstadt.adtn.ciphersuite.IGroupCipher;
import de.tu_darmstadt.timberdoodle.R;

public class ShareGroupKeyFragment extends GroupManagerFragment {

    private final static String ARGNAME_KEY = "key";
    private final static String ARGNAME_TIMESTAMP = "timestamp";

    public static ShareGroupKeyFragment newInstance(SecretKey key, Instant timestamp) {
        Bundle args = new Bundle(2);
        args.putSerializable(ARGNAME_KEY, key);
        args.putSerializable(ARGNAME_TIMESTAMP, timestamp);
        ShareGroupKeyFragment fragment = new ShareGroupKeyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_share_group_key, container, false);
        setViewReady(view);
        return view;
    }

    @Override
    protected void onViewAndAdtnServiceReady(View view, IService service) {
        super.onViewAndAdtnServiceReady(view, service);


        setHasOptionsMenu(service.getPreferences().getShowHelpButtons());

        // Read arguments from bundle
        SecretKey secretKey = (SecretKey) getArguments().getSerializable(ARGNAME_KEY);
        assert secretKey != null;
        Instant dateTime = (Instant) getArguments().getSerializable(ARGNAME_TIMESTAMP);
        assert dateTime != null;

        // Show the checksum of the key
        IGroupCipher cipher = service.getGroupCipher();
        byte[] keyBytes = cipher.secretKeyToByteArray(secretKey);
        long checksum = new ChecksumGenerator().generate(keyBytes);
        TextView textView = (TextView) view.findViewById(R.id.shareTextView);
        textView.setText(getString(R.string.checksum, checksum));

        // Create and show the QR code
        Bitmap QRCode = new GroupQRReaderWriter().createQrCode(cipher, secretKey, dateTime, 200, 200);
        ImageView imageView = (ImageView) view.findViewById(R.id.qrCodeView);
        imageView.setImageBitmap(QRCode);
    }
}
