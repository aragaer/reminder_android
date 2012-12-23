package com.aragaer.reminder;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class ReminderProvider extends ContentProvider {
	private static final int DATABASE_VERSION = 1;
	private SQLiteDatabase db = null;

	public static final Uri content_uri = Uri
			.parse("content://com.aragaer.reminder.provider/reminder");

	private static final UriMatcher uri_matcher = new UriMatcher(0);
	private static final int REMINDER_CODE = 1;
	private static final int REMINDER_WITH_ID = 2;

	private static final String TAG = ReminderProvider.class.getSimpleName();

	static {
		uri_matcher.addURI("com.aragaer.reminder.provider", "reminder",	REMINDER_CODE);
		uri_matcher.addURI("com.aragaer.reminder.provider", "reminder/#", REMINDER_WITH_ID);
	}

	public int delete(Uri uri, String arg1, String[] arg2) {
		int result = 0;
		switch (uri_matcher.match(uri)) {
		case REMINDER_CODE:
			result = db.delete("memo", arg1 == null ? "1" : arg1, arg2);
			break;
		case REMINDER_WITH_ID:
			result = db.delete("memo", "_id=?", uri2selection(uri));
			break;
		default:
			Log.e(TAG, "Unknown URI requested: " + uri);
			break;
		}
		if (result > 0) {
			Toast.makeText(getContext(), R.string.toast_deleted, Toast.LENGTH_LONG).show();
			getContext().getContentResolver().notifyChange(content_uri, null);
		}
		return result;
	}

	public String getType(Uri arg0) {
		return null;
	}

	public Uri insert(Uri uri, ContentValues arg1) {
		switch (uri_matcher.match(uri)) {
		case REMINDER_CODE:
			long id = db.insert("memo", null, arg1);
			if (id != -1) {
				Toast.makeText(getContext(), R.string.toast_created, Toast.LENGTH_LONG).show();
				getContext().getContentResolver().notifyChange(content_uri, null);
			}
			return ContentUris.withAppendedId(content_uri, id);
		default:
			Log.e(TAG, "Unknown URI requested: " + uri);
			break;
		}
		return null;
	}

	public boolean onCreate() {
		db = new ReminderSQLHelper(getContext(), "MEMO", null, DATABASE_VERSION).getWritableDatabase();
		if (db == null || db.isReadOnly())
			return false;

		Log.d(TAG, "Created. Checking if we need to move old data");

		SharedPreferences prefs = getContext().getSharedPreferences("DB", Context.MODE_PRIVATE);
		int current_version = prefs.getInt("DATABASE_VERSION", 0);
		if (current_version > 0 && moveOldData(db))
			prefs.edit().putInt("DATABASE_VERSION", 0).commit();

		return true;
	}

	boolean moveOldData(SQLiteDatabase db) {
		Log.d(TAG, "Converting old database");
		File sdcard = Environment.getExternalStorageDirectory();
		File dir = new File(sdcard, "Android");
		dir = new File(new File(dir, "data"), ReminderProvider.class
				.getPackage().getName());
		if (!dir.exists())
			return true;

		File db_file = new File(dir, "memo.db");

		if (db_file.exists()) {
			try {
				db.execSQL("attach ? as sd", new String[] {db_file.getAbsolutePath()} );
			} catch (SQLiteException e) {
				Log.e(TAG, "Failed to attach old DB: "+e);
				return false;
			}

			try {
				db.beginTransaction();
				db.execSQL("insert into memo select * from sd.memo");
				db.delete("sd.memo", null, null);
				db.setTransactionSuccessful();
			} catch (SQLiteException e) {
				Log.e(TAG, "Failed to move old DB: "+e);
				if (db.inTransaction())
					db.endTransaction();
				return false;
			} finally {
				db.endTransaction();
			}

			Log.d(TAG, "Data moved");

			try {
				db.execSQL("detach sd");
			} catch (SQLiteException e) {
				Log.w(TAG, "Failed to detach database");
				return false;
			}

			db_file.delete();
		}

		dir.delete();

		return true;
	}

	public Cursor query(Uri uri, String[] arg1, String arg2, String[] arg3,
			String arg4) {
		switch (uri_matcher.match(uri)) {
		case REMINDER_CODE:
			return db.query("memo", arg1, arg2, arg3, null, null, arg4);
		case REMINDER_WITH_ID:
			return db.query("memo", arg1, "_id=?", uri2selection(uri), null, null, arg4);
		default:
			Log.e(TAG, "Unknown URI requested: " + uri);
			break;
		}
		return null;
	}

	String[] uri2selection(Uri uri) {
		return new String[] { Long.toString(ContentUris.parseId(uri)) };
	}

	public int update(Uri uri, ContentValues arg1, String arg2, String[] arg3) {
		int result = 0;
		switch (uri_matcher.match(uri)) {
		case REMINDER_CODE:
			result = db.update("memo", arg1, arg2, arg3);
			break;
		case REMINDER_WITH_ID:
			result = db.update("memo", arg1, "_id=?", uri2selection(uri));
			break;
		default:
			Log.e(TAG, "Unknown URI requested: " + uri);
			break;
		}
		if (result > 0) {
			Toast.makeText(getContext(), R.string.toast_saved, Toast.LENGTH_LONG).show();
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return result;
	}

	public static ReminderItem getItem(Cursor c, ReminderItem reuse) {
		if (reuse == null)
			reuse = new ReminderItem(c.getLong(0), c.getBlob(1), c.getString(2),
				new Date(c.getLong(3)), c.getInt(4));
		else
			reuse.setTo(c.getLong(0), c.getBlob(1), c.getString(2),
				new Date(c.getLong(3)), c.getInt(4));
		return reuse;
	}

	public static ReminderItem getItem(Cursor c) {
		return getItem(c, null);
	}

	public static List<ReminderItem> getAll(Cursor c) {
		ArrayList<ReminderItem> result = new ArrayList<ReminderItem>();
		while (c.moveToNext())
			result.add(getItem(c));
		return result;
	}

	public static List<ReminderItem> getAllSublist(Cursor c, int n) {
		ArrayList<ReminderItem> result = new ArrayList<ReminderItem>();
		while (c.moveToNext() && n-- > 0)
			result.add(getItem(c));
		return result;
	}

	class ReminderSQLHelper extends SQLiteOpenHelper {
		public ReminderSQLHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }
		
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG, "creating DB");
			try {
				db.execSQL("CREATE TABLE memo (_id integer primary key autoincrement, glyph blob, comment text, date integer, color integer)");
			} catch (SQLException e) {
				Log.e(TAG, e.toString());
			}
		}
	};
}
