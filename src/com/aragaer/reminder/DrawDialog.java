package com.aragaer.reminder;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;

public class DrawDialog extends Dialog {
	public DrawDialog(Context context) {
		super(context);
		setTitle(R.string.add_new);
		setContentView(R.layout.drawing);
		final Button btn = (Button) findViewById(R.id.btn);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dismiss();
			}
		});
	}

	public void onWindowFocusChanged(boolean hasFocus) {
	    if (hasFocus)
	        ((DrawView) findViewById(R.id.draw)).reset();
	    super.onWindowFocusChanged(hasFocus);
	}
}
