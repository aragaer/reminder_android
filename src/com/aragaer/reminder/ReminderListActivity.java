package com.aragaer.reminder;

import com.aragaer.simpleactionbar.ActionBar;
import com.aragaer.simpleactionbar.Activity;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.ListView;
import android.widget.TextView;

public class ReminderListActivity extends Activity {
	ListView list;

	@SuppressLint("NewApi")
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

		ActionBar ab = getActionBar();
		ab.setTitle(R.string.app_name);
		setContentView(list);
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

	public boolean onCreateActionBarMenu(Menu menu) {
		menu.add(Menu.NONE, R.string.menu_settings, Menu.NONE, R.string.menu_settings)
				.setIcon(R.drawable.ic_sysbar_quicksettings)
				.setIntent(new Intent(this, ReminderSettings.class));
		menu.add(Menu.NONE, R.string.add_new, Menu.NONE, R.string.add_new)
				.setIcon(R.drawable.content_new)
				.setIntent(new Intent(this, ReminderCreateActivity.class));
		return true;
	}
}

