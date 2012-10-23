package com.aragaer.reminder;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;

public class Bitmaps {
	static final ColorFilter inv80 = new ColorMatrixColorFilter(new float[] {-1, 0, 0, 0, 255, 0, -1, 0, 0, 255, 0, 0, -1, 0, 255, 0, 0, 0, 0.8f, 0});
	static final Paint inv80p = new Paint(0x07);
	static final ColorFilter d80 = new ColorMatrixColorFilter(new float[] {1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0.6f, 0});
	static final Paint d80p = new Paint(0x07);
	private static boolean do_invert = Build.VERSION.SDK_INT > 10;
	static final Paint p80 = do_invert ? inv80p : d80p;

	static {
		inv80p.setColorFilter(inv80);
		d80p.setColorFilter(d80);
	}
	
	static public Bitmap list_bmp(Context ctx, int extra) {
		Resources r = ctx.getResources();
		int size = r.getDimensionPixelSize(R.dimen.notification_height) - 2
				* r.getDimensionPixelSize(R.dimen.notification_glyph_margin);
		Bitmap t = BitmapFactory.decodeResource(r, R.drawable.list);
		Bitmap b = Bitmap.createBitmap(size, size, Config.ARGB_8888);
		Canvas c = new Canvas(b);

		Drawable d = ctx.getResources().getDrawable(R.drawable.glyph_border);
		d.setBounds(0, 0, size, size);
		d.draw(c);
		c.drawBitmap(t, 0, 0, p80);
		if (extra > 0) {
			Paint p = new Paint(0x07);
			p.setTextSize(size / 5);
			p.setColor(Color.WHITE);
			p.setTypeface(Typeface.DEFAULT_BOLD);
			String draw = String.format("+%d", extra);
			Rect bounds = new Rect();
			p.getTextBounds(draw, 0, draw.length(), bounds);
			RectF border = new RectF(bounds);
			border.inset(-2, -2);
			border.offsetTo(size * 0.9f - bounds.width() - 2, size * 0.9f - bounds.height() - 2);
			p.setColor(Color.argb(200, 255, 136, 0));
			p.setStyle(Style.FILL);
			c.drawRoundRect(border, size/20, size/20, p);
			p.setColor(Color.argb(255, 255, 136, 0));
			p.setStyle(Style.STROKE);
			c.drawRoundRect(border, size/20, size/20, p);
			p.setColor(Color.WHITE);
			c.drawText(draw, size * 0.9f - bounds.width(), size * 0.9f, p);
		}
		return b;
	}

	static public Bitmap add_new_bmp(Context ctx) {
		Resources r = ctx.getResources();
		int size = r.getDimensionPixelSize(R.dimen.notification_height) - 2
				* r.getDimensionPixelSize(R.dimen.notification_glyph_margin);
		Bitmap b = Bitmap.createBitmap(size, size, Config.ARGB_8888);
		Canvas c = new Canvas(b);
		Paint p = new Paint(p80);
		p.setTextSize(size);
		p.setColor(Color.BLACK);
		Rect bounds = new Rect();
		p.getTextBounds("+", 0, 1, bounds);
		Drawable d = ctx.getResources().getDrawable(R.drawable.glyph_border);
		d.setBounds(0, 0, size, size);
		d.draw(c);
		c.drawText("+", size / 2 - bounds.centerX(), size / 2 - bounds.centerY(), p);
		return b;
	}

	static public Bitmap memo_bmp(Context ctx, ReminderItem item) {
		Resources r = ctx.getResources();
		int size = r.getDimensionPixelSize(R.dimen.notification_height) - 2
				* r.getDimensionPixelSize(R.dimen.notification_glyph_margin);
		Bitmap glyph = BitmapFactory.decodeByteArray(item.glyph_data, 0,
				item.glyph_data.length);
		Bitmap result = Bitmap.createScaledBitmap(glyph, size, size, false);

		Bitmap b = Bitmap.createBitmap(size, size, Config.ARGB_8888);
		Canvas c = new Canvas(b);
		c.drawBitmap(result, 0, 0, p80);
		return b;
	}
}
