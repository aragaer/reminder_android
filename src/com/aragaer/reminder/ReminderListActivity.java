package com.aragaer.reminder;

import java.sql.Date;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class ReminderListActivity extends Activity {
    static final int GLYPH_DIALOG_ID = 1;
    CursorAdapter adapter;
    ListView list;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, ReminderService.class));
        list = new ListView(this);
        list.setId(1);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,	long id) {
                if (id + 1 == adapter.getCount())
                    showDialog(GLYPH_DIALOG_ID, null);
                else {
                    Intent i = new Intent(ReminderListActivity.this, ReminderViewActivity.class);
                    i.putExtra("reminder_id", adapter.getItemId(position));
                    startActivityForResult(i, 0);
                }
            }
        });

        registerForContextMenu(list);

        final ReminderItem add_new = new ReminderItem(add_new_bmp(this), getString(R.string.add_new));
        adapter = new CursorAdapter(this, managedQuery(ReminderProvider.content_uri, null, null, null, null)) {
            public int getCount() {
                return getCursor().getCount() + 1;
            }

            public View getView(int position, View view, ViewGroup parent) {
                if (view == null)
                    view = ViewGroup.inflate(parent.getContext(), android.R.layout.activity_list_item, null);
                ReminderItem item;
                final Cursor c = getCursor();
                if (position == c.getCount())
                    item = add_new;
                else {
                    c.moveToPosition(position);
                    item = ReminderProvider.getItem(c);
                }
                ((ImageView) view.findViewById(android.R.id.icon)).setImageBitmap(item.getGlyph(50));
                ((TextView) view.findViewById(android.R.id.text1)).setText(item.getText());
                return view;
            }
            public void bindView(View view, Context context, Cursor cursor) { }
            public View newView(Context context, Cursor cursor, ViewGroup parent) { return null; }
        };

        list.setAdapter(adapter);
        setContentView(list);
    }

    static public Bitmap add_new_bmp(Context ctx) {
        Resources r = ctx.getResources();
    	int size =  r.getDimensionPixelSize(R.dimen.notification_height) - 2 * r.getDimensionPixelSize(R.dimen.notification_glyph_margin);
        Bitmap b = Bitmap.createBitmap(size, size, Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(0x07);
        p.setTextSize(size);
        p.setColor(Color.WHITE);
        p.setShadowLayer(1, 0, 0, Color.BLACK);
        Rect bounds = new Rect();
        p.getTextBounds("+", 0, 1, bounds);
        c.drawText("+", size/2 - bounds.centerX(), size/2 - bounds.centerY(), p);
        Drawable d = ctx.getResources().getDrawable(R.drawable.new_glyph);
        d.setBounds(0, 0, size, size);
        d.draw(c);
        return b;
    }

//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == ReminderViewActivity.MEMO_DELETED) {
//            long id = data.getLongExtra("reminder_id", 0);
//            for (int i = 0; i < adapter.getCount(); i++) {
//                final ReminderItem item = adapter.getItem(i);
//                if (item._id == id) {
//                    adapter.remove(item);
//                    break;
//                }
//            }
//        }
//    }

    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuItem item = menu.add(R.string.delete);
        final View view = v;
        item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                Log.d("LONG", view.toString());
                return true;
            }
        });
    }

    protected Dialog onCreateDialog(int id, Bundle data) {
        final Dialog dlg = new Dialog(this);
        dlg.setTitle(R.string.add_new);
        dlg.setContentView(R.layout.drawing);
        dlg.findViewById(R.id.btn).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dlg.dismiss();
                DrawView dv = (DrawView) dlg.findViewById(R.id.draw);
                Bitmap res = dv.getBitmap();
                dv.reset();

                ReminderItem item = new ReminderItem(res);
                ContentValues row = new ContentValues();
                row.put("glyph", item.glyph_data);
                row.put("comment", item.text);
                row.put("date", item.when.getTime());
                item._id = ContentUris.parseId(getContentResolver().insert(ReminderProvider.content_uri, row));
//                adapter.insert(item, 0);
                startService(new Intent("com.aragaer.reminder.ReminderUpdate"));
            }
        });
        return dlg;
    }
}
