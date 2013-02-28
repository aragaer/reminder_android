package com.aragaer.reminder;

import java.util.List;

import android.database.Cursor;
import android.database.CursorWrapper;

public class ReorderedCursor extends CursorWrapper {
	int pos;
	private final int count, pos2true[], true2pos[];
	public ReorderedCursor(Cursor cursor) {
		super(cursor);
		count = cursor.getCount();
		pos2true = new int[count];
		true2pos = new int[count];
	}

	public int getPosition() {
		return pos;
	}

	public boolean isAfterLast() {
		return pos < 0;
	}

	public boolean isBeforeFirst() {
		return pos >= count;
	}

	public boolean isFirst() {
		return pos == count - 1;
	}

	public boolean isLast() {
		return pos == 0;
	}

	final boolean try_move(int to) {
		pos = to;
		try {
//			Log.d("REORDER", "Requested "+pos+", returning "+pos2true[pos]);
			return super.moveToPosition(pos2true[to]);
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}
	public boolean moveToFirst() {
		return try_move(0);
	}

	public boolean moveToLast() {
		return try_move(count - 1);
	}

	public boolean moveToNext() {
		return try_move(pos + 1);
	}
	
	public boolean moveToPrevious() {
		return try_move(pos - 1);
	}

	public boolean moveToPosition(int position) {
		return try_move(position);
	}

	public ReorderedCursor setOrder(List<Long> ids) {
		final int id_col = getColumnIndex("_id");
		int position = 0;
		super.moveToFirst();
		do {
			final long id = super.getLong(id_col);
			int translated = count;
			while (translated-- > 0)
				if (ids.get(translated) == id)
					break;
			pos2true[translated] = position;
			true2pos[position] = translated;
			position++;
		} while (super.moveToNext());
		return this;
	}
}
