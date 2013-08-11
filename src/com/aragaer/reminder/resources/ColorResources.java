package com.aragaer.reminder.resources;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

public class ColorResources {
	public static final int COLOR_WHITE = 0;
	public static final int COLOR_BLUE = 1;
	public static final int COLOR_PURPLE = 2;
	public static final int COLOR_GREEN = 3;
	public static final int COLOR_YELLOW = 4;
	public static final int COLOR_RED = 5;

	public static final int N_COLORS = 6;

	public enum COLOR {
		WHITE, BLUE, PURPLE, GREEN, YELLOW, RED;

		public int getColor() {
			return colors[ordinal()];
		}

		public Paint getPaint() {
			return paints[ordinal()];
		}
	}

	public static final int colors[] = {
		Color.WHITE,
		Color.argb(0xff, 0x33, 0xb5, 0xe5),
		Color.argb(0xff, 0xaa, 0x66, 0xcc),
		Color.argb(0xff, 0x99, 0xcc, 0x00),
		Color.argb(0xff, 0xff, 0xbb, 0x33),
		Color.argb(0xff, 0xff, 0x44, 0x44),
	};
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

	private static ColorMatrixColorFilter filter(final int color_num) {
		return new ColorMatrixColorFilter(filters[color_num]);
	}

	public static final int alpha50(final int color) {
		return color & ~0x80000000;
	}

	ColorResources(final Context context) { }
}
