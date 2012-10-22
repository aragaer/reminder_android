package com.aragaer.reminder;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.WrapperListAdapter;

public class ReminderListActivity extends Activity {
	ListView list;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startService(new Intent(this, ReminderService.class));
		list = new ListView(this);
		list.setId(1);

		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapter, View arg1,
					int position, long id) {
				if (position + 1 == adapter.getCount())
					startActivity(new Intent(ReminderListActivity.this, ReminderCreateActivity.class));
				else {
					Intent i = new Intent(ReminderListActivity.this, ReminderViewActivity.class);
					i.putExtra("reminder_id", id);
					startActivity(i);
				}
			}
		});

		View add_new = ViewGroup.inflate(this, android.R.layout.activity_list_item, null);
		((ImageView) add_new.findViewById(android.R.id.icon)).setImageBitmap(add_new_bmp(this));
		((TextView) add_new.findViewById(android.R.id.text1)).setText(R.string.add_new);
		list.addFooterView(add_new);

		registerForContextMenu(list);
		list.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> av, View v, int position, long id) {
				boolean consume = position + 1 == av.getCount(); // intercept long press for "add new"
				av.setHapticFeedbackEnabled(!consume);
				return consume;
			}
		});

		Cursor cursor = getContentResolver().query(ReminderProvider.content_uri, null, null, null, null);
		list.setAdapter(new CursorAdapter(this, cursor) {
			public View newView(Context context, Cursor cursor, ViewGroup parent) {
				return ViewGroup.inflate(parent.getContext(), android.R.layout.activity_list_item, null);
			}

			public void bindView(View view, Context context, Cursor cursor) {
				ReminderItem item = ReminderProvider.getItem(cursor);
				((ImageView) view.findViewById(android.R.id.icon)).setImageBitmap(item.getGlyph(50));
				((TextView) view.findViewById(android.R.id.text1)).setText(item.getText());
			}
		});
		setContentView(list);
		registerReceiver(update, new IntentFilter(ReminderProvider.UPDATE_ACTION));
	}

	private final BroadcastReceiver update = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			((CursorAdapter) ((WrapperListAdapter) list.getAdapter()).getWrappedAdapter())
					.changeCursor(getContentResolver().query(ReminderProvider.content_uri, null, null, null, null));
		}
	};

	static public Bitmap add_new_bmp(Context ctx) {
		Resources r = ctx.getResources();
		int size = r.getDimensionPixelSize(R.dimen.notification_height) - 2
				* r.getDimensionPixelSize(R.dimen.notification_glyph_margin);
		Bitmap b = Bitmap.createBitmap(size, size, Config.ARGB_8888);
		Canvas c = new Canvas(b);
		Paint p = new Paint(0x07);
		p.setTextSize(size);
		p.setColor(Color.WHITE);
		p.setShadowLayer(1, 0, 0, Color.BLACK);
		Rect bounds = new Rect();
		p.getTextBounds("+", 0, 1, bounds);
		Drawable d = ctx.getResources().getDrawable(R.drawable.new_glyph);
		d.setBounds(0, 0, size, size);
		d.draw(c);
		c.drawText("+", size / 2 - bounds.centerX(), size / 2 - bounds.centerY(), p);
		return b;
	}

	static public Bitmap list_bmp(Context ctx) {
		Resources r = ctx.getResources();
		int size = r.getDimensionPixelSize(R.dimen.notification_height) - 2
				* r.getDimensionPixelSize(R.dimen.notification_glyph_margin);
		Bitmap t = BitmapFactory.decodeResource(r, R.drawable.list64x64);
		Bitmap t2 = Bitmap.createScaledBitmap(t, size, size, true);
		Bitmap b = Bitmap.createBitmap(size, size, Config.ARGB_8888);
		Canvas c = new Canvas(b);
		Drawable d = ctx.getResources().getDrawable(R.drawable.new_glyph);
		d.setBounds(0, 0, size, size);
		d.draw(c);
		c.drawBitmap(t2, 0, 0, new Paint(0x07));
		return b;
	}

	private static final int DELETE = 1;
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(Menu.NONE, DELETE, Menu.NONE, R.string.delete);
	}

	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case DELETE:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			getContentResolver().delete(ReminderProvider.content_uri, "_id=?",
					new String[] { String.format("%d", info.id) });
			break;
		}
		return true;
	}

	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(update);
		((CursorAdapter) ((WrapperListAdapter) list.getAdapter())
				.getWrappedAdapter()).getCursor().close();
	}
}
