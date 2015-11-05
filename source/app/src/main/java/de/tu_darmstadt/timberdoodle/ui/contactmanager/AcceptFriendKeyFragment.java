package de.tu_darmstadt.timberdoodle.ui.contactmanager;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.security.PublicKey;

import de.tu_darmstadt.adtn.generickeystore.KeyStoreEntry;
import de.tu_darmstadt.adtn.ui.groupmanager.ChecksumGenerator;
import de.tu_darmstadt.adtn.ui.groupmanager.Helper;
import de.tu_darmstadt.timberdoodle.IService;
import de.tu_darmstadt.timberdoodle.R;
import de.tu_darmstadt.timberdoodle.friendcipher.IFriendCipher;

public class AcceptFriendKeyFragment extends ContactManagerFragment {

    private final static String ARGNAME_KEY = "key";

    /**
     * Creates a new AcceptFriendKeyFragment.
     *
     * @param key The key the user is asked to accept.
     * @return A new AcceptFriendKeyFragment.
     */
    public static AcceptFriendKeyFragment newInstance(byte[] key) {
        Bundle args = new Bundle(1);
        args.putByteArray(ARGNAME_KEY, key);
        AcceptFriendKeyFragment fragment = new AcceptFriendKeyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accept_friend_key, container, false);
        setViewReady(view);
        return view;
    }

    @Override
    protected void onViewAndServiceReady(View view, final IService service) {
        super.onViewAndServiceReady(view, service);

        setHasOptionsMenu(service.getAdtnService().getPreferences().getShowHelpButtons());

        // Get arguments from bundle
        Bundle arguments = getArguments();
        final byte[] key = arguments.getByteArray(ARGNAME_KEY);

        // Show key checksum and ask user if he wants to add this friend
        TextView textView = (TextView) view.findViewById(R.id.checkSumTexView);
        textView.setText(getString(R.string.add_this_friend) + "\n\n" +
                getString(R.string.checksum, new ChecksumGenerator().generate(key)));

        // Create and show QR code
        IFriendCipher cipher = service.getFriendCipher();
        ImageView imageView = (ImageView) view.findViewById(R.id.acceptQRImgView);
        final PublicKey publicKey = cipher.byteArrayToPublicKey(key);
        Bitmap qrCode = new FriendQRReaderWriter().createQrCode(cipher, publicKey, 500, 500);
        imageView.setImageBitmap(qrCode);

        // Set up accept button
        view.findViewById(R.id.acceptInviteButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.showAliasInputDialog(getActivity(), new ContactAliasDialogData(),
                        new Helper.OnConfirmAliasListener() {
                            @Override
                            public boolean onConfirmAlias(String newAlias) {
                                // Try to put key in store
                                KeyStoreEntry<PublicKey> entry = service.getFriendKeyStore().addEntry(newAlias, publicKey);
                                if (!Helper.checkAndHandleAddKey(getActivity(), entry,
                                        R.string.name_already_exist, R.string.friend_key_already_known)) {
                                    return false; // Keep dialog open on error
                                }

                                // Go to friend key list after accepting the key
                                goToFragment(FriendKeyManagementFragment.newInstance(entry.getId()), false);
                                return true; // Close dialog
                            }
                        });
            }
        });
    }
}
