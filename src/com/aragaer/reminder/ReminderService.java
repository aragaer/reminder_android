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
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.os.Handler;
import android.util.Pair;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RemoteViews;

public class ReminderService extends Service implements View.OnTouchListener {
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

	private static boolean window_created = false;
	private static View click_catcher;
	static float x = -1, size;

	private void handleCommand(Intent command) {
		if (!window_created) {
			WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
					1, 1, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
					WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
					//							| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
					//							| WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
					//							| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
					| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
					PixelFormat.TRANSLUCENT);
			lp.gravity = Gravity.LEFT | Gravity.TOP;
			lp.x = 0;
			click_catcher = new View(this);
			click_catcher.setOnTouchListener(this);
			((WindowManager) getSystemService(Context.WINDOW_SERVICE)).addView(click_catcher, lp);
			window_created = true;
		}
		registerReceiver(update, new IntentFilter(settings_changed));
		registerReceiver(catcher, new IntentFilter(catcher_action));
		getContentResolver().registerContentObserver(ReminderProvider.content_uri, false, observer);
		startForeground(1, buildNotification(this));
	}

	static List<Pair<Bitmap, Intent>> list = new ArrayList<Pair<Bitmap, Intent>>();
	static int n_sym; // number of icons on the left

	private static final String PKG_NAME = ReminderService.class.getPackage().getName();

	private static Notification buildNotification(Context ctx) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		boolean invert = pref.getBoolean("notification_invert", true);
		Resources r = ctx.getResources();
		int height = r.getDimensionPixelSize(R.dimen.notification_height);
		int padding = r.getDimensionPixelSize(R.dimen.notification_glyph_margin);
		int size = height - padding * 2;
		int screen_width = r.getDisplayMetrics().widthPixels;
		int num = screen_width / height;

		list.clear();
		Cursor cursor = ctx.getContentResolver().query(
				ReminderProvider.content_uri, null, null, null, null);
		int max = num - 2;
		ReminderItem item = null;
		while (cursor.moveToNext() && --max > 0) {
			item = ReminderProvider.getItem(cursor, item);
			list.add(Pair.create(Bitmaps.memo_bmp(ctx, item, size, invert),
					new Intent(ctx, ReminderViewActivity.class)
							.putExtra("reminder_id", item._id)));
		}
		int n_sym = list.size();
		int lost = cursor.getCount() - n_sym;
		cursor.close();

		Notification n = new Notification(R.drawable.notify,
				ctx.getString(R.string.app_name), System.currentTimeMillis());

		Pair<Bitmap, Intent> list_btn = Pair.create(
				Bitmaps.list_bmp(ctx, lost, invert),
				new Intent(ctx, ReminderListActivity.class).addFlags(intent_flags));
		Pair<Bitmap, Intent> new_btn = Pair.create(
				Bitmaps.add_new_bmp(ctx, invert),
				new Intent(ctx, ReminderCreateActivity.class).addFlags(intent_flags));
		list.add(list_btn);
		list.add(new_btn);

		RemoteViews rv = new RemoteViews(PKG_NAME, R.layout.notification);
		rv.removeAllViews(R.id.wrap);
		RemoteViews image = new RemoteViews(PKG_NAME, R.layout.image);
		Bitmap bmp = Bitmap.createBitmap(screen_width - padding * 2, size, Config.ARGB_8888);
		Canvas c = new Canvas(bmp);
		int position = 0;
		for (int i = 0; i < list.size(); i++) {
			final Pair<Bitmap, Intent> glyph = list.get(i);
			if (i == n_sym)
				position += screen_width - height * list.size();
			c.drawBitmap(glyph.first, position, 0, null);
			position += height;
			glyph.first.recycle();
		}
		image.setImageViewBitmap(R.id.image, bmp);
		rv.addView(R.id.wrap, image);
		n.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR
				| Notification.FLAG_ONLY_ALERT_ONCE;

		n.iconLevel = n_sym;
		n.contentView = rv;
		n.contentIntent = PendingIntent.getBroadcast(ctx, list.size(),
				new Intent(catcher_action), 0);

		n.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR
				| Notification.FLAG_ONLY_ALERT_ONCE;

		return n;
	}

	private final BroadcastReceiver update = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
				.notify(1, buildNotification(context));
		}
	};

	ContentObserver observer = new ContentObserver(new Handler()) {
		public void onChange(boolean selfChange) {
			((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
				.notify(1, buildNotification(ReminderService.this));
		}
	};

	public static final String catcher_action = "com.aragaer.reminder.CATCH_ACTION";
	private final BroadcastReceiver catcher = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			int glyph_width = context.getResources().getDimensionPixelSize(
					R.dimen.notification_height);
			int position = (int) x / glyph_width;
			if (position >= n_sym) { // go from the right edge instead
				int scrw = context.getResources().getDisplayMetrics().widthPixels;
				position = list.size() - (scrw - (int) x) / glyph_width - 1;
				if (position < n_sym) // if we are i between
					position = n_sym; // point to the 'list' icon
			}
			context.startActivity(list.get(position).second.addFlags(intent_flags));
		}
	};

	public void onDestroy() {
		((WindowManager) getSystemService("window")).removeView(click_catcher);
		getContentResolver().unregisterContentObserver(observer);
		window_created = false;
	}

	public boolean onTouch(View v, MotionEvent event) {
		x = event.getRawX();
		return false;
	}
}
