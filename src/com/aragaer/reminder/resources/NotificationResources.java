package com.aragaer.reminder.resources;

import android.content.res.Resources;

import com.aragaer.reminder.R;

final class NotificationResources extends RuntimeResources {
	private final int notification_height;
	protected NotificationResources(final Resources base) {
		super(base);
		notification_height = r.getDimensionPixelSize(R.dimen.notification_height);
	}

	public static int n_glyphs(int display_size, int glyph_size) {
		int num = display_size / glyph_size;
		if (num > 7) // Hardcoded value, yo!
			num = 7;
		return num - 2;
	}

	public final int getNumGlyphs() {
		return n_glyphs(r.getDisplayMetrics().widthPixels, notification_height);
	}

	public static final NotificationResources getInstance(Resources r) {
		return getInstance(NotificationResources.class, r);
	}
}
