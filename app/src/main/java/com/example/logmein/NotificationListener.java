package com.example.logmein;

import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class NotificationListener extends NotificationListenerService {
    public static final String NOTIFICATION_POSTED = "NotificationPosted";

    public static String lastPackageName;
    public static String lastNotificationTitle;
    public static String lastNotificationText;

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        CharSequence notificationTitle = sbn.getNotification().extras.getCharSequence("android.title");
        CharSequence notificationText = sbn.getNotification().extras.getCharSequence("android.text");

        lastPackageName = packageName;
        lastNotificationTitle = notificationTitle != null ? notificationTitle.toString() : "No Title";
        lastNotificationText = notificationText != null ? notificationText.toString() : "No Text";

        // Send a broadcast to update the UI
        Intent intent = new Intent(NOTIFICATION_POSTED);
        sendBroadcast(intent);
    }
}

