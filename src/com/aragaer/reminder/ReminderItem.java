package com.aragaer.reminder;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.Bitmap;

class ReminderItem {
    long _id = -1;
    Bitmap glyph;
    String text;
    Date when;

    static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public ReminderItem(Bitmap b) {
        this(b, null, new Date());
    }

    public ReminderItem(Bitmap b, String s) {
        this(b, s, new Date());
    }

    public ReminderItem(Bitmap b, String s, Date w) {
        glyph = b;
        text = s;
        when = w;
    }

    public ReminderItem(long id, Bitmap b, String s, Date w) {
        _id = id;
        glyph = b;
        text = s;
        when = w;
    }

    public String getText() {
        return text == null ? df.format(when) : text;
    }
}