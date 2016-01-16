package de.tudarmstadt.timberdoodle.ui.contactmanager;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.security.KeyPair;
import java.security.PublicKey;

import de.tudarmstadt.adtn.ui.passworddialog.PasswordDialog;
import de.tudarmstadt.timberdoodle.IService;
import de.tudarmstadt.timberdoodle.R;
import de.tudarmstadt.timberdoodle.friendcipher.IFriendCipher;
import de.tudarmstadt.timberdoodle.ui.PrivateKeyStorePasswordDialog;

public class ShareFriendKeyFragment extends ContactManagerFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_share_friend_key, container, false);
        setHelpString(R.string.help_10);
        setViewReady(view);
        return view;
    }

    @Override
    protected void onViewAndServiceReady(final View view, final IService service) {
        super.onViewAndServiceReady(view, service);

        setHasOptionsMenu(service.getAdtnService().getPreferences().getShowHelpButtons());

        new PrivateKeyStorePasswordDialog(getActivity(), new PasswordDialog.OnDoneListener() {
            @Override
            public void onDone() {
                // Create key pair if not done already
                KeyPair keyPair = service.getPrivateKeyStore().getKeyPair();
                if (keyPair == null) {
                    keyPair = service.getFriendCipher().generateKeyPair();
                    service.getPrivateKeyStore().setKeyPair(keyPair);
                    service.getFriendCipher().setPrivateKey(keyPair.getPrivate());
                }

                // Create and show QR code
                PublicKey ownPublicKey = getService().getPrivateKeyStore().getKeyPair().getPublic();
                IFriendCipher cipher = service.getFriendCipher();
                Bitmap QRCode = new FriendQRReaderWriter().createQrCode(cipher, ownPublicKey, 500, 500);
                ImageView imageView = (ImageView) view.findViewById(R.id.qrCodeView);
                imageView.setImageBitmap(QRCode);
            }
        }, service, false).show();
    }
}
