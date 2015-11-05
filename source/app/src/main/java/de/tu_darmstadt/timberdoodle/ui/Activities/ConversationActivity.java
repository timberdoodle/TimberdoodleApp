package de.tu_darmstadt.timberdoodle.ui.Activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.security.PublicKey;

import de.tu_darmstadt.adtn.generickeystore.KeyStoreEntry;
import de.tu_darmstadt.timberdoodle.IService;
import de.tu_darmstadt.timberdoodle.R;
import de.tu_darmstadt.timberdoodle.chatlog.IChatLog;
import de.tu_darmstadt.timberdoodle.friendkeystore.IFriendKeyStore;
import de.tu_darmstadt.timberdoodle.messagehandler.IMessageHandler;
import de.tu_darmstadt.timberdoodle.ui.ConversationAdapter;
import de.tu_darmstadt.timberdoodle.ui.ConversationMessages;
import de.tu_darmstadt.timberdoodle.ui.MessageInputBox;

public class ConversationActivity extends TimberdoodleActivity {

    public static final String INTENT_EXTRA_CONVERSATION_ID = "conversation_id";
    private static final int MENUITEM_DELETE = 1;

    private ListView listView;
    private long id;
    private ConversationAdapter conversationAdapter;

    // Receives message arrival broadcasts
    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshEntries();
            listView.smoothScrollToPosition(0);
        }
    };

    @Override
    protected void onTimberdoodleServiceReady(final IService service) {
        super.onTimberdoodleServiceReady(service);

        // Set up list view adapter and load log entries
        conversationAdapter = new ConversationAdapter(this);
        listView.setAdapter(conversationAdapter);
        IFriendKeyStore friendKeyStore = service.getFriendKeyStore();
        refreshEntries();

        // Register broadcast receiver to get new messages
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(messageReceiver, new IntentFilter(IMessageHandler.ACTION_HANDLE_RECEIVED_PRIVATE_CHAT_MESSAGE));

        // Set up MessageInputBox
        final MessageInputBox messageInputBox = (MessageInputBox) findViewById(R.id.messageInputBox);
        messageInputBox.setCharset(IMessageHandler.CHAT_MESSAGE_CHARSET);
        final IMessageHandler messageHandler = service.getMessageHandler();
        messageInputBox.setMaxBytes(messageHandler.getMaxUnsignedPrivateChatMessageSize());

        // Set up send button click handler
        Button sendButton = (Button) findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String message = messageInputBox.getValidatedMessageAndClear();
                if (message == null) return;

                // If there is no private key, only unsigned messages can be sent
                if (!service.getFriendCipher().isPrivateKeySet()) {
                    sendMessage(service, message, false);
                    return;
                }

                // Ask user if he wants to sign the message
                new AlertDialog.Builder(ConversationActivity.this)
                        .setMessage(R.string.anonymous_writing)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Send unsigned message
                                sendMessage(service, message, false);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Check if message length exceeds max. allowed signed message length
                                if (messageInputBox.getEncodedSize() > messageHandler.getMaxSignedPrivateChatMessageSize()) {
                                    new AlertDialog.Builder(ConversationActivity.this)
                                            .setMessage(getString(R.string.too_long_for_signed_message,
                                                    (int) Math.ceil(messageInputBox.getEncodedSize() / messageInputBox.getAvgBytesPerChar())))
                                            .show();
                                    return;
                                }

                                // Send signed message
                                sendMessage(service, message, true);
                            }
                        })
                        .show();
            }
        });

        // Set activity title to contact name
        KeyStoreEntry<PublicKey> friendEntry = friendKeyStore.getEntry(id);
        String alias = friendEntry == null ? null : friendEntry.getAlias();
        setTitle(alias == null ? getString(R.string.anonymous) : alias);
    }

    private void sendMessage(IService service, String message, boolean sign) {
        service.getMessageHandler().sendPrivateChatMessage(message, id, sign);
        refreshEntries();
        listView.smoothScrollToPosition(0);
    }

    @Override
    public void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);

        setContentView(R.layout.fragment_conversation);
        id = getIntent().getLongExtra(INTENT_EXTRA_CONVERSATION_ID, 0);
        // Set up ListView
        listView = (ListView) findViewById(R.id.conversationMessageListView);
        listView.setEmptyView(findViewById(R.id.empty));
        registerForContextMenu(listView);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(Menu.NONE, MENUITEM_DELETE, Menu.NONE, R.string.menu_delete);
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        if (item.getItemId() == MENUITEM_DELETE) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.message_delete_dialog)
                    .setPositiveButton(R.string.delete_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            long id = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).id;
                            deleteChatLogEntry(id);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
            return true;
        }

        return super.onContextItemSelected(item);
    }

    private void deleteChatLogEntry(long entryID) {
        ConversationMessages conversationMessages = (ConversationMessages) conversationAdapter.getItem((int) entryID);

        getService().getChatLog().deletePrivateMessage(conversationMessages.getID());
        refreshEntries();
    }

    private void refreshEntries() {
        IChatLog chatLog = getService().getChatLog();
        Cursor cursor = chatLog.getPrivateMessages();

        // Update list
        conversationAdapter.getRelevantMessages(cursor, id, getService().getFriendKeyStore().getEntries());

        // Mark as read
        for (boolean moreEntries = cursor.moveToFirst(); moreEntries; moreEntries = cursor.moveToNext()) {
            chatLog.setPrivateMessageRead(cursor.getLong(IChatLog.CURSORINDEX_ID));
        }

        cursor.close();
    }
}
