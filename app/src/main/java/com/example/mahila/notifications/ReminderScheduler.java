package com.example.mahila.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;

import com.example.mahila.DatabaseHelper;
import com.example.mahila.api.TokenManager;
import com.example.mahila.utils.CycleCalculator;

import java.util.ArrayList;
import java.util.List;

public class ReminderScheduler {

    public static boolean scheduleNotification(Context context, int notificationId, String title, String message, String channelId, long timeMillis) {
        if (timeMillis <= System.currentTimeMillis()) {
            return false;
        }

        NotificationReceiver.createNotificationChannels(context);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return false;

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(NotificationReceiver.EXTRA_TITLE, title);
        intent.putExtra(NotificationReceiver.EXTRA_MESSAGE, message);
        intent.putExtra(NotificationReceiver.EXTRA_CHANNEL_ID, channelId);
        intent.putExtra(NotificationReceiver.EXTRA_NOTIFICATION_ID, notificationId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent);
            }
            return true;
        } catch (SecurityException e) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void cancelNotification(Context context, int notificationId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    public static boolean schedulePeriodReminder(Context context, String userEmail, int daysBefore) {
        if (userEmail == null || userEmail.isEmpty()) return false;

        DatabaseHelper db = new DatabaseHelper(context);
        List<Long> history = new ArrayList<>();
        try (Cursor cursor = db.getPeriod(userEmail)) {
            if (cursor != null && cursor.moveToFirst()) {
                int col = cursor.getColumnIndex(DatabaseHelper.KEY_PERIOD_DATE);
                do {
                    if (col >= 0 && !cursor.isNull(col)) {
                        long ts = cursor.getLong(col);
                        if (ts > 0) history.add(ts);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception ignored) {}

        if (history.isEmpty()) return false;

        CycleCalculator.CycleInfo info = CycleCalculator.calculateCycleInfoFromHistory(history, 28);
        if (info.isEstimated && history.size() < 1) return false;

        long latestMillis = history.get(0);
        long estimatedNextPeriodMillis = latestMillis + (long) (info.averageCycleLength * 24L * 60 * 60 * 1000);
        long reminderTimeMillis = estimatedNextPeriodMillis - (daysBefore * 24L * 60 * 60 * 1000);

        if (reminderTimeMillis <= System.currentTimeMillis()) {
            reminderTimeMillis = System.currentTimeMillis() + (10L * 60 * 1000); // fallback +10 mins
        }

        String title = "Estimated Period Reminder";
        String message = "Your next estimated period is starting on " + info.nextPeriodDate + " (Calendar Estimate).";

        int notifId = (userEmail.hashCode() & 0x7FFFFFFF) % 10000 + 100;
        return scheduleNotification(context, notifId, title, message, NotificationReceiver.CHANNEL_PERIOD, reminderTimeMillis);
    }

    public static boolean scheduleOvulationReminder(Context context, String userEmail) {
        if (userEmail == null || userEmail.isEmpty()) return false;

        DatabaseHelper db = new DatabaseHelper(context);
        List<Long> history = new ArrayList<>();
        try (Cursor cursor = db.getPeriod(userEmail)) {
            if (cursor != null && cursor.moveToFirst()) {
                int col = cursor.getColumnIndex(DatabaseHelper.KEY_PERIOD_DATE);
                do {
                    if (col >= 0 && !cursor.isNull(col)) {
                        long ts = cursor.getLong(col);
                        if (ts > 0) history.add(ts);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception ignored) {}

        if (history.isEmpty()) return false;

        CycleCalculator.CycleInfo info = CycleCalculator.calculateCycleInfoFromHistory(history, 28);
        long latestMillis = history.get(0);
        long estimatedOvulationMillis = latestMillis + (long) ((info.averageCycleLength - 14) * 24L * 60 * 60 * 1000);

        if (estimatedOvulationMillis <= System.currentTimeMillis()) {
            estimatedOvulationMillis = System.currentTimeMillis() + (30L * 60 * 1000); // fallback +30 mins
        }

        String title = "Estimated Ovulation Window";
        String message = "Your estimated fertile/ovulation window is active (" + info.ovulationWindow + "). Calendar estimate only.";

        int notifId = (userEmail.hashCode() & 0x7FFFFFFF) % 10000 + 200;
        return scheduleNotification(context, notifId, title, message, NotificationReceiver.CHANNEL_PERIOD, estimatedOvulationMillis);
    }

    public static void rescheduleAllOnBoot(Context context) {
        TokenManager tm = new TokenManager(context);
        String userEmail = tm.getUserEmail();
        if (userEmail != null && !userEmail.isEmpty()) {
            schedulePeriodReminder(context, userEmail, 2);
            scheduleOvulationReminder(context, userEmail);
        }
    }
}
