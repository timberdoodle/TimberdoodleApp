package de.tudarmstadt.timberdoodle.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import de.tudarmstadt.timberdoodle.R;
import de.tudarmstadt.timberdoodle.chatlog.IChatLog;
import de.tudarmstadt.timberdoodle.messagehandler.IMessageHandler;
import de.tudarmstadt.timberdoodle.ui.Activities.MessagingActivity;

public class MessageArrivalNotification {

    private final static int NOTIFICATION_ID = 1;

    private final NotificationManager notificationManager;
    private final LocalBroadcastManager broadcastManager;
    private final IChatLog chatLog;

    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(IMessageHandler.INTENT_ARG_ID, IChatLog.INVALID_MESSAGE_ID);
            if (id == IChatLog.INVALID_MESSAGE_ID) return;

            // Create text for the notification
            int numUnreadPublic = chatLog.getNumUnreadPublicMessages();
            int numUnreadPrivate = chatLog.getNumUnreadPrivateMessages();
            int numUnreadTotal = numUnreadPublic + numUnreadPrivate;
            if (numUnreadTotal == 0) return;

            // Create text to show
            String text;
            if (numUnreadPublic > 0 && numUnreadPrivate > 0) {
                text = context.getString(R.string.unread_public_and_private_messages,
                        numUnreadPublic, numUnreadPrivate);
            } else if (numUnreadPublic > 0) {
                text = context.getString(R.string.unread_public_messages, numUnreadPublic);
            } else { // if (numUnreadPrivate > 1)
                text = context.getString(R.string.unread_private_messages, numUnreadPrivate);
            }

            // Create notification and show it
            String title = context.getString(R.string.unread_messages);
            Notification notification = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_message_white_18dp)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_face_black_48dp))
                    .setTicker(title)
                    .setWhen(System.currentTimeMillis())
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setNumber(numUnreadTotal)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(PendingIntent.getActivity(context, 0,
                            new Intent(context, MessagingActivity.class),
                            PendingIntent.FLAG_UPDATE_CURRENT))
                    .build();
            notification.flags |= NotificationCompat.FLAG_AUTO_CANCEL;
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    };

    /**
     * Creates a new MessageArrivalNotification object.
     *
     * @param context The context to use.
     * @param chatLog The chat log object.
     */
    public MessageArrivalNotification(Context context, IChatLog chatLog) {
        this.chatLog = chatLog;

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Register broadcast receiver for handling incoming messages
        broadcastManager = LocalBroadcastManager.getInstance(context);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(IMessageHandler.ACTION_HANDLE_RECEIVED_PUBLIC_CHAT_MESSAGE);
        intentFilter.addAction(IMessageHandler.ACTION_HANDLE_RECEIVED_PRIVATE_CHAT_MESSAGE);
        broadcastManager.registerReceiver(messageReceiver, intentFilter);
    }

    /**
     * Clears all notifications and stops listening for new messages.
     */
    public void close() {
        broadcastManager.unregisterReceiver(messageReceiver);
        notificationManager.cancel(NOTIFICATION_ID);
    }
}
