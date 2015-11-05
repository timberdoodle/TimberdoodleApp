package de.tu_darmstadt.timberdoodle.ui.Fragments;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import de.tu_darmstadt.adtn.generickeystore.KeyStoreEntry;
import de.tu_darmstadt.timberdoodle.IService;
import de.tu_darmstadt.timberdoodle.R;
import de.tu_darmstadt.timberdoodle.messagehandler.IMessageHandler;
import de.tu_darmstadt.timberdoodle.ui.Activities.ConversationActivity;
import de.tu_darmstadt.timberdoodle.ui.ConversationOverviewAdapter;
import de.tu_darmstadt.timberdoodle.ui.FriendListEntry;
import de.tu_darmstadt.timberdoodle.ui.PrivateConversationListEntry;


public class PrivateMessagesFragment extends DoodleFragment {
    private ListView listView;
    private ConversationOverviewAdapter conversationOverviewAdapter;
    private ArrayList<FriendListEntry> friendNames = new ArrayList<>();
    private ArrayAdapter<FriendListEntry> friendListEntryArrayAdapter;
    // Receives message arrival broadcasts
    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            conversationOverviewAdapter.convertChatlogToEntries(getService().getChatLog().getPrivateMessages(), getService().getFriendKeyStore().getEntries());
            friendNameListUpdater();
        }
    };
    private LocalBroadcastManager broadcastManager;
    private boolean wasPaused = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_private_messages, container, false);
        setHelpString(R.string.help_2);

        // Initialize arrayadapter for friendlist
        friendListEntryArrayAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                friendNames);

        // Adds functionality to the floating action button
        view.findViewById(R.id.conversationButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (friendNames.size() > 0) {
                    ListView friendListView = new ListView(getActivity());
                    friendListView.setAdapter(friendListEntryArrayAdapter);

                    final AlertDialog listDialog = new AlertDialog.Builder(getActivity())
                            .setMessage(R.string.choose_friend_receiver)
                            .setView(friendListView)
                            .show();


                    // After clicking on one Item it will lead to another fragment
                    friendListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            listDialog.cancel();
                            Intent intent = new Intent(getActivity(), ConversationActivity.class);
                            intent.putExtra(ConversationActivity.INTENT_EXTRA_CONVERSATION_ID, ((FriendListEntry) parent.getItemAtPosition(position)).getId());
                            getActivity().startActivity(intent);
                        }
                    });
                } else {
                    new AlertDialog.Builder(getActivity())
                            .setMessage(R.string.empty_contact_list)
                            .setPositiveButton(android.R.string.ok, null)
                            .show();

                }
            }
        });


        // Set up ListView
        listView = (ListView) view.findViewById(R.id.conversationList);
        listView.setEmptyView(view.findViewById(R.id.empty));

        setViewReady(view);

        return view;
    }

    @Override
    protected void onViewAndServiceReady(View view, IService service) {
        super.onViewAndServiceReady(view, service);


        setHasOptionsMenu(service.getAdtnService().getPreferences().getShowHelpButtons());

        // Set up list view adapter and load log entries
        conversationOverviewAdapter = new ConversationOverviewAdapter(this.getActivity());
        listView.setAdapter(conversationOverviewAdapter);
        conversationOverviewAdapter.convertChatlogToEntries(service.getChatLog().getPrivateMessages(), service.getFriendKeyStore().getEntries());

        // OnClickListener to open the conversations
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PrivateConversationListEntry privateConversationListEntry = (PrivateConversationListEntry) parent.getItemAtPosition(position);
                Intent intent = new Intent(getActivity(), ConversationActivity.class);
                intent.putExtra(ConversationActivity.INTENT_EXTRA_CONVERSATION_ID, privateConversationListEntry.getId());
                getActivity().startActivity(intent);
            }
        });


        // BroadcastManager to get new Messages
        broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        broadcastManager.registerReceiver(messageReceiver, new IntentFilter(IMessageHandler.ACTION_HANDLE_RECEIVED_PRIVATE_CHAT_MESSAGE));

        friendNameListUpdater();
    }

    /**
     * Updates ArrayList with friend names for the floating action button
     */
    private void friendNameListUpdater() {
        friendNames.clear();
        for (KeyStoreEntry<PublicKey> pke : getService().getFriendKeyStore().getEntries())
            friendNames.add(new FriendListEntry(pke));

        Collections.sort(friendNames, new Comparator<FriendListEntry>() {
            @Override
            public int compare(FriendListEntry lhs, FriendListEntry rhs) {
                return lhs.toString().compareTo(rhs.toString());
            }
        });

        friendListEntryArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        broadcastManager.unregisterReceiver(messageReceiver);
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        wasPaused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (wasPaused) {
            friendNameListUpdater();
            conversationOverviewAdapter.convertChatlogToEntries(getService().getChatLog().getPrivateMessages(), getService().getFriendKeyStore().getEntries());
            wasPaused = false;
        }
    }
}
