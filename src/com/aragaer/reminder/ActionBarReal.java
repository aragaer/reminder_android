package com.aragaer.reminder;

import android.annotation.SuppressLint;
import android.app.ActionBar;

@SuppressLint("NewApi")
public class ActionBarReal extends ActionBarCompat {
	ActionBar ab;

	public ActionBarReal(ActionBar _ab) {
		ab = _ab;
	}

	public void setHomeLogo(int resId) {
	}
}
