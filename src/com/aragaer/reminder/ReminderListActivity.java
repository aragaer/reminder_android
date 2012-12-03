package com.aragaer.reminder;

import com.markupartist.android.widget.ActionBar;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ReminderListActivity extends Activity {
	ListView list;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startService(new Intent(this, ReminderService.class));
		list = new ListView(this);

		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapter, View arg1,
					int position, long id) {
				startActivity(new Intent(ReminderListActivity.this,
						ReminderViewActivity.class).putExtra("reminder_id", id));
			}
		});

		registerForContextMenu(list);

		Cursor cursor = getContentResolver().query(ReminderProvider.content_uri, null, null, null, null);
		list.setAdapter(new CursorAdapter(this, cursor) {
			public View newView(Context context, Cursor cursor, ViewGroup parent) {
				return ViewGroup.inflate(parent.getContext(), android.R.layout.activity_list_item, null);
			}

			public void bindView(View view, Context context, Cursor cursor) {
				ReminderItem item = ReminderProvider.getItem(cursor);
				((ImageView) view.findViewById(android.R.id.icon)).setImageBitmap(Bitmaps.memo_bmp(context, item, 64, false));
				((TextView) view.findViewById(android.R.id.text1)).setText(item.getText());
			}
		});

		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		ActionBar ab = new ActionBar(this, null);
		ll.addView(ab);
		ab.setTitle(R.string.title_activity_main);
		ab.setHomeLogo(R.drawable.ic_launcher);
		ab.addAction(new ActionBar.IntentAction(this,
				new Intent(this, ReminderCreateActivity.class),
				R.drawable.content_new));
		ll.addView(list);
		setContentView(ll);
		getContentResolver().registerContentObserver(ReminderProvider.content_uri, true, observer);

	}

	ContentObserver observer = new ContentObserver(new Handler()) {
		public void onChange(boolean selfChange) {
			((CursorAdapter) list.getAdapter())
					.changeCursor(getContentResolver().query(
							ReminderProvider.content_uri, null, null, null, null));
		}
	};

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
			getContentResolver().delete(ContentUris.withAppendedId(ReminderProvider.content_uri, info.id), null, null);
			break;
		}
		return true;
	}

	public void onDestroy() {
		super.onDestroy();
		getContentResolver().unregisterContentObserver(observer);
		((CursorAdapter) list.getAdapter()).getCursor().close();
	}
}

