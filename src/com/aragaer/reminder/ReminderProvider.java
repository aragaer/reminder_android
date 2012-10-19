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
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class ReminderProvider extends ContentProvider {
    private static final int DATABASE_VERSION = 1;

    public static final Uri content_uri =  Uri.parse("content://com.aragaer.reminder.provider/reminder");

    private SQLiteDatabase db = null;

    private static final UriMatcher uri_matcher = new UriMatcher(0);
    private static final int REMINDER_CODE = 1;
    private static final int REMINDER_WITH_ID = 2;
    
    private static final String TAG = ReminderProvider.class.getSimpleName(); 

    static {
        uri_matcher.addURI("com.aragaer.reminder.provider", "reminder", REMINDER_CODE);
        uri_matcher.addURI("com.aragaer.reminder.provider", "reminder/#", REMINDER_WITH_ID);
    }
    
    public int delete(Uri uri, String arg1, String[] arg2) {
        switch (uri_matcher.match(uri)) {
        case REMINDER_WITH_ID:
            Log.d(TAG, "Deleting item "+ContentUris.parseId(uri));
        default:
            Log.e(TAG, "Unknown URI requested: "+uri);
            break;
        }
        return 0;
    }

    public String getType(Uri arg0) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues arg1) {
        switch (uri_matcher.match(uri)) {
        case REMINDER_CODE:
            long id = db.insert("memo", null, arg1);
            return ContentUris.withAppendedId(content_uri, id);
        default:
            Log.e(TAG, "Unknown URI requested: "+uri);
            break;
        }
        return null;
    }

    public boolean onCreate() {
        SharedPreferences prefs = getContext().getSharedPreferences("DB", Context.MODE_PRIVATE);
        int current_version = prefs.getInt("DATABASE_VERSION", 0);

        File sdcard = Environment.getExternalStorageDirectory();
        File dir = new File(sdcard, "Android");
        dir = new File(new File(dir, "data"), ReminderProvider.class.getPackage().getName());
        if (!dir.exists() && !dir.mkdirs())
            return false;

        File db_file = new File(dir, "memo.db");
        if (db_file.exists() && current_version == 0)
            db_file.delete();

        try {
            db = SQLiteDatabase.openOrCreateDatabase(db_file, null);
        } catch (SQLiteException e) {
            Log.e(TAG, e.toString());
            return false;
        }

        boolean success = false;
        if (current_version == DATABASE_VERSION)
            return true;
        if (current_version == 0)
            success = createDB(db);
        else
            success = upgradeDB(db, current_version);

        if (success)
            prefs.edit().putInt("DATABASE_VERSION", DATABASE_VERSION).commit();
        else {
            db.close();
            db = null;
        }
        return db != null;
    }

    static boolean createDB(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE memo (_id integer primary key autoincrement, glyph blob, comment text, date integer)");
            return true;
        } catch (SQLException e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    static boolean upgradeDB(SQLiteDatabase db, int old_version) {
        return false;
    }

    public Cursor query(Uri uri, String[] arg1, String arg2, String[] arg3,
            String arg4) {
        switch (uri_matcher.match(uri)) {
        case REMINDER_CODE:
            return db.query("memo", arg1, arg2, arg3, null, null, arg4);
        default:
            Log.e(TAG, "Unknown URI requested: "+uri);
            break;
        }
        return null;
    }

    public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
        return 0;
    }
    
    public static ReminderItem getItem(Cursor c) {
        return new ReminderItem(c.getLong(0), c.getBlob(1), c.getString(2), new Date(c.getLong(3)));
    }

    public static List<ReminderItem> getAll(Cursor c) {
        ArrayList<ReminderItem> result = new ArrayList<ReminderItem>();
        while (c.moveToNext())
            result.add(getItem(c));
        return result;
    }
}
