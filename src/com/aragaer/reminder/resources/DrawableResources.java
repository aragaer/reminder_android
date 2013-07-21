package com.aragaer.reminder.resources;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

public class DrawableResources {
	private final Context c;

	final static GradientDrawable border(final int stroke, final int color) {
		GradientDrawable result = new GradientDrawable();
		result.setCornerRadius(7);
		result.setSize(60, 60);
		result.setColor(Color.TRANSPARENT);
		result.setStroke(stroke, color);
		return result;
	}


	DrawableResources(final Context context) {
		c = context;
	}
}
