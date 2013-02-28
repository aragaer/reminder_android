package com.aragaer.reminder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ReminderListActivity extends Activity {
	int size, space, drag_size;
	GridView list;
	DragDropAdapter adapter;
	Bitmap dragged;

	int x, y;
	OnTouchListener touch = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			x = (int) event.getX();
			y = (int) event.getY();
			if (!adapter.inDrag())
				return false;
			int pos = list.pointToPosition(x, y);
			if (pos == GridView.INVALID_POSITION) {
				if (x < 0 || y < 0)
					pos = 0;
				else
					pos = adapter.getCount() - 1;
			}
			switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
				adapter.drop_at(pos);
				list.invalidate();
				dragged.recycle();
				dragged = null;
				return true;
			case MotionEvent.ACTION_MOVE:
				adapter.drag_to(pos);
				list.invalidate();
				return true;
			default:
				return false;
			}
		}
	};

	Paint p = new Paint();
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		p.setStyle(Style.FILL);
		p.setColor(Color.WHITE);
		Resources r = getResources();
		startService(new Intent(this, ReminderService.class));
		list = new GridView(this) {
			protected void onDraw(Canvas canvas) {
				super.onDraw(canvas);
				if (dragged != null)
					canvas.drawBitmap(dragged, x - dragged.getWidth() / 2, y - dragged.getHeight() / 2, null);
			}
		};
		size = r.getDimensionPixelSize(R.dimen.tile_size);
		space = r.getDimensionPixelSize(R.dimen.tile_space);
		drag_size = r.getDimensionPixelSize(R.dimen.view_glyph_size);
		list.setNumColumns(-1);
		list.setColumnWidth(size);
		list.setHorizontalSpacing(0);
		list.setVerticalSpacing(0);
		list.setPadding(space, space, space, space);

		int width = r.getDisplayMetrics().widthPixels;
		int height = r.getDisplayMetrics().heightPixels;
		if (width > height) { // make it portrait
			height = width;
			width = r.getDisplayMetrics().heightPixels;
		}
		int notification_size = r.getDimensionPixelSize(R.dimen.notification_height);

		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapter, View arg1,
					int position, long id) {
				startActivity(new Intent(ReminderListActivity.this,
						ReminderViewActivity.class).putExtra("reminder_id", id));
			}
		});

		list.setOnTouchListener(touch);

		Cursor cursor = getContentResolver().query(ReminderProvider.content_uri, null, null, null, null);
		adapter = new DragDropAdapter(r, new CursorAdapter(this, cursor) {
			public View newView(Context context, Cursor cursor, ViewGroup parent) {
				ImageView result = new ImageView(parent.getContext()) {
					public void onMeasure(int wms, int hms) {
					    super.onMeasure(wms, wms);
					}
				};
				result.setBackgroundResource(R.drawable.padded_border_normal);
				return result;
			}

			public void bindView(View view, Context context, Cursor cursor) {
				ReminderItem item = ReminderProvider.getItem(cursor);
				((ImageView) view).setImageBitmap(Bitmaps.memo_bmp(context, item, size));
			}
		});
		list.setSelector(R.drawable.padded_border_pressed);
		list.setAdapter(adapter);
		list.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> arg0, View view,
					int position, long arg3) {
				adapter.drag_start(position);
//				dragged.setBackgroundColor(Color.RED);
//				dragged.setImageDrawable(Bitmaps.border(5, Bitmaps.colors[Bitmaps.COLOR_RED]));
//				dragged.setImageBitmap(Bitmaps.memo_bmp(dragged.getContext(), ReminderProvider.getItem((Cursor) adapter.getItem(position)), drag_size));
//				dragged.setVisibility(View.VISIBLE);
				dragged = Bitmaps.memo_bmp(ReminderListActivity.this, ReminderProvider.getItem((Cursor) adapter.getItem(position)), drag_size);
				list.invalidate();
				return true;
			}
		});

		adapter.green = ReminderService.n_glyphs(width, notification_size);
		adapter.yellow = ReminderService.n_glyphs(height, notification_size);

		setContentView(list);
		getContentResolver().registerContentObserver(ReminderProvider.content_uri, true, observer);
	}

	ContentObserver observer = new ContentObserver(new Handler()) {
		@SuppressLint("NewApi")
		public void onChange(boolean selfChange) {
			this.onChange(selfChange, null);
		}
		public void onChange(boolean selfChange, Uri uri) {
			((CursorAdapter) ((DragDropAdapter) list.getAdapter()).inner)
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

	public void onDestroy() {
		super.onDestroy();
		getContentResolver().unregisterContentObserver(observer);
		((CursorAdapter) ((DragDropAdapter) list.getAdapter()).inner).getCursor().close();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, R.string.add_new, Menu.NONE, R.string.add_new)
				.setIcon(R.drawable.content_new)
				.setIntent(new Intent(this, ReminderCreateActivity.class))
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}
}

class DragDropAdapter extends BaseAdapter {
	private int observers = 0;
	private final DataSetObserver dso = new DataSetObserver() {
		public void onChanged() {
			notifyDataSetChanged();
		}
		public void onInvalidated() {
			notifyDataSetInvalidated();
		}
	};
	final BaseAdapter inner;
	private int from = -1, to = -1;
	int green, yellow;
	Drawable border_green, border_yellow, border_red;

	public boolean inDrag() {
		return from >= 0;
	}

	private final int translate(int position) {
		int result = position;
//		Log.d("translate", "from="+from+", to="+to+", pos="+position);
		if (from < 0)
			return position;
		if (position == to)
			return from;
		if (position == from)
			return position + (to < from ? -1 : 1);
		if (position > from)
			result++;
		if (position > to)
			result--;
		return result;
	}

	public void drag_start(int from) {
		this.from = from;
		this.to = from;
		notifyDataSetChanged();
	}

	public void drag_to(int to) {
		if (this.to == to)
			return;
		this.to = to;
		notifyDataSetChanged();
	}

	public void drop_at(int to) {
		// move stuff here!!
		this.from = -1;
		this.to = -1;
		notifyDataSetChanged();
	}

	public DragDropAdapter(Resources r, BaseAdapter inner) {
		this.inner = inner;
	}

	public int getCount() {
		return inner.getCount();
	}

	public Object getItem(int position) {
		return inner.getItem(translate(position));
	}

	public long getItemId(int position) {
		return inner.getItemId(translate(position));
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View res = inner.getView(translate(position), convertView, parent);
		res.setAlpha(position == to ? 0.5f : 1);

		return res;
	}

	public void registerDataSetObserver(DataSetObserver observer) {
		super.registerDataSetObserver(observer);
		synchronized (this) {
			if (observers == 0)
				inner.registerDataSetObserver(dso);
			observers++;
		}
	}

	public void unregisterDataSetObserver(DataSetObserver observer) {
		super.unregisterDataSetObserver(observer);
		synchronized (this) {
			observers--;
			if (observers == 0)
				inner.unregisterDataSetObserver(dso);
		}
	}
}
