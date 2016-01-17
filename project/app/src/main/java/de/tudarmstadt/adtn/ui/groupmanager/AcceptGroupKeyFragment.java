package de.tudarmstadt.adtn.ui.groupmanager;

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

import de.tudarmstadt.adtn.IService;
import de.tudarmstadt.adtn.ciphersuite.IGroupCipher;
import de.tudarmstadt.adtn.generickeystore.KeyStoreEntry;
import de.tudarmstadt.adtn.ui.passworddialog.GroupPasswordDialog;
import de.tudarmstadt.adtn.ui.passworddialog.PasswordDialog;
import de.tudarmstadt.timberdoodle.R;

/**
 * A fragment where to user is asked to confirm to add a scanned group key.
 */
public class AcceptGroupKeyFragment extends GroupManagerFragment {

    private static final String ARGNAME_KEY = "key";
    private static final String ARGNAME_TIMESTAMP = "timestamp";

    /**
     * Creates a new AcceptGroupKeyFragment.
     *
     * @param key       The key the user is asked to accept.
     * @param timestamp The timestamp of the key.
     * @return A new AcceptGroupKeyFragment.
     */
    public static AcceptGroupKeyFragment newInstance(byte[] key, Instant timestamp) {
        Bundle args = new Bundle(2);
        args.putByteArray(ARGNAME_KEY, key);
        args.putSerializable(ARGNAME_TIMESTAMP, timestamp);
        AcceptGroupKeyFragment fragment = new AcceptGroupKeyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accept_group_key, container, false);
        setViewReady(view);
        return view;
    }

    @Override
    protected void onViewAndAdtnServiceReady(final View view, final IService service) {
        super.onViewAndAdtnServiceReady(view, service);

        setHasOptionsMenu(service.getPreferences().getShowHelpButtons());
        // Get arguments from bundle
        Bundle arguments = getArguments();
        final byte[] key = arguments.getByteArray(ARGNAME_KEY);
        final Instant timestamp = (Instant) arguments.getSerializable(ARGNAME_TIMESTAMP);

        // Show key checksum and ask user if he wants to join the group
        TextView textView = (TextView) view.findViewById(R.id.checkSumTexView);
        textView.setText(getString(R.string.join_this_group) + "\n\n" +
                getString(R.string.checksum, new ChecksumGenerator().generate(key)));

        // Create and show QR code
        ImageView imageView = (ImageView) view.findViewById(R.id.acceptQRImgView);
        IGroupCipher cipher = service.getGroupCipher();
        SecretKey secretKey = cipher.byteArrayToSecretKey(key);
        Bitmap qrCode = new GroupQRReaderWriter().createQrCode(cipher, secretKey, timestamp, 200, 200);
        imageView.setImageBitmap(qrCode);

        new GroupPasswordDialog(getActivity(), new PasswordDialog.OnDoneListener() {
            @Override
            public void onDone() {
                // Cancel if group key store is not loaded
                if (service.getGroupKeyStore() == null) {
                    getFragmentManager().popBackStack();
                    return;
                }

                // Set up accept button
                view.findViewById(R.id.acceptInviteButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Helper.showAliasInputDialog(getActivity(), new GroupAliasDialogData(),
                                new Helper.OnConfirmAliasListener() {
                                    @Override
                                    public boolean onConfirmAlias(String newAlias) {
                                        // Try to put key in store
                                        SecretKey secretKey = service.getGroupCipher().byteArrayToSecretKey(key);
                                        KeyStoreEntry<SecretKey> entry = service.getGroupKeyStore().addEntry(newAlias, secretKey);
                                        if (!Helper.checkAndHandleAddKey(getActivity(), entry,
                                                R.string.name_already_exist, R.string.group_key_already_known)) {
                                            return false; // Keep dialog open on error
                                        }

                                        // Go to group key list after accepting the key
                                        goToFragment(GroupKeyManagementFragment.newInstance(entry.getId()), false);
                                        return true; // Close dialog
                                    }
                                });
                    }
                });
            }
        }, service).show();
    }
}
