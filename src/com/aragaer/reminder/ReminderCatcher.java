package com.aragaer.reminder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ReminderCatcher extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int glyph_width = getResources().getDimensionPixelSize(R.dimen.notification_height);
		int position = (int) ReminderService.x / glyph_width;
		Intent i;
		if (ReminderService.list == null
				|| position >= ReminderService.list.size()) {
			i = new Intent(this, ReminderListActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		} else
			i = ReminderService.list.get(position).intent;
		startActivity(i);
		finish();
	}
}
