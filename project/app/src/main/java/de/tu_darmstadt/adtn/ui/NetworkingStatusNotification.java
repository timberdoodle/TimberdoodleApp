package de.tu_darmstadt.adtn.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;

import de.tu_darmstadt.adtn.NetworkingStatus;
import de.tu_darmstadt.timberdoodle.R;

/**
 * Show the networking status in the notification bar.
 */
public class NetworkingStatusNotification {

    private final static int NOTIFICATION_ID = 2;

    private final Context context;
    private final NotificationManager notificationManager;

    /**
     * Creates a new NetworkingStatusNotification object.
     *
     * @param context The context to use.
     */
    public NetworkingStatusNotification(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * Updates the notification with the specified networking status.
     *
     * @param status The networking status to show.
     */
    public void setStatus(final NetworkingStatus status) {
        new Handler(context.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                // Set up small icon, large icon, text and priority
                int smallIcon, largeIcon;
                int text;
                int priority = NotificationCompat.PRIORITY_DEFAULT;
                if (status.getStatus() == NetworkingStatus.STATUS_ENABLED) {
                    smallIcon = R.drawable.ic_sync_white_18dp;
                    largeIcon = R.drawable.ic_sync_black_48dp;
                    text = R.string.networking_enabled;
                } else {
                    smallIcon = R.drawable.ic_sync_disabled_white_18dp;
                    largeIcon = R.drawable.ic_sync_disabled_black_48dp;
                    if (status.getStatus() == NetworkingStatus.STATUS_ERROR) {
                        text = R.string.networking_error;
                        priority = NotificationCompat.PRIORITY_MAX;
                    } else {
                        text = R.string.networking_disabled;
                    }
                }

                // Create and show notification
                Notification notification = new NotificationCompat.Builder(context)
                        .setSmallIcon(smallIcon)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), largeIcon))
                        .setContentTitle(context.getString(R.string.networking_status))
                        .setContentText(context.getString(text))
                        .setPriority(priority)
                        .setAutoCancel(false)
                        .setOngoing(true)
                        .setContentIntent(PendingIntent.getActivity(context, 0,
                                new Intent(context, NetworkingStatusActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT))
                        .build();
                notificationManager.notify(NOTIFICATION_ID, notification);
            }
        });
    }

    public void close() {
        notificationManager.cancel(NOTIFICATION_ID);
    }
}
