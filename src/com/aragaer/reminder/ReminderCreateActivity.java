package com.aragaer.reminder;

import android.app.Activity;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout.LayoutParams;

public class ReminderCreateActivity extends Activity {
	DrawView dv;
	CheckBox chk;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		DrawDialogLayout layout = new DrawDialogLayout(this);
		LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		dv = new DrawView(this);
		dv.setId(1);

		chk = new CheckBox(this);
		chk.setText(R.string.add_extra);
		chk.setId(2);

		Button btn = new Button(this);
		btn.setText(android.R.string.ok);
		btn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Bitmap res = dv.getBitmap();
                dv.reset();

                ReminderItem item = new ReminderItem(res);
                ContentValues row = new ContentValues();
                row.put("glyph", item.glyph_data);
                row.put("comment", item.text);
                row.put("date", item.when.getTime());
                getContentResolver().insert(ReminderProvider.content_uri, row);
                ReminderCreateActivity.this.finish();
            }
        });
		btn.setId(3);

		if (savedInstanceState == null)
			dv.reset();
		else
			dv.setBitmap((Bitmap) savedInstanceState.getParcelable("image"));
		
		layout.addView(dv, lp);
		layout.addView(chk, lp);
		layout.addView(btn, lp);

		setContentView(layout, lp);
	}

	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable("image", dv.getBitmap());
	}
}
