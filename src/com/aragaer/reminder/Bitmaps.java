package com.aragaer.reminder;

import java.io.ByteArrayInputStream;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;

public class Bitmaps {
	public static final int COLOR_WHITE = 0;
	public static final int COLOR_BLUE = 1;
	public static final int COLOR_PURPLE = 2;
	public static final int COLOR_GREEN = 3;
	public static final int COLOR_YELLOW = 4;
	public static final int COLOR_RED = 5;

	public static final int N_COLORS = 6;

	static final int colors[] = { Color.WHITE,
			Color.argb(0xff, 0x33, 0xb5, 0xe5),
			Color.argb(0xff, 0xaa, 0x66, 0xcc),
			Color.argb(0xff, 0x99, 0xcc, 0x00),
			Color.argb(0xff, 0xff, 0xbb, 0x33),
			Color.argb(0xff, 0xff, 0x44, 0x44), };
	static final float filters[][] = {
			{ 0, 0, 0, 0, 0xFF, 0, 0, 0, 0, 0xFF, 0, 0, 0, 0, 0xFF, .8f, 0, 0, 0, 0x00 },
			{ 0, 0, 0, 0, 0x33, 0, 0, 0, 0, 0xB5, 0, 0, 0, 0, 0xE5, 1, 0, 0, 0, 0x00 },
			{ 0, 0, 0, 0, 0xAA, 0, 0, 0, 0, 0x66, 0, 0, 0, 0, 0xCC, 1, 0, 0, 0, 0x00 },
			{ 0, 0, 0, 0, 0x99, 0, 0, 0, 0, 0xCC, 0, 0, 0, 0, 0x00, 1, 0, 0, 0, 0x00 },
			{ 0, 0, 0, 0, 0xFF, 0, 0, 0, 0, 0xBB, 0, 0, 0, 0, 0x33, 1, 0, 0, 0, 0x00 },
			{ 0, 0, 0, 0, 0xFF, 0, 0, 0, 0, 0x44, 0, 0, 0, 0, 0x44, 1, 0, 0, 0, 0x00 }, };
	static final Paint paints[] = new Paint[N_COLORS];
	static {
		for (int i = 0; i < N_COLORS; i++) {
			paints[i] = new Paint(0x07);
			paints[i].setColorFilter(filter(i));
		}
	}

	static ColorMatrixColorFilter filter(int color_num) {
		return new ColorMatrixColorFilter(filters[color_num]);
	}

	static public Bitmap draw_char(String str, int size) {
		Bitmap b = Bitmap.createBitmap(size, size, Config.RGB_565);
		Canvas c = new Canvas(b);
		Paint p = new Paint(0x07);
		p.setTextSize(size);
		p.setColor(Color.WHITE);
		Rect bounds = new Rect();
		p.getTextBounds(str, 0, str.length(), bounds);
		c.drawText(str, size / 2 - bounds.centerX(),
				size / 2 - bounds.centerY(), p);
		return b;
	}

	static public Bitmap memo_bmp(Context ctx, ReminderItem item,
			int required_size) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(item.glyph_data, 0,
				item.glyph_data.length, options);

		options.inSampleSize = Math.round(1f * options.outHeight
				/ required_size + 0.5f);
		options.inJustDecodeBounds = false;
		Bitmap result = BitmapFactory.decodeByteArray(item.glyph_data, 0,
				item.glyph_data.length, options);
		float offset = (required_size - result.getHeight()) * 0.5f;

		Bitmap b = Bitmap.createBitmap(required_size, required_size,
				Config.ARGB_8888);
		new Canvas(b).drawBitmap(result, offset, offset, paints[item.color]);
		result.recycle();
		return b;
	}

	static public BitmapDrawable memo_drawable(Resources r, ReminderItem item, boolean invert) {
		BitmapDrawable result = new BitmapDrawable(r, new ByteArrayInputStream(item.glyph_data));
		result.setColorFilter(filter(item.color));
		return result;
	}

	static GradientDrawable border(int stroke, int color) {
		GradientDrawable result = new GradientDrawable();
		result.setCornerRadius(7);
		result.setSize(60, 60);
		result.setColor(Color.TRANSPARENT);
		result.setStroke(stroke, color);
		return result;
	}
}
