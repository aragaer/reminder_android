package com.aragaer.reminder.resources;


import android.content.res.Resources;

public final class ReminderResources extends RuntimeResources {
	protected ReminderResources(final Resources base) {
		super(base);
	}

	public static final ReminderResources getInstance(final Resources r) {
		return getInstance(ReminderResources.class, r);
	}
}
