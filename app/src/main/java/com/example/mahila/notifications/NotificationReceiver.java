package com.example.mahila.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.example.mahila.R;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String CHANNEL_PERIOD = "mahila_period_channel";
    public static final String CHANNEL_MEDS = "mahila_meds_channel";

    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_MESSAGE = "extra_message";
    public static final String EXTRA_CHANNEL_ID = "extra_channel_id";
    public static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        // Handle Boot Completed: Reschedule alarms silently without firing notification banner
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            ReminderScheduler.rescheduleAllOnBoot(context);
            return;
        }

        String title = intent.getStringExtra(EXTRA_TITLE);
        String message = intent.getStringExtra(EXTRA_MESSAGE);
        String channelId = intent.getStringExtra(EXTRA_CHANNEL_ID);
        int notifId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 1001);

        if (title == null) title = "Mahila Health Reminder";
        if (message == null) message = "You have an upcoming health reminder.";
        if (channelId == null) channelId = CHANNEL_PERIOD;

        createNotificationChannels(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_nav_notifications)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(notifId, builder.build());
        }
    }

    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                NotificationChannel periodChannel = new NotificationChannel(
                        CHANNEL_PERIOD,
                        "Cycle & Period Reminders",
                        NotificationManager.IMPORTANCE_HIGH
                );
                periodChannel.setDescription("Notifications for estimated period starts and fertile windows.");

                NotificationChannel medsChannel = new NotificationChannel(
                        CHANNEL_MEDS,
                        "Medication & Health Reminders",
                        NotificationManager.IMPORTANCE_HIGH
                );
                medsChannel.setDescription("Scheduled alerts for medications, symptoms, and appointments.");

                manager.createNotificationChannel(periodChannel);
                manager.createNotificationChannel(medsChannel);
            }
        }
    }
}
