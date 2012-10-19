package com.aragaer.reminder;

import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RemoteViews;

public class ReminderService extends Service implements View.OnTouchListener {
    private static final int intent_flags = Intent.FLAG_ACTIVITY_NEW_TASK
            | Intent.FLAG_ACTIVITY_CLEAR_TOP
            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED;
    static boolean window_created = false;

    public IBinder onBind(Intent i) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        handleCommand(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleCommand(intent);
        return START_STICKY;
    }

    private static View click_catcher;
    static float x = -1;
    private void handleCommand(Intent command) {
        Resources r = getResources(); 
    	int size = r.getDimensionPixelSize(R.dimen.notification_height) - 2 * r.getDimensionPixelSize(R.dimen.notification_glyph_margin);
        int color = Color.parseColor(getString(R.color.simple)); // FIXME: remove this later
        Cursor cursor = getContentResolver().query(ReminderProvider.content_uri, null, null, null, null);
        final List<ReminderItem> list = ReminderProvider.getAll(cursor);
        cursor.close();

        Notification n = new Notification(list.isEmpty() ? R.drawable.notify : R.drawable.notify_reminder,
                getString(R.string.app_name), System.currentTimeMillis());
        RemoteViews rv = new RemoteViews(getPackageName(), R.layout.notification);
        rv.removeAllViews(R.id.wrap);

        list.add(new ReminderItem(ReminderListActivity.add_new_bmp(this),
                getString(R.string.add_new)));

        for (ReminderItem item : list) {
            final RemoteViews image = new RemoteViews(getPackageName(),
                    R.layout.image);
            image.setImageViewBitmap(R.id.image, item.getGlyph(size));
            image.setInt(R.id.image, "setColorFilter", color);
            rv.addView(R.id.wrap, image);
        }
        n.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;

        n.contentView = rv;
        final Intent i = new Intent(this, ReminderListActivity.class);
        i.addFlags(intent_flags);
        n.contentIntent = PendingIntent.getActivity(this, 0, i, 0);
        if (!window_created) {
//          WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
//          WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
//          WindowManager.LayoutParams
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(1, 1, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, 0x50128, -3);
            lp.gravity = Gravity.LEFT | Gravity.TOP;
            lp.x = 0;
            click_catcher = new View(this);
            click_catcher.setOnTouchListener(this);
            ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).addView(click_catcher, lp);
            window_created = true;
        }
        final String action = command == null ? null : command.getAction();
        if (action == null
                || action.equals("com.aragaer.reminder.ServiceStart")) {
            startForeground(1, n);
        } else if (action.equals("com.aragaer.reminder.ReminderUpdate")) {
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(1, n);
        }
    }

    public void onDestroy() {
        ((WindowManager) getSystemService("window")).removeView(click_catcher);
    }

    public boolean onTouch(View v, MotionEvent event) {
        x = event.getX();
        return false;
    }
}
