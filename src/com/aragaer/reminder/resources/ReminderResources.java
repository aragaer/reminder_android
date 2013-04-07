package com.aragaer.reminder.resources;


import android.content.res.Resources;

public final class ReminderResources extends RuntimeResources {
	private final NotificationResources nr;
	protected ReminderResources(final Resources base) {
		super(base);
		nr = NotificationResources.getInstance(base);
	}

	public final int getNumGlyphs() {
		return nr.getNumGlyphs();
	}

	public static final ReminderResources getInstance(Resources r) {
		return getInstance(ReminderResources.class, r);
	}
}
