package com.aragaer.reminder.resources;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

public class DrawableResources extends RuntimeResources {
	final static GradientDrawable border(final int stroke, final int color) {
		GradientDrawable result = new GradientDrawable();
		result.setCornerRadius(7);
		result.setSize(60, 60);
		result.setColor(Color.TRANSPARENT);
		result.setStroke(stroke, color);
		return result;
	}

	protected DrawableResources(final Resources base) {
		super(base);
	}

	public static final DrawableResources getInstance(final Resources r) {
		return getInstance(DrawableResources.class, r);
	}
}
