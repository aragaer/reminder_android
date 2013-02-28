package com.aragaer.reminder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.EditText;

public class LinedEditText extends EditText {
    private final Rect r = new Rect();
    private static final Paint paint = new Paint();
    
    static {
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0x80808080);
    }

    public LinedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onDraw(Canvas canvas) {
        final int line_height = getLineHeight();
    	final int height = canvas.getHeight();
        for (int baseline = getLineBounds(0, r); baseline < height; baseline += line_height)
            canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1, paint);
        super.onDraw(canvas);
    }
}
