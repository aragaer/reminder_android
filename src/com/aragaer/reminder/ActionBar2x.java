package com.aragaer.reminder;

import android.content.Context;
import android.view.ViewGroup;

import com.markupartist.android.widget.ActionBar;

public class ActionBar2x extends ActionBarCompat {
	ActionBar ab;
	public ActionBar2x(ViewGroup parent) {
		final Context ctx = parent.getContext();
		ab = new ActionBar(ctx, null);
		parent.addView(ab);
		ab.setTitle(R.string.title_activity_main);
		ab.setDisplayUseLogoEnabled(true);
		ab.setHomeLogo(R.drawable.ic_launcher);
	}

	public void setHomeLogo(int resId) {
		ab.setHomeLogo(resId);
	}
}
