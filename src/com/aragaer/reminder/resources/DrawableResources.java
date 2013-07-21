package com.aragaer.reminder.resources;

import java.io.ByteArrayInputStream;

import com.aragaer.reminder.ReminderItem;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;

public class DrawableResources {
	private final Resources r;
	private final ColorResources cr;
	private static final int STD_SIZE = 60;

	private static final Path ribbon_path = new Path();

	static {
		ribbon_path.moveTo(15, 0);
		ribbon_path.lineTo(22, 0);
		ribbon_path.lineTo(0, 22);
		ribbon_path.lineTo(0, 15);
		ribbon_path.close();
	}

	public static GradientDrawable border(final int stroke, final int color) {
		return border(stroke, color, Color.TRANSPARENT);
	}

	public static GradientDrawable border(final int stroke, final int color, final int fill) {
		GradientDrawable result = new GradientDrawable();
		result.setCornerRadius(7);
		result.setSize(STD_SIZE, STD_SIZE);
		result.setColor(fill);
		result.setStroke(stroke, color);
		return result;
	}

	public static ShapeDrawable ribbon(final int color) {
		PathShape path_shape = new PathShape(ribbon_path, STD_SIZE, STD_SIZE);
		ShapeDrawable result = new ShapeDrawable(path_shape);
		final Paint paint = result.getPaint();
		paint.setStyle(Style.FILL);
		paint.setColor(color);
		return result;
	}

	public static LayerDrawable inset(final Drawable drawable, final int inset) {
		final LayerDrawable result = drawable instanceof LayerDrawable
			? (LayerDrawable) drawable
			: new LayerDrawable(new Drawable[] { drawable });

		int i = result.getNumberOfLayers();
		while (i-- > 0)
			result.setLayerInset(i, inset, inset, inset, inset);

		return result;
	}

	public BitmapDrawable memo_drawable(final ReminderItem item) {
		final BitmapDrawable result = new BitmapDrawable(r, new ByteArrayInputStream(item.glyph_data));
		result.getPaint().set(cr.paints[item.color]);
		return result;
	}

	DrawableResources(final Context context) {
		r = context.getResources();
		cr = RuntimeResources.get(context).getInstance(ColorResources.class);
	}
}
