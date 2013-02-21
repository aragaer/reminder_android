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
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ReminderListActivity extends Activity {
	int size, space, drag_size, border;
	GridView list;
	DragDropAdapter adapter;
	Bitmap dragged, background = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

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
		size = r.getDimensionPixelSize(R.dimen.tile_size);
		space = r.getDimensionPixelSize(R.dimen.tile_space);
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
				canvas.setBitmap(background);
				redraw_background();
			}
		};
		list.setNumColumns(-1);
		list.setColumnWidth(size);
//		list.setHorizontalSpacing(space);
//		list.setVerticalSpacing(space);
		list.setPadding(space, space, space, space);
		paint.setStrokeWidth(border);

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
				return new ImageView(parent.getContext()) {
					public void onMeasure(int wms, int hms) {
					    super.onMeasure(wms, wms);
					}
				};
			}

			public void bindView(View view, Context context, Cursor cursor) {
				ReminderItem item = ReminderProvider.getItem(cursor);
				((ImageView) view).setImageBitmap(Bitmaps.memo_bmp(context, item, size));
			}
		});
		list.setAdapter(adapter);
		list.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> arg0, View view,
					int position, long arg3) {
				adapter.drag_start(position);
				dragged = Bitmaps.memo_bmp(ReminderListActivity.this, ReminderProvider.getItem((Cursor) adapter.getItem(position)), drag_size);
				list.invalidate();
				return true;
			}
		});

		adapter.registerDataSetObserver(dso);
		adapter.green_zone = ReminderService.n_glyphs(width, notification_size);
		adapter.yellow_zone = ReminderService.n_glyphs(height, notification_size);

		setContentView(list);
		getContentResolver().registerContentObserver(ReminderProvider.content_uri, true, observer);
	}

	final Paint paint = new Paint(0x07);
	final Path path = new Path();
	final RectF round = new RectF();
	final Canvas canvas = new Canvas();

	private void draw_background_item(int color, int num, int inset) {
		int columns = list.getNumColumns();
		int tile_size = (list.getWidth() - space * 2)/columns;
		int r = space - inset;
		round.set(-r, -r, r, r);
		int col = num % columns;
		int row = num / columns;
		int start = space / 2 + inset;
		int end = tile_size - space * 5 / 2 + inset;

		canvas.setMatrix(null);
		canvas.translate(space + col * tile_size, space + row * tile_size);
		canvas.clipRect(0, 0, tile_size, tile_size, Op.REPLACE);

		paint.setColor(color);
		paint.setAlpha(128);
		paint.setStyle(Style.STROKE);

		round.offsetTo(start, start);
		path.addArc(round, 180, 90);
		round.offsetTo(end, start);
		path.arcTo(round, 270, 90);
		round.offsetTo(end, end);
		path.arcTo(round, 0, 90);
		round.offsetTo(start, end);
		path.arcTo(round, 90, 90);
		path.close();

		canvas.drawPath(path, paint);
		path.reset();
	}

	private void draw_ribbon(int color, int num, int inset) {
		int columns = list.getNumColumns();
		int tile_size = (list.getWidth() - space * 2)/columns;
		int col = num % columns;
		int row = num / columns;
		int left = space / 2 + inset;
		int top = space / 2 + inset;

		canvas.setMatrix(null);
		canvas.translate(space + col * tile_size, space + row * tile_size);
		canvas.clipRect(0, 0, tile_size, tile_size, Op.REPLACE);

		path.moveTo(left + space, top);
		path.lineTo(left + space * 2, top);
		path.lineTo(left, left + space * 2);
		path.lineTo(left, top + space);
		path.close();

		paint.setColor(color);
		paint.setAlpha(192);
		paint.setStyle(Style.FILL);
		canvas.clipRect(left, top, left + space * 2, top + space * 2, Op.REPLACE);
		canvas.drawColor(0, Mode.CLEAR);
		canvas.drawPath(path, paint);
		path.reset();
	}

	@SuppressLint("HandlerLeak")
	final Handler draw = new Handler() {
		public void handleMessage(Message msg) {
			redraw_background();
		}
	};

	void redraw_background() {
		int cnt = adapter.getCount();
		int inset = border * 2;
		for (int i = 0; i < cnt; i++) {
			if (i >= adapter.yellow_zone)
				draw_ribbon(Bitmaps.colors[Bitmaps.COLOR_RED], i, inset);
			else if (i >= adapter.green_zone)
				draw_ribbon(Bitmaps.colors[Bitmaps.COLOR_YELLOW], i, inset);
			draw_background_item(Bitmaps.colors[Bitmaps.COLOR_BLUE], i, inset);
		}
	}

	DataSetObserver dso = new DataSetObserver() {
		public void onChanged() {
			draw.sendEmptyMessage(0);
		}
		public void onInvalidated() {
			draw.sendEmptyMessage(0);
		}
	};

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
	int green_zone, yellow_zone;
	Drawable green, yellow, red, white;

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
		green = Bitmaps.border(r.getDimensionPixelSize(R.dimen.border_width), Bitmaps.colors[Bitmaps.COLOR_GREEN]);
		yellow = Bitmaps.border(r.getDimensionPixelSize(R.dimen.border_width), Bitmaps.colors[Bitmaps.COLOR_YELLOW]);
		red = Bitmaps.border(r.getDimensionPixelSize(R.dimen.border_width), Bitmaps.colors[Bitmaps.COLOR_RED]);
		white = Bitmaps.border(r.getDimensionPixelSize(R.dimen.border_width), Bitmaps.colors[Bitmaps.COLOR_BLUE]);
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
