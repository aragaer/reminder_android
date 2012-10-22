package com.aragaer.reminder;

import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RemoteViews;

public class ReminderService extends Service implements View.OnTouchListener {
	private static final boolean multiple_intents = Build.VERSION.SDK_INT > 13; // ICS+
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
		if (!multiple_intents && !window_created) {
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
		registerReceiver(update, new IntentFilter(ReminderProvider.UPDATE_ACTION));
		startForeground(1, buildNotification(this));
	}

	public static List<ReminderItem> list;

	private static final String PKG_NAME = ReminderService.class.getPackage().getName();
	private static Notification buildNotification(Context ctx) {
		Resources r = ctx.getResources();
		int height = r.getDimensionPixelSize(R.dimen.notification_height);
		int padding = r.getDimensionPixelSize(R.dimen.notification_glyph_margin);
		int size = height - padding * 2;
		int color = Color.argb(128, 0, 0, 0); // FIXME: remove this later

		DisplayMetrics dm = r.getDisplayMetrics();
		int num = dm.widthPixels / height;

		Cursor cursor = ctx.getContentResolver()
				.query(ReminderProvider.content_uri, null, null, null, null);
		list = ReminderProvider.getAllSublist(cursor, num - 2);
		cursor.close();

		Notification n = new Notification(
				list.isEmpty()
					? R.drawable.notify
					: R.drawable.notify_reminder,
				ctx.getString(R.string.app_name),
				System.currentTimeMillis());
		RemoteViews rv;

		list.add(new ReminderItem(ReminderListActivity.add_new_bmp(ctx), ""));
		list.add(new ReminderItem(ReminderListActivity.list_bmp(ctx), ""));

		if (multiple_intents) {
			rv = new RemoteViews(PKG_NAME, R.layout.notification);
			rv.removeAllViews(R.id.wrap);
			for (int i = 0; i < list.size(); i++) {
				final ReminderItem item = list.get(i);
				final RemoteViews image = new RemoteViews(PKG_NAME, R.layout.image);
				Intent intent;
				switch (list.size() - i) {
				case 1:
					intent = new Intent(ctx, ReminderListActivity.class);
					break;
				case 2:
					intent = new Intent(ctx, ReminderCreateActivity.class);
					break;
				default:
					intent = new Intent(ctx, ReminderViewActivity.class);
					intent.putExtra("reminder_id", item._id);
					intent.setAction("View " + item._id);
					break;
				}
				intent.addFlags(intent_flags);
				final PendingIntent pi = PendingIntent.getActivity(ctx, 0, intent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				image.setOnClickPendingIntent(R.id.image, pi);
				image.setImageViewBitmap(R.id.image, item.getGlyph(size));
				image.setInt(R.id.image, "setColorFilter", color);
				rv.addView(R.id.wrap, image);
			}
		} else {
			rv = new RemoteViews(PKG_NAME, R.layout.notification);
			rv.removeAllViews(R.id.wrap);

			final RemoteViews image = new RemoteViews(PKG_NAME, R.layout.image);
			Bitmap bmp = Bitmap.createBitmap(height * list.size() - padding * 2, size, Config.ARGB_8888);
			Canvas c = new Canvas(bmp);
			Paint p = new Paint(0x7);
			for (int i = 0; i < list.size(); i++) {
				final ReminderItem item = list.get(i);
				c.drawBitmap(item.getGlyph(size), i * height, 0, p);
			}
			image.setImageViewBitmap(R.id.image, bmp);
			image.setInt(R.id.image, "setColorFilter", color);
			rv.addView(R.id.wrap, image);
		}
		n.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;

		n.contentView = rv;
		final Intent i = new Intent(ctx, multiple_intents ? ReminderListActivity.class : ReminderCatcher.class);
		i.addFlags(intent_flags);
		n.contentIntent = PendingIntent.getActivity(ctx, 0, i, 0);

		return n;
	}

	private final BroadcastReceiver update = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(1, buildNotification(context));
		}
	};

	public void onDestroy() {
		((WindowManager) getSystemService("window")).removeView(click_catcher);
		unregisterReceiver(update);
	}

	public boolean onTouch(View v, MotionEvent event) {
		x = event.getRawX();
		return false;
	}
}
