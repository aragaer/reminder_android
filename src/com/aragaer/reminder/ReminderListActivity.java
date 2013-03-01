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
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LongSparseArray;
import android.util.Log;
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
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ReminderListActivity extends Activity {
	int size, space, drag_size, border;
	GridView list;
	DragDropAdapter adapter;
	Bitmap dragged, background = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
	final LongSparseArray<Bitmap> cached_bitmaps = new LongSparseArray<Bitmap>();

	final RibbonDrawHandler draw = new RibbonDrawHandler();

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

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Resources r = getResources();
		size = r.getDimensionPixelSize(R.dimen.tile_size);
		draw.space = space = r.getDimensionPixelSize(R.dimen.tile_space);
		draw.ribbon = r.getDimensionPixelSize(R.dimen.tile_ribbon);
		drag_size = r.getDimensionPixelSize(R.dimen.view_glyph_size);
		border = r.getDimensionPixelSize(R.dimen.border_width);

		startService(new Intent(this, ReminderService.class));
		list = new GridView(this) {
			protected void onDraw(Canvas canvas) {
				canvas.drawBitmap(background, 0, 0, null);
				super.onDraw(canvas);
				if (dragged != null)
					canvas.drawBitmap(dragged, x - dragged.getWidth() / 2, y - dragged.getHeight() / 2, null);
			}

			protected void onSizeChanged(int w, int h, int oldw, int oldh) {
				background.recycle();
				background = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
				draw.canvas.setBitmap(background);
				draw.columns = getNumColumns();
				draw.redraw_background(adapter.getCount());
			}
		};
		list.setNumColumns(-1);
		list.setColumnWidth(size);
		list.setPadding(space, space, space, space);
		draw.paint.setStrokeWidth(border);

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
		final Drawable[] border = { Bitmaps.border(r.getDimensionPixelSize(R.dimen.border_width), Bitmaps.colors[Bitmaps.COLOR_BLUE]) };
		adapter = new DragDropAdapter(new CursorAdapter(this, cursor, true) {
			public View newView(Context context, Cursor cursor, ViewGroup parent) {
				LayerDrawable back = new LayerDrawable(border);
				back.setLayerInset(0, space, space, space, space);
				ImageView result = new ImageView(parent.getContext()) {
					public void onMeasure(int wms, int hms) {
					    super.onMeasure(wms, wms);
					}
				};
				result.setBackgroundDrawable(back);
				return result;
			}

			public void bindView(View view, Context context, Cursor cursor) {
				final long id = cursor.getLong(0);
				Bitmap bmp = cached_bitmaps.get(id);
				if (bmp == null) {
					final ReminderItem item = ReminderProvider.getItem(cursor);
					bmp = Bitmaps.memo_bmp(context, item, size);
					cached_bitmaps.put(id, bmp);
				}
				((ImageView) view).setImageBitmap(bmp);
			}
		}) {
			void handle_drag_drop(int from, int to) {
				ReminderProvider.reorder(ReminderListActivity.this, from, to);
			}
		};
		list.setAdapter(adapter);
		list.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> arg0, View view,
					int position, long id) {
				adapter.drag_start(position);
				dragged = Bitmap.createScaledBitmap(cached_bitmaps.get(id), drag_size, drag_size, true);
				list.invalidate();
				return true;
			}
		});
		list.setSelector(Bitmaps.inset_border(r.getDimensionPixelSize(R.dimen.border_width), Bitmaps.colors[Bitmaps.COLOR_BLUE], 0x8033b5e5, space));

		adapter.registerDataSetObserver(dso);
		draw.green_zone = ReminderService.n_glyphs(width, notification_size);
		draw.yellow_zone = ReminderService.n_glyphs(height, notification_size);

		setContentView(list);
		getContentResolver().registerContentObserver(ReminderProvider.content_uri, true, observer);
	}

	DataSetObserver dso = new DataSetObserver() {
		int old_count = 0;
		private void check_send() {
			int count = adapter.getCount();
			if (count == old_count)
				return;
			old_count = count;
			draw.sendEmptyMessage(count);
		}
		public void onChanged() {
			check_send();
		}
		public void onInvalidated() {
			check_send();
		}
	};

	ContentObserver observer = new ContentObserver(new Handler()) {
		@SuppressLint("NewApi")
		public void onChange(boolean selfChange) {
			this.onChange(selfChange, null);
		}
		@SuppressLint("Override")
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

class RibbonDrawHandler extends Handler {
	final Paint paint = new Paint(0x07);
	final Path ribbon_path = new Path();
	int columns;
	int space, ribbon;
	int green_zone, yellow_zone;
	final Canvas canvas = new Canvas();

	private void draw_ribbon_at(int x, int y) {
		canvas.setMatrix(null);
		canvas.translate(x + space * 2, y + space * 2);
		canvas.clipRect(0, 0, ribbon * 2, ribbon * 2, Op.REPLACE);

		canvas.drawColor(0, Mode.CLEAR);
		canvas.drawPath(ribbon_path, paint);
	}

	void redraw_background(int i) {
		paint.setAlpha(192);
		paint.setStyle(Style.FILL);

		ribbon_path.reset();
		ribbon_path.moveTo(ribbon, 0);
		ribbon_path.lineTo(ribbon * 2, 0);
		ribbon_path.lineTo(0, ribbon * 2);
		ribbon_path.lineTo(0, ribbon);
		ribbon_path.close();

		final int tile_size = (canvas.getWidth() - space * 2)/columns;
		int x = (i % columns) * tile_size;
		int y = (i / columns) * tile_size;

		paint.setColor(Bitmaps.colors[Bitmaps.COLOR_RED]);
		while (i-- > green_zone) {
			if (x < tile_size) {
				x = columns * tile_size;
				y -= tile_size;
			}
			x -= tile_size;
			draw_ribbon_at(x, y);
			if (i == yellow_zone)
				paint.setColor(Bitmaps.colors[Bitmaps.COLOR_YELLOW]);
		}
	}

	public void handleMessage(Message msg) {
		redraw_background(msg.what);
	}
}
