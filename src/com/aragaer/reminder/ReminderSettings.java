package com.aragaer.reminder;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ReminderSettings extends PreferenceActivity {
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.preferences);
	}
}
