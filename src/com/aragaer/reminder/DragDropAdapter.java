package com.aragaer.reminder;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

abstract class DragDropAdapter extends BaseAdapter {
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
		handle_drag_drop(this.from, to);
		this.from = -1;
		this.to = -1;
	}

	public DragDropAdapter(BaseAdapter inner) {
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
			if (observers++ == 0)
				inner.registerDataSetObserver(dso);
		}
	}

	public void unregisterDataSetObserver(DataSetObserver observer) {
		super.unregisterDataSetObserver(observer);
		synchronized (this) {
			if (--observers == 0)
				inner.unregisterDataSetObserver(dso);
		}
	}

	abstract void handle_drag_drop(int from, int to);
}
