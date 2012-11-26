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
import android.util.DisplayMetrics;
import android.widget.RemoteViews;

public class ReminderService extends Service {
	private static final boolean need_collapse = Build.VERSION.SDK_INT > 13; // ICS+
	private static final int intent_flags = Intent.FLAG_ACTIVITY_NEW_TASK
			| Intent.FLAG_ACTIVITY_CLEAR_TOP
			| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED;
	static boolean window_created = false;

	public static final String settings_changed = "com.aragaer.reminder.SETTINGS_CHANGE";

	public IBinder onBind(Intent i) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handleCommand(intent);
		return START_STICKY;
	}

	static float size;
	private void handleCommand(Intent command) {
		IntentFilter filter = new IntentFilter(ReminderProvider.UPDATE_ACTION);
		filter.addAction(settings_changed);
		registerReceiver(update, filter);

		registerReceiver(catcher, new IntentFilter(catcher_action));
		startForeground(1, buildNotification(this));
	}

	static List<Glyph2Intent> list = new ArrayList<Glyph2Intent>();
	static int n_sym;	// number of icons on the left

	static boolean buttons_on_left;
	private static final String PKG_NAME = ReminderService.class.getPackage().getName(); 
	private static Notification buildNotification(Context ctx) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
		buttons_on_left = pref.getBoolean("notification_btn_left", false);
		Resources r = ctx.getResources();
		int height = r.getDimensionPixelSize(R.dimen.notification_height);

		DisplayMetrics dm = r.getDisplayMetrics();
		int num = dm.widthPixels / height;
		if (num > 7) // Hardcoded value, yo!
			num = 7;

		Cursor cursor = ctx.getContentResolver()
				.query(ReminderProvider.content_uri, null, null, null, null);
		List<ReminderItem> items = ReminderProvider.getAllSublist(cursor, num - 2);
		int lost = cursor.getCount() - items.size();
		cursor.close();

		list.clear();
		for (ReminderItem item : items) {
			Intent intent = new Intent(ctx, ReminderViewActivity.class)
				.putExtra("reminder_id", item._id)
				.setAction("View " + item._id);
			list.add(new Glyph2Intent(Bitmaps.memo_bmp(ctx, item), intent));
		}
		items.clear();

		@SuppressWarnings("deprecation")
		Notification n = new Notification(
				list.isEmpty()
					? R.drawable.notify
					: R.drawable.notify_reminder,
				ctx.getString(R.string.app_name),
				System.currentTimeMillis());

		Intent intent = new Intent(ctx, ReminderListActivity.class);
		intent.addFlags(intent_flags);
		Glyph2Intent list_btn = new Glyph2Intent(Bitmaps.list_bmp(ctx, lost), intent);
		intent = new Intent(ctx, ReminderCreateActivity.class);
		intent.addFlags(intent_flags);
		Glyph2Intent new_btn = new Glyph2Intent(Bitmaps.add_new_bmp(ctx), intent);
		n_sym = list.size();
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
			final Glyph2Intent g2i = list.get(i);
			final RemoteViews image = new RemoteViews(PKG_NAME, R.layout.image);
			final PendingIntent pi = PendingIntent.getBroadcast(ctx, i,
					new Intent(catcher_action).putExtra("what", i), 0);
			image.setOnClickPendingIntent(R.id.image, pi);
			image.setImageViewBitmap(R.id.image, g2i.image);
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
				: ReminderService.list.get(position).intent;
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

	public static final class Glyph2Intent {
		public Bitmap image;
		public Intent intent;
		public Glyph2Intent (Bitmap b, Intent i) {
			image = b;
			intent = i;
		}
	}
}
