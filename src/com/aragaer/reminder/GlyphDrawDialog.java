package com.aragaer.reminder;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

public class GlyphDrawDialog extends Dialog {
	public static final int BITMAP_SIZE = 500;
	Bitmap bmp = Bitmap.createBitmap(BITMAP_SIZE, BITMAP_SIZE, Config.ARGB_8888);
	public GlyphDrawDialog(Context context) {
		super(context);
		setTitle(R.string.add_new);
		LinearLayout ll = new LinearLayout(context) {
			protected void onMeasure(int wms, int hms) {
				int w = MeasureSpec.getSize(wms);
				int h = MeasureSpec.getSize(hms);
				if (w == 0 || h == 0)
					return; // do nothing - size is quite invalid
				DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
				float maxw = dm.widthPixels * 0.8f;
				float maxh = dm.heightPixels * 0.8f;
				Log.d("DrawView", "ll "+w+":"+h+" max possible "+maxw+":"+maxh);

				float scale = Math.min(maxw/w, maxh/h);
				if (scale < 1) {
					w *= scale;
					h *= scale;
					Log.d("DrawView", "ll shrink to "+w+":"+h);
					wms = MeasureSpec.makeMeasureSpec(w, MeasureSpec.getMode(wms));
					hms = MeasureSpec.makeMeasureSpec(h, MeasureSpec.getMode(hms));
				}

				// now check all children - we might want to make it all even smaller
				int sumh = 0, minw = 0;
				int child_count = getChildCount();
				for (int i = 0 ; i < child_count; i++) {
					final View child = getChildAt(i);
					child.measure(wms, hms); // FIXME: Should be actual child's measurements, not total size!
					sumh += child.getMeasuredHeight();
					minw = Math.max(minw, child.getMeasuredWidth());
				}

				if (sumh > h) { // got to rescale again
					w *= h/sumh;
					h = sumh;
					Log.d("DrawView", "ll shrink again to "+w+":"+h);
					wms = MeasureSpec.makeMeasureSpec(w, MeasureSpec.getMode(wms));
					hms = MeasureSpec.makeMeasureSpec(h, MeasureSpec.getMode(hms));
				}

				for (int i = 0 ; i < child_count; i++) {
					final View child = getChildAt(i);
					child.measure(wms, hms);
					sumh += child.getMeasuredHeight();
					minw = Math.max(minw, child.getMeasuredWidth());
				}

				super.onMeasure(wms, hms);
				setMeasuredDimension(w, h);
			}
		};

		DrawView img = new DrawView(context, bmp);
		ll.addView(img);

		CheckBox extra = new CheckBox(context);
		extra.setText(R.string.add_extra);
		ll.addView(extra);

		Button btn = new Button(context);
		btn.setText(android.R.string.ok);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				GlyphDrawDialog.this.hide();
			}
		});
		ll.addView(btn);
		setContentView(ll);
	}
	protected void onMeasure(int w, int h) {
	}
}

class DrawView extends View implements OnTouchListener {
	Paint p_grid = new Paint(0x7), p = new Paint(0x7);
	boolean grid_drawn = false;
	int size;
	Bitmap bmp;
	Canvas c;
	Matrix m = new Matrix();

	public DrawView(Context context, Bitmap bmp) {
		super(context);
		p_grid.setColor(Color.LTGRAY);
		setFocusable(true);
        setFocusableInTouchMode(true);
        setOnTouchListener(this);
		this.bmp = bmp;
		c = new Canvas(bmp);
		p.setColor(Color.WHITE);
	}

	protected void onMeasure(int w, int h) {
		size = Math.min(MeasureSpec.getSize(w), MeasureSpec.getSize(h)) - 1;
		size -= size % 4;
		p.setStrokeWidth(GlyphDrawDialog.BITMAP_SIZE/40);
		m.setScale(1f*size/GlyphDrawDialog.BITMAP_SIZE, 1f*size/GlyphDrawDialog.BITMAP_SIZE);

		super.onMeasure(size+1, size+1);
		setMeasuredDimension(size+1, size+1);
		Log.d("DrawView", "Size is "+size);
	}

	public void onDraw(Canvas canvas) {
		final int cell = size / 4;
		for (int i = 0; i <= 4; i++) {
			canvas.drawLine(0, cell * i, size, cell * i, p_grid);
			canvas.drawLine(cell * i, 0, cell * i, size, p_grid);
		}
		canvas.drawBitmap(bmp, m, p);
	}

	float x, y;
    public boolean onTouch(View view, MotionEvent event) {
    	final float scale = 1f*GlyphDrawDialog.BITMAP_SIZE / size;
    	switch (event.getAction()) {
    	case MotionEvent.ACTION_DOWN:
    		x = event.getX() * scale;
    		y = event.getY() * scale;
    		c.drawCircle(x, y, GlyphDrawDialog.BITMAP_SIZE/80, p);
    		break;
    	case MotionEvent.ACTION_MOVE:
    		float ox = x;
    		float oy = y;
    		x = event.getX() * scale;
    		y = event.getY() * scale;
    		c.drawLine(ox, oy, x, y, p);
    		c.drawCircle(x, y, GlyphDrawDialog.BITMAP_SIZE/80, p);
    		break;
    	default:
    		return false;
    	}
    	invalidate();
        return true;
    }
}
