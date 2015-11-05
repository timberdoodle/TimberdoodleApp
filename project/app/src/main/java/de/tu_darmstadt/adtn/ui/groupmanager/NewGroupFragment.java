package de.tu_darmstadt.adtn.ui.groupmanager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.joda.time.Instant;

import javax.crypto.SecretKey;

import de.tu_darmstadt.adtn.IService;
import de.tu_darmstadt.adtn.generickeystore.KeyStoreEntry;
import de.tu_darmstadt.adtn.groupkeystore.IGroupKeyStore;
import de.tu_darmstadt.adtn.ui.passworddialog.GroupPasswordDialog;
import de.tu_darmstadt.adtn.ui.passworddialog.PasswordDialog;
import de.tu_darmstadt.timberdoodle.R;

public class NewGroupFragment extends GroupManagerFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_new_group, container, false);

        EditText input = (EditText) view.findViewById(R.id.newGroupEditText);
        Helper.setUpAliasEditText(input, IGroupKeyStore.MAX_LENGTH_GROUP_NAME);

        setHelpString(R.string.help_6);
        setViewReady(view);
        return view;
    }

    @Override
    protected void onViewAndAdtnServiceReady(View view, final IService service) {
        super.onViewAndAdtnServiceReady(view, service);

        setHasOptionsMenu(service.getPreferences().getShowHelpButtons());

        // Set up button to create the key and show the QR code in a new fragment
        final EditText input = (EditText) view.findViewById(R.id.newGroupEditText);
        final Button button = (Button) view.findViewById(R.id.newGroupButton);

        new GroupPasswordDialog(getActivity(), new PasswordDialog.OnDoneListener() {
            @Override
            public void onDone() {
                // Cancel if group key store is not loaded
                if (service.getGroupKeyStore() == null) {
                    getFragmentManager().popBackStack();
                    return;
                }

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Check if group alias meets minimum length
                        if (input.length() == 0) {
                            Toast.makeText(getActivity(), R.string.groupLengthWarning, Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Generate key and add it to the group key store
                        SecretKey key = service.getGroupCipher().generateKey();
                        String alias = input.getEditableText().toString();
                        KeyStoreEntry<SecretKey> entry = service.getGroupKeyStore().addEntry(alias, key);
                        if (!Helper.checkAndHandleAddKey(getActivity(), entry,
                                R.string.name_already_exist, R.string.group_key_already_known)) {
                            return;
                        }

                        // Store creation timestamp of the new key
                        Instant timestamp = service.getExpirationManager().addKeyTimestamp(entry.getId());

                        // Show the QR code of the newly created key
                        goToFragment(ShareGroupKeyFragment.newInstance(key, timestamp), false);
                    }
                });
            }
        }, service).show();
    }
}
