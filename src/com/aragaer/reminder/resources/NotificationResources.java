package com.aragaer.reminder.resources;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

import com.aragaer.reminder.R;

public final class NotificationResources {
	private final Context c;
	private final Resources r;
	private final BitmapResources br;
	private final int notification_height, symbol_margin, symbol_size, border_stroke;

	public final int yellow, red;

	NotificationResources(final Context context) {
		c = context;
		r = c.getResources();
		br = RuntimeResources.get(c).getInstance(BitmapResources.class);
		notification_height = r.getDimensionPixelSize(R.dimen.notification_height);
		symbol_margin = r.getDimensionPixelSize(R.dimen.notification_glyph_margin);
		symbol_size = notification_height - 2 * symbol_margin;
		border_stroke = r.getDimensionPixelSize(R.dimen.border_width);

		final int w = r.getDisplayMetrics().widthPixels, h = r.getDisplayMetrics().heightPixels;
		if (w > h) {
			red = n_glyphs(w, notification_height);
			yellow = n_glyphs(h, notification_height);
		} else {
			red = n_glyphs(h, notification_height);
			yellow = n_glyphs(w, notification_height);
		}
	}

	private final static int n_glyphs(final int display_size, final int glyph_size) {
		int num = display_size / glyph_size;
		if (num > 7) // Hardcoded value, yo!
			num = 7;
		return num - 2;
	}

	public final int getNumGlyphs() {
		return n_glyphs(r.getDisplayMetrics().widthPixels, notification_height);
	}

	private Bitmap list_bmp, list_bmp2, new_bmp;

	protected final Bitmap list() {
		if (list_bmp == null) {
			list_bmp = br.list_bmp(symbol_size);
			list_bmp = br.add_layer(list_bmp, DrawableResources.border(
					border_stroke,
					ColorResources.colors[ColorResources.COLOR_BLUE]));
		}
		return list_bmp;
	}

	private int prev_extra = -1;
	private final Bitmap extras[] = new Bitmap[5];
	public final Bitmap list(final int extra) {
		if (prev_extra == extra && list_bmp2 != null)
			return list_bmp2;

		if (list_bmp2 != null)
			list_bmp2.recycle();
		list_bmp2 = list().copy(Config.ARGB_8888, true);
		if (extra > 0) {
			if (extras[extra] == null)
				extras[extra] = BitmapResources.extra_bmp(extra, symbol_size / 5, symbol_size / 20);
			final int x_offset = Math.round(symbol_size * 0.9f) - extras[extra].getWidth();
			final int y_offset = Math.round(symbol_size * 0.9f) - extras[extra].getHeight();
			list_bmp2 = br.add_layer(list_bmp2, extras[extra], x_offset, y_offset);
		}
		prev_extra = extra;
		return list_bmp2;
	}

	public final Bitmap add_new() {
		if (new_bmp == null) {
			new_bmp = br.add_new_bmp(symbol_size);
			new_bmp = br.add_layer(new_bmp, DrawableResources.border(
					border_stroke,
					ColorResources.colors[ColorResources.COLOR_BLUE]));
		}
		return new_bmp;
	}
}
