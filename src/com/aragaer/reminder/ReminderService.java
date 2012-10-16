package com.aragaer.reminder;

import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class ReminderService extends Service {
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

	private void handleCommand(Intent command) {
        Notification n = new Notification(R.drawable.new_glyph, getString(R.string.app_name), System.currentTimeMillis());
        RemoteViews rv = new RemoteViews(getPackageName(), R.layout.notification);
        rv.removeAllViews(R.id.wrap);

		final List<ReminderItem> list = new ReminderDB(this).getAllMemos();
		list.add(new ReminderItem(ReminderListActivity.add_new_bmp(this), getString(R.string.add_new)));

		for (ReminderItem item : list) {
			final Intent i = new Intent(this, ReminderViewActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			i.putExtra("reminder_id", item._id);
			i.setAction("View " +item._id);
			final PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
	        final RemoteViews image = new RemoteViews(getPackageName(), R.layout.image);
	        image.setOnClickPendingIntent(R.id.image, pi);
	        image.setImageViewBitmap(R.id.image, item.getGlyph(40));
	        rv.addView(R.id.wrap, image);
		}
        n.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        n.contentView = rv;
		final Intent i = new Intent(this, ReminderListActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        n.contentIntent = PendingIntent.getActivity(this, 0, i, 0);

		final String action = command.getAction();
		if (action == null || action.equals("com.aragaer.reminder.ServiceStart")) {
	        startForeground(1, n);
		} else if (action.equals("com.aragaer.reminder.ReminderUpdate")) {
			((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(1, n);
		}
	}
}
