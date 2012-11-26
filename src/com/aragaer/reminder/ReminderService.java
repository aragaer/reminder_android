package com.aragaer.reminder;

import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Pair;
import android.widget.RemoteViews;

public class ReminderService extends Service {
	private static final boolean need_collapse = Build.VERSION.SDK_INT > 13; // ICS+
	private static final int intent_flags = Intent.FLAG_ACTIVITY_NEW_TASK
			| Intent.FLAG_ACTIVITY_CLEAR_TOP
			| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED;

	public static final String settings_changed = "com.aragaer.reminder.SETTINGS_CHANGE";

	public IBinder onBind(Intent i) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handleCommand(intent);
		return START_STICKY;
	}

	private void handleCommand(Intent command) {
		IntentFilter filter = new IntentFilter(ReminderProvider.UPDATE_ACTION);
		filter.addAction(settings_changed);
		registerReceiver(update, filter);

		registerReceiver(catcher, new IntentFilter(catcher_action));
		startForeground(1, buildNotification(this));
	}

	static List<Pair<Bitmap, Intent>> list = new ArrayList<Pair<Bitmap,Intent>>();

	private static final String PKG_NAME = ReminderService.class.getPackage().getName(); 
	private static Notification buildNotification(Context ctx) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
		boolean buttons_on_left = pref.getBoolean("notification_btn_left", false);
		Resources r = ctx.getResources();
		int height = r.getDimensionPixelSize(R.dimen.notification_height);

		int num = r.getDisplayMetrics().widthPixels / height;
		if (num > 7) // Hardcoded value, yo!
			num = 7;

		Cursor cursor = ctx.getContentResolver()
				.query(ReminderProvider.content_uri, null, null, null, null);
		List<ReminderItem> items = ReminderProvider.getAllSublist(cursor, num - 2);
		int lost = cursor.getCount() - items.size();
		cursor.close();

		list.clear();
		for (ReminderItem item : items)
			list.add(Pair.create(
					Bitmaps.memo_bmp(ctx, item),
					new Intent(ctx, ReminderViewActivity.class).putExtra(
							"reminder_id", item._id).setAction(
							"View " + item._id)));
		items.clear();

		@SuppressWarnings("deprecation")
		Notification n = new Notification(
				list.isEmpty()
					? R.drawable.notify
					: R.drawable.notify_reminder,
				ctx.getString(R.string.app_name),
				System.currentTimeMillis());

		Pair<Bitmap, Intent> list_btn = Pair.create(
				Bitmaps.list_bmp(ctx, lost), new Intent(ctx,
						ReminderListActivity.class).addFlags(intent_flags));
		Pair<Bitmap, Intent> new_btn = Pair.create(Bitmaps.add_new_bmp(ctx),
				new Intent(ctx, ReminderCreateActivity.class)
						.addFlags(intent_flags));
		int n_sym = list.size();
		if (buttons_on_left) {
			list.add(0, list_btn);
			list.add(0, new_btn);
			n_sym += 2;
		} else {
			list.add(list_btn);
			list.add(new_btn);
		}

		RemoteViews rv = new RemoteViews(PKG_NAME, R.layout.notification);
		rv.removeAllViews(R.id.wrap);
		rv.removeAllViews(R.id.wrap2);
		for (int i = 0; i < list.size(); i++) {
			final Pair<Bitmap, Intent> g2i = list.get(i);
			final RemoteViews image = new RemoteViews(PKG_NAME, R.layout.image);
			final PendingIntent pi = PendingIntent.getBroadcast(ctx, i,
					new Intent(catcher_action).putExtra("what", i), 0);
			image.setOnClickPendingIntent(R.id.image, pi);
			image.setImageViewBitmap(R.id.image, g2i.first);
			if (i < n_sym)
				rv.addView(R.id.wrap, image);
			else
				rv.addView(R.id.wrap2, image);
		}
		n.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR | Notification.FLAG_ONLY_ALERT_ONCE;

		n.contentView = rv;
		n.contentIntent = PendingIntent.getBroadcast(ctx, list.size(), new Intent(catcher_action), 0);

		return n;
	}

	private final BroadcastReceiver update = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(1, buildNotification(context));
		}
	};

	public static final String catcher_action = "com.aragaer.reminder.CATCH_ACTION";
	private final BroadcastReceiver catcher = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			int position = intent.getIntExtra("what", 99);
			Intent i = ReminderService.list == null
					|| position >= ReminderService.list.size()
				? new Intent(context, ReminderListActivity.class)
				: ReminderService.list.get(position).second;
			if (need_collapse)
				try {
					Object obj = context.getSystemService("statusbar");
				    Class.forName("android.app.StatusBarManager").getMethod("collapse", new Class[0]).invoke(obj, (Object[]) null);
				} catch (Exception e) {
					// do nothing, it's OK
				}
			context.startActivity(i.addFlags(intent_flags));
		}
	};

	public void onDestroy() {
		unregisterReceiver(update);
	}
}
