package com.aragaer.reminder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

public class ReminderViewActivity extends Activity {
    public static final int MEMO_DELETED = 10;

    ReminderDB db;
    ReminderItem memo = null;
    ImageView glyph_view;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new ReminderDB(this);

        Log.d("VIEW", "Got "+getIntent().getLongExtra("reminder_id", -1));
        if (savedInstanceState != null)
            memo = db.getMemo(savedInstanceState.getLong("reminder_id"));
        else
            memo = db.getMemo(getIntent().getLongExtra("reminder_id", 0));
        glyph_view = new ImageView(this);
        glyph_view.setImageBitmap(memo.getGlyph(100));
        glyph_view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        
        setContentView(glyph_view);
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (memo != null)
            outState.putLong("reminder_id", memo._id);
    }

    protected void onDestroy() {
        super.onDestroy();

        db.close();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem delete = menu.add(R.string.delete);
        delete.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                db.deleteMemo(memo);
                startService(new Intent("com.aragaer.reminder.ReminderUpdate"));
                Intent i = new Intent();
                i.putExtra("reminder_id", memo._id);
                ReminderViewActivity.this.setResult(MEMO_DELETED, i);
                ReminderViewActivity.this.finish();
                return true;
            }
        });
        return true;
    }
}
