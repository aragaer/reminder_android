package com.aragaer.reminder;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.DragEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListAdapter;

public abstract class DraggableGridView extends GridView implements OnItemLongClickListener {
	private static final ClipData cd = ClipData.newPlainText("", "");

	private final static class DrawableShadowBuilder extends DragShadowBuilder {
		private Drawable dragged;
		public void onDrawShadow(Canvas canvas) {
			dragged.draw(canvas);
		}
		public void onProvideShadowMetrics(Point size, Point touch) {
			final Rect rect = dragged.getBounds();
			rect.offsetTo(0, 0);
			size.set(rect.right, rect.bottom);
			touch.set(rect.centerX(), rect.centerY());
		}
	};

	private final DrawableShadowBuilder shadow = new DrawableShadowBuilder();
	private DragDropAdapter adapter;

	public abstract Drawable getDragDrawable(View view, int position, long id);

	public boolean onItemLongClick(AdapterView<?> arg0, View view,
			int position, long id) {
		shadow.dragged = getDragDrawable(view, position, id);
		startDrag(cd, shadow, null, 0);
		adapter.drag_start(position);
		return true;
	}

	public DraggableGridView(Context context) {
		super(context);
		setOnItemLongClickListener(this);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		this.adapter = (DragDropAdapter) adapter; // otherwise we get cast exception
		super.setAdapter(adapter);
	}

	@Override
	public boolean onDragEvent(DragEvent event) {
		final int drag_x = (int) event.getX();
		final int drag_y = (int) event.getY();
		int pos = pointToPosition(drag_x, drag_y);
		if (pos == GridView.INVALID_POSITION)
			pos = drag_x < 0 || drag_y < 0
				? 0
				: adapter.getCount() - 1;

		switch(event.getAction()) {
		case DragEvent.ACTION_DRAG_STARTED:
			return true;
		case DragEvent.ACTION_DRAG_LOCATION:
			adapter.drag_to(pos);
			return true;
		case DragEvent.ACTION_DROP:
			adapter.drop_at(pos);
			return true;
		default:
			return false;
		}
	}
}
