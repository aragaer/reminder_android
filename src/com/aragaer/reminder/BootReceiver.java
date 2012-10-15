package com.aragaer.reminder;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
//        if (PreferenceManager
//                .getDefaultSharedPreferences(context)
//                .getBoolean("jtt_bootup", true))
//            context.startService(new Intent(context, JTTService.class));
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification n = new Notification();
        n.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        nm.notify(1, n);
    }
}
