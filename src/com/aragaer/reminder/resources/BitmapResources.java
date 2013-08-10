package com.aragaer.reminder.resources;

import com.aragaer.reminder.R;
import com.aragaer.reminder.ReminderItem;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;

public class BitmapResources {
	private final Resources r;

	BitmapResources(final Context context) {
		r = context.getResources();
	}

	static public Bitmap memo_bmp(ReminderItem item, int required_size) {
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
		new Canvas(b).drawBitmap(result, offset, offset,
				ColorResources.paints[item.color]);
		result.recycle();
		return b;
	}

	private Bitmap res2bmp(final int resource, final int required_size) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(r, resource, options);

		if (options.outHeight > required_size)
			options.inSampleSize = Math.round(1f * options.outHeight
					/ required_size + 0.5f);
		else
			options.inSampleSize = 1;
		options.inJustDecodeBounds = false;
		Bitmap result = BitmapFactory.decodeResource(r, resource, options);
		float offset = (required_size - result.getHeight()) * 0.5f;

		Bitmap b = Bitmap.createBitmap(required_size, required_size,
				Config.ARGB_8888);
		new Canvas(b).drawBitmap(result, offset, offset, ColorResources.paints[0]);
		result.recycle();
		return b;
	}

	public Bitmap add_layer(final Bitmap bitmap, final Drawable layer) {
		layer.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
		layer.draw(new Canvas(bitmap));
		return bitmap;
	}

	public Bitmap add_layer(final Bitmap bitmap, final Bitmap layer) {
		return add_layer(bitmap, layer, 0, 0);
	}

	public Bitmap add_layer(final Bitmap bitmap, final Bitmap layer, final int ox, final int oy) {
		new Canvas(bitmap).drawBitmap(layer, ox, oy, null);
		return bitmap;
	}

	static public Bitmap draw_char(final String str, final int size) {
		final Bitmap b = Bitmap.createBitmap(size, size, Config.RGB_565);
		final Canvas c = new Canvas(b);
		final Paint p = new Paint(0x07);
		p.setTextSize(size);
		p.setColor(Color.WHITE);
		final Rect bounds = new Rect();
		p.getTextBounds(str, 0, str.length(), bounds);
		c.drawText(str, size / 2 - bounds.centerX(),
				size / 2 - bounds.centerY(), p);
		return b;
	}

	private final static int ORANGE = Color.argb(255, 255, 136, 0);
	private final static int ORANGE2 = Color.argb(192, 255, 136, 0);
	private final static int extra_inset = 2;
	public static Bitmap extra_bmp(final int extra, final int text_size, final int round) {
		final Paint p = new Paint(0x07);
		p.setTextSize(text_size);
		p.setTypeface(Typeface.DEFAULT_BOLD);
		final String draw = "+" + Integer.toString(extra);
		final Rect bounds = new Rect();
		p.getTextBounds(draw, 0, draw.length(), bounds);
		final Bitmap result = Bitmap.createBitmap(
				bounds.width() + extra_inset * 2,
				bounds.height() + extra_inset * 2,
				Config.ARGB_8888);
		final Canvas c = new Canvas(result);
		RectF border = new RectF(bounds);
		border.inset(-2, -2);
		border.offsetTo(0, 0);
		p.setColor(ORANGE2);
		p.setStyle(Style.FILL);
		c.drawRoundRect(border, round, round, p);
		p.setColor(ORANGE);
		p.setStyle(Style.STROKE);
		c.drawRoundRect(border, round, round, p);
		p.setColor(Color.WHITE);
		c.drawText(draw, 2, bounds.height() + 2, p);
		return result;
	}

	public Bitmap list_bmp(final int required_size) {
		return res2bmp(R.drawable.list, required_size);
	}

	public Bitmap add_new_bmp(final int required_size) {
		final Bitmap b = Bitmap.createBitmap(required_size, required_size, Config.ARGB_8888);
		final Bitmap plus = draw_char("+", required_size);
		new Canvas(b).drawBitmap(plus, 0, 0, ColorResources.paints[ColorResources.COLOR_WHITE]);
		plus.recycle();
		return b;
	}
}
