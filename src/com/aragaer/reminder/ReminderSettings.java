package com.aragaer.reminder;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

public class ReminderSettings extends PreferenceActivity {
	private static final String prefs_to_broadcast[] = { "notification_invert",
			"notification_btn_left", "notification_hide_list" };

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.preferences);
	    for (String s : prefs_to_broadcast)
		    findPreference(s).setOnPreferenceChangeListener(pref_broadcast);
	}
	OnPreferenceChangeListener pref_broadcast = new OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			sendBroadcast(new Intent(ReminderService.settings_changed));
			return true;
		}
	};
}
