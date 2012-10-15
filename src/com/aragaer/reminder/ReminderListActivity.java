package com.aragaer.reminder;

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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
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
        startService(new Intent(this, ReminderService.class));
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
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,	long id) {
                if (id + 1 == adapter.getCount())
                    showDialog(GLYPH_DIALOG_ID, null);
                else {
                    Intent i = new Intent(ReminderListActivity.this, ReminderViewActivity.class);
                    i.putExtra("reminder_id", adapter.getItem(position)._id);
                    startActivityForResult(i, 0);
                }
            }
        });

        registerForContextMenu(list);
        list.setAdapter(adapter);

        db = new ReminderDB(this);
        for (ReminderItem item : db.getAllMemos())
            adapter.add(item);

        adapter.add(new ReminderItem(add_new_bmp(this), getString(R.string.add_new)));
        setContentView(list);
    }

    static public Bitmap add_new_bmp(Context ctx) {
        Bitmap b = Bitmap.createBitmap(50, 50, Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(0x07);
        p.setTextSize(50f);
        p.setColor(Color.WHITE);
        p.setShadowLayer(1, 0, 0, Color.BLACK);
        Rect bounds = new Rect();
        p.getTextBounds("+", 0, 1, bounds);
        c.drawText("+", 25 - bounds.centerX(), 25 - bounds.centerY(), p);
        Drawable d = ctx.getResources().getDrawable(R.drawable.new_glyph);
        d.setBounds(0, 0, 49, 49);
        d.draw(c);
        return b;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ReminderViewActivity.MEMO_DELETED) {
            long id = data.getLongExtra("reminder_id", 0);
            for (int i = 0; i < adapter.getCount(); i++) {
                final ReminderItem item = adapter.getItem(i);
                if (item._id == id) {
                    adapter.remove(item);
                    break;
                }
            }
        }
    }

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
                db.storeMemo(item);
                adapter.insert(item, 0);
            }
        });
        return dlg;
    }

    protected void onDestroy() {
        super.onDestroy();

        db.close();
    }
}
