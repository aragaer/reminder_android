package com.aragaer.reminder;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class ColorSwitch extends RadioGroup {
	private static final int ADD = 100;
	private int margin;
	public ColorSwitch(Context context) {
		this(context, null);
	}
	public ColorSwitch(Context context, AttributeSet attrs) {
		super(context, attrs);
		margin = context.getResources().getDimensionPixelSize(R.dimen.notification_glyph_margin);

		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1);
		lp.setMargins(margin, margin, margin, margin);
		for (int i = 0; i < Bitmaps.N_COLORS; i++) {
			final int c = Bitmaps.colors[i];
			RadioButton rb = new RadioButton(context);
			GradientDrawable border = Bitmaps.border(margin, c);
			rb.setId(ADD + i);
			border.setColor(Color.argb(192, Color.red(c), Color.green(c), Color.blue(c)));
			rb.setBackgroundDrawable(border);
			rb.setButtonDrawable(android.R.color.transparent);
			addView(rb, lp);
		}
		check(ADD);
	}
	public void onMeasure(int wms, int hms) {
		int w = MeasureSpec.getSize(wms);
		int h = MeasureSpec.getSize(hms);
		final boolean horizontal = getOrientation() == LinearLayout.HORIZONTAL;

		if (horizontal)
			w /= getChildCount();
		else
			h /= getChildCount();

		int s = Math.min(w, h);

		int large = s * getChildCount();

		if (horizontal)
			setMeasuredDimension(large, s);
		else
			setMeasuredDimension(s, large);

		s -= 2 * margin;
		wms = MeasureSpec.makeMeasureSpec(s, MeasureSpec.EXACTLY);

		for (int i = 0; i < getChildCount(); i++)
			getChildAt(i).measure(wms, wms);
	}

	public int getValue() {
		return getCheckedRadioButtonId() - ADD;
	}
}
