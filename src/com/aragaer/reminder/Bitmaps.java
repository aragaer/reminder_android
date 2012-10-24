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

public class Bitmaps {
	static final ColorFilter wb2wt = new ColorMatrixColorFilter(new float[] {1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0.8f, 0, 0, 0, 0});
	static final Paint wb2wtp = new Paint(0x07);
	static final ColorFilter wb2bt = new ColorMatrixColorFilter(new float[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.6f, 0, 0, 0, 0});
	static final Paint wb2btp = new Paint(0x07);

	static {
		wb2wtp.setColorFilter(wb2wt);
		wb2btp.setColorFilter(wb2bt);
	}
	
	static public Bitmap list_bmp(Context ctx, int extra, boolean invert) {
		Resources r = ctx.getResources();
		int size = r.getDimensionPixelSize(R.dimen.notification_height) - 2
				* r.getDimensionPixelSize(R.dimen.notification_glyph_margin);
		Bitmap t = BitmapFactory.decodeResource(r, R.drawable.list);
		Bitmap b = Bitmap.createBitmap(size, size, Config.ARGB_8888);
		Canvas c = new Canvas(b);

		Drawable d = ctx.getResources().getDrawable(R.drawable.glyph_border);
		d.setBounds(0, 0, size, size);
		d.draw(c);
		c.drawBitmap(t, 0, 0, invert ? wb2btp : wb2wtp);
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

	static public Bitmap draw_char(String str, int size) {
		Bitmap b = Bitmap.createBitmap(size, size, Config.RGB_565);
		Canvas c = new Canvas(b);
		Paint p = new Paint(0x07);
		p.setTextSize(size);
		p.setColor(Color.WHITE);
		Rect bounds = new Rect();
		p.getTextBounds(str, 0, str.length(), bounds);
		c.drawText(str, size / 2 - bounds.centerX(), size / 2 - bounds.centerY(), p);
		return b;
	}

	static public Bitmap add_new_bmp(Context ctx, boolean invert) {
		Resources r = ctx.getResources();
		int size = r.getDimensionPixelSize(R.dimen.notification_height) - 2
				* r.getDimensionPixelSize(R.dimen.notification_glyph_margin);
		Bitmap b = Bitmap.createBitmap(size, size, Config.ARGB_8888);
		Canvas c = new Canvas(b);
		Drawable d = ctx.getResources().getDrawable(R.drawable.glyph_border);
		d.setBounds(0, 0, size, size);
		d.draw(c);
		c.drawBitmap(draw_char("+", size), 0, 0, invert ? wb2btp : wb2wtp);
		return b;
	}

	static public Bitmap memo_bmp(Context ctx, ReminderItem item, boolean invert) {
		Resources r = ctx.getResources();
		int size = r.getDimensionPixelSize(R.dimen.notification_height) - 2
				* r.getDimensionPixelSize(R.dimen.notification_glyph_margin);
		Bitmap glyph = BitmapFactory.decodeByteArray(item.glyph_data, 0,
				item.glyph_data.length);
		Bitmap result = Bitmap.createScaledBitmap(glyph, size, size, false);

		Bitmap b = Bitmap.createBitmap(size, size, Config.ARGB_8888);
		Canvas c = new Canvas(b);
		c.drawBitmap(result, 0, 0, invert ? wb2btp : wb2wtp);
		return b;
	}
}
