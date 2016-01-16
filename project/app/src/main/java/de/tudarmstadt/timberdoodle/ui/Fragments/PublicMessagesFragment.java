package de.tudarmstadt.timberdoodle.ui.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import de.tudarmstadt.timberdoodle.IService;
import de.tudarmstadt.timberdoodle.R;
import de.tudarmstadt.timberdoodle.chatlog.ChatLogEntry;
import de.tudarmstadt.timberdoodle.chatlog.IChatLog;
import de.tudarmstadt.timberdoodle.messagehandler.IMessageHandler;
import de.tudarmstadt.timberdoodle.ui.Activities.SingleFragmentMultiInstancesActivity;
import de.tudarmstadt.timberdoodle.ui.MessageInputBox;
import de.tudarmstadt.timberdoodle.ui.PublicMessagesAdapter;

public class PublicMessagesFragment extends DoodleFragment {

    private final int REQUEST_CODE_REVIEW = 0;
    private final int REQUEST_CODE_DISPLAY = 1;

    private final int MENUITEM_DELETE = 1;

    private ListView listView;
    private PublicMessagesAdapter messageListAdapter;
    // Receives message arrival broadcasts
    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(IMessageHandler.INTENT_ARG_ID, IChatLog.INVALID_MESSAGE_ID);
            listView.smoothScrollToPosition(messageListAdapter.addItem(id));
        }
    };
    private IChatLog chatLog;
    private LocalBroadcastManager broadcastManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_public_messages, container, false);

        // Set up ListView
        listView = (ListView) view.findViewById(R.id.publicMessagesView);
        listView.setEmptyView(view.findViewById(R.id.empty));
        registerForContextMenu(listView);

        setHelpString(R.string.help_1);
        setViewReady(view);
        return view;
    }

    @Override
    protected void onViewAndServiceReady(View view, IService service) {
        super.onViewAndServiceReady(view, service);

        this.chatLog = service.getChatLog();

        final FragmentActivity activity = getActivity();

        setHasOptionsMenu(service.getAdtnService().getPreferences().getShowHelpButtons());

        // Set up list view adapter and load log entries
        messageListAdapter = new PublicMessagesAdapter(activity, chatLog);
        listView.setAdapter(messageListAdapter);
        messageListAdapter.loadFromLog();

        // On click to a list item, open a fragment that shows the whole message
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = SingleFragmentMultiInstancesActivity.newIntent(getActivity(),
                        DisplayPublicMessageFragment.class,
                        DisplayPublicMessageFragment.createArgs(id), false);
                startActivityForResult(intent, REQUEST_CODE_DISPLAY);
            }
        });

        // Enable receiving of message arrival broadcasts
        broadcastManager = LocalBroadcastManager.getInstance(activity);
        broadcastManager.registerReceiver(messageReceiver, new IntentFilter(IMessageHandler.ACTION_HANDLE_RECEIVED_PUBLIC_CHAT_MESSAGE));

        // Set up MessageInputBox
        final MessageInputBox messageInputBox = (MessageInputBox) view.findViewById(R.id.messageInputBox);
        messageInputBox.setCharset(IMessageHandler.CHAT_MESSAGE_CHARSET);
        final int maxBytes = service.getMessageHandler().getMaxPublicChatMessageSize();
        messageInputBox.setMaxBytes(maxBytes);

        // Set up send button click handler: Ask the user to review the message
        Button sendButton = (Button) view.findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String message = messageInputBox.getValidatedMessageAndClear();
                if (message == null) return;
                new AlertDialog.Builder(activity)
                        .setMessage(R.string.popupMessage_text)
                        .setPositiveButton(R.string.popupMessage_view, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = SingleFragmentMultiInstancesActivity.newIntent(getActivity(),
                                        MessageReviewFragment.class,
                                        MessageReviewFragment.createArgs(message, maxBytes), false);
                                startActivityForResult(intent, REQUEST_CODE_REVIEW);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null).show();
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.publicMessagesView)
            menu.add(Menu.NONE, MENUITEM_DELETE, Menu.NONE, R.string.menu_delete);
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        if (item.getItemId() == MENUITEM_DELETE) {
            AlertDialog deleteDialog = new AlertDialog.Builder(getActivity())
                    .setMessage("Do you really want to delete this message? It cannot be undone.")
                    .setPositiveButton(R.string.delete_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            long id = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).id;
                            deleteChatLogEntry(id);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
            deleteDialog.show();
            return true;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_REVIEW) { // Returning from MessageReviewFragment?
            if (resultCode != Activity.RESULT_OK) return;

            // Send message
            String message = data.getStringExtra(MessageReviewFragment.OUTARGNAME_MESSAGE);
            long id = getService().getMessageHandler().sendPublicChatMessage(message);
            // Add message to list on scroll to it
            int position = messageListAdapter.addItem(id);
            listView.smoothScrollToPosition(position);
            // Show toast informing that message was sent
            if (message.length() > 15) {
                Toast.makeText(getActivity(),
                        getString(R.string.truncated_message_send_confirmation, message.substring(0, Math.min(message.length(), 10))),
                        Toast.LENGTH_LONG).show();
            } else
                Toast.makeText(getActivity(), getString(R.string.message_send_confirmation, message),
                        Toast.LENGTH_LONG).show();
        } else if (requestCode == REQUEST_CODE_DISPLAY) { // Returning from DisplayPublicMessageFragment?
            if (resultCode != Activity.RESULT_OK) return;

            // Delete message if requested
            long deleteMessageId = data.getLongExtra(DisplayPublicMessageFragment.OUTARGNAME_DELETE, 0);
            if (deleteMessageId != 0) deleteChatLogEntry(deleteMessageId);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        broadcastManager.unregisterReceiver(messageReceiver);

        super.onDestroy();
    }

    private void deleteChatLogEntry(long id) {
        ChatLogEntry message = chatLog.getPublicMessage(id);

        String textMsg = message.getContent();
        chatLog.deletePublicMessage(id);

        if (textMsg.length() > 15) {
            Toast.makeText(getActivity(), getString(R.string.truncated_message_delete_confirmation, textMsg.substring(0, Math.min(textMsg.length(), 15))),
                    Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(getActivity(), getString(R.string.message_delete_confirmation, textMsg),
                    Toast.LENGTH_SHORT).show();
        messageListAdapter.removeById(id);
    }
}
