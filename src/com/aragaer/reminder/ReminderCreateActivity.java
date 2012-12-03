package com.aragaer.reminder;

import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class ReminderCreateActivity extends Activity {
	DrawView dv;
	ColorSwitch cs;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		ActionBar ab = new ActionBar(this, null);
		ab.setDisplayHomeAsUpEnabled(true);

		ll.addView(ab);

		LinearLayout inner = new LinearLayout(this);
		inner.setGravity(Gravity.CENTER);

		cs = new ColorSwitch(this);
		cs.setId(3);

		dv = new DrawView(this);
		dv.setPaint(Bitmaps.paints[cs.getValue()]);
		dv.setId(1);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			inner.setOrientation(LinearLayout.VERTICAL);
			cs.setOrientation(LinearLayout.HORIZONTAL);
		} else {
			inner.setOrientation(LinearLayout.HORIZONTAL);
			cs.setOrientation(LinearLayout.VERTICAL);
		}

		if (savedInstanceState == null)
			dv.reset();
		else
			dv.setBitmap((Bitmap) savedInstanceState.getParcelable("image"));

		cs.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				dv.setPaint(Bitmaps.paints[cs.getValue()]);
			}
		});

		inner.addView(cs);
		inner.addView(dv);
		ll.addView(inner);
		setContentView(ll);

		ab.setHomeAction(new ActionBar.Action() {
			public int getDrawable() {
				return 0;
			}
			public void performAction(View view) {
				finish();
			}
		});

		ab.addAction(new ActionBar.AbstractAction(R.drawable.ic_cab_done_holo_dark) {
			public void performAction(View view) {
				save(false);
			}
		});
		ab.addAction(new ActionBar.AbstractAction(R.drawable.navigation_forward) {
			public void performAction(View view) {
				save(true);
			}
		});
	}

	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable("image", dv.getBitmap());
	}

	void save(boolean extra) {
		Bitmap res = dv.getBitmap();
		dv.reset();

		ContentValues row = new ContentValues();
		row.put("glyph", ReminderItem.bitmap_to_bytes(res));
		row.put("date", System.currentTimeMillis());
		row.put("color", cs.getValue());
		Uri result_uri = getContentResolver().insert(ReminderProvider.content_uri, row);
		if (extra)
			startActivity(new Intent(ReminderCreateActivity.this, ReminderViewActivity.class)
				.putExtra("reminder_id", ContentUris.parseId(result_uri)));
		finish();
	}
}
