package com.aragaer.reminder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class ReminderDB {
    private static final String TAG = "DatabaseHelper";
    private static final int DATABASE_VERSION = 1;

    private SQLiteDatabase db = null;

    public ReminderDB(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("DB", Context.MODE_PRIVATE);
        int current_version = prefs.getInt("DATABASE_VERSION", 0);

        File sdcard = Environment.getExternalStorageDirectory();
        File dir = new File(sdcard, "Android");
        dir = new File(new File(dir, "data"), ReminderDB.class.getPackage().getName());
        if (!dir.exists() && !dir.mkdirs())
            return;

        File db_file = new File(dir, "memo.db");
        if (db_file.exists() && current_version == 0)
            db_file.delete();

        try {
            db = SQLiteDatabase.openOrCreateDatabase(db_file, null);
        } catch (SQLiteException e) {
            Log.e(TAG, e.toString());
            return;
        }

        boolean success = false;
        if (current_version == DATABASE_VERSION)
            return;
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
    }

    public synchronized List<ReminderItem> getAllMemos() {
        List<ReminderItem> result = new ArrayList<ReminderItem>();
        Cursor c = db.query("memo", null, null, null, null, null, null);
        if (c == null)
            return null;
        while (c.moveToNext()) {
            byte bb[] = c.getBlob(1);
            Bitmap glyph = BitmapFactory.decodeByteArray(bb, 0, bb.length);
            result.add(new ReminderItem(glyph, c.getString(2), new Date(c.getLong(3))));
        }
        c.close();
        return result;
    }

    public void storeMemo(ReminderItem item) {
        ContentValues row = new ContentValues();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        item.glyph.compress(Bitmap.CompressFormat.PNG, 100, bos);
        row.put("glyph", bos.toByteArray());
        row.put("comment", item.text);
        row.put("date", item.when.getTime());
        db.insert("memo", null, row);
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
}
