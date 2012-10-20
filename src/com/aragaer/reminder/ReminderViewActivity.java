package com.aragaer.reminder;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

public class ReminderViewActivity extends Activity {
    public static final int MEMO_DELETED = 10;

    ReminderItem memo = null;
    ImageView glyph_view;

    public void onCreate(Bundle savedInstanceState) {
        long id;
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
            id = savedInstanceState.getLong("reminder_id");
        else
            id = getIntent().getLongExtra("reminder_id", 0);
        Cursor c = getContentResolver().query(ReminderProvider.content_uri, null, "_id=?", new String[] {String.format("%d", id)}, null);
        c.moveToFirst();
        memo = ReminderProvider.getItem(c);
        c.close();

        DisplayMetrics dm = getResources().getDisplayMetrics();
        glyph_view = new ImageView(this);
        glyph_view.setImageBitmap(memo.getGlyph(dm.widthPixels));
        glyph_view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        
        setContentView(glyph_view);
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("reminder_id", memo._id);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem delete = menu.add(R.string.delete);
        delete.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                getContentResolver().delete(ReminderProvider.content_uri, "_id=?", new String[] {String.format("%d", memo._id)});
                ReminderViewActivity.this.finish();
                return true;
            }
        });
        return true;
    }
}
