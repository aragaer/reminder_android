package com.aragaer.reminder;

import com.aragaer.simpleactionbar.AbActivity;
import com.aragaer.simpleactionbar.ActionBar;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class ReminderCreateActivity extends AbActivity {
	DrawView dv;
	ColorSwitch cs;

	@SuppressLint("NewApi")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LinearLayout ll = new LinearLayout(this);
		ll.setGravity(Gravity.CENTER);

		cs = new ColorSwitch(this);
		cs.setId(3);

		dv = new DrawView(this);
		dv.setPaint(Bitmaps.paints[cs.getValue()]);
		dv.setId(1);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			ll.setOrientation(LinearLayout.VERTICAL);
			cs.setOrientation(LinearLayout.HORIZONTAL);
		} else {
			ll.setOrientation(LinearLayout.HORIZONTAL);
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

		ll.addView(cs);
		ll.addView(dv);
		setContentView(ll);

		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setTitle(R.string.app_name);
	}

	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable("image", dv.getBitmap());
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case com.aragaer.simpleactionbar.R.id.home:
			finish();
			break;
		case R.string.no_extra:
			save(false);
			break;
		case R.string.add_extra:
			save(true);
			break;
		default:
			break;
		}
		return true;
	}

	public boolean onCreateActionBarMenu(Menu menu) {
		menu.add(R.string.no_extra).setIcon(R.drawable.ic_cab_done_holo_dark);
		menu.add(R.string.add_extra).setIcon(R.drawable.navigation_forward);
		return true;
	}

	void save(boolean extra) {
		Bitmap res = dv.getBitmap();
		dv.reset();

		ContentValues row = new ContentValues();
		row.put("glyph", ReminderItem.bitmap_to_bytes(res));
		row.put("date", System.currentTimeMillis());
		row.put("color", cs.getValue());
		Uri result_uri = getContentResolver().insert(
				ReminderProvider.content_uri, row);
		if (extra)
			startActivity(new Intent(ReminderCreateActivity.this,
					ReminderViewActivity.class).putExtra("reminder_id",
					ContentUris.parseId(result_uri)));
		finish();
	}
}
