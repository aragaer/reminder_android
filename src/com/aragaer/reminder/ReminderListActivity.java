package com.aragaer.reminder;

import java.text.SimpleDateFormat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class ReminderListActivity extends Activity {
    ReminderDB db;

    static final int GLYPH_DIALOG_ID = 1;
    ArrayAdapter<ReminderItem> adapter;
    ListView list;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        list = new ListView(this);
        adapter = new ArrayAdapter<ReminderItem>(this, android.R.layout.activity_list_item) {
            public View getView(int position, View convertView, ViewGroup parent) {
                ReminderItem item = getItem(position);
                if (item == null)
                    return null;
                if (convertView == null)
                    convertView = ViewGroup.inflate(parent.getContext(), android.R.layout.activity_list_item, null);
                ((ImageView) convertView.findViewById(android.R.id.icon)).setImageBitmap(item.glyph);
                ((TextView) convertView.findViewById(android.R.id.text1)).setText(item.getText());

                return convertView;
            };
        };
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,	long id) {
                if (id + 1 == adapter.getCount()) {
                    showDialog(GLYPH_DIALOG_ID, null);
                } else {
                    Log.d("Reminder", "clickety " + id);
                }
            }
        });
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,	long id) {
                if (id + 1 == adapter.getCount())
                    return false;
                Log.d("Reminder", "long clickety " + id);
                return true;
            }
        });
        list.setAdapter(adapter);

        Bitmap b = Bitmap.createBitmap(50, 50, Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(0x07);
        p.setTextSize(50f);
        p.setColor(Color.WHITE);
        p.setShadowLayer(1, 0, 0, Color.BLACK);
        Rect bounds = new Rect();
        p.getTextBounds("+", 0, 1, bounds);
        c.drawText("+", 25 - bounds.centerX(), 25 - bounds.centerY(), p);
        Drawable d = getResources().getDrawable(R.drawable.new_glyph);
        d.setBounds(0, 0, 50, 50);
        d.draw(c);

        db = new ReminderDB(this);
        for (ReminderItem item : db.getAllMemos())
            adapter.add(item);

        adapter.add(new ReminderItem(b, getString(R.string.add_new)));
        setContentView(list);
    }

    protected Dialog onCreateDialog(int id, Bundle data) {
        Dialog dlg = new DrawDialog(this);
        dlg.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                Bitmap res = ((DrawView) ((Dialog) dialog).findViewById(R.id.draw)).getBitmap();
                ReminderItem item = new ReminderItem(res);
                db.storeMemo(item);
                adapter.insert(item, 0);
            }
        });
        return dlg;
    }
}
