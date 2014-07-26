package cn.garymb.ygomobile.utils;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.database.CursorWindow;
import android.os.Build;

/**
 * Static utility methods for dealing with databases and {@link Cursor}s.
 */
public class DatabaseUtils {
	
    /**
     * Fills the specified cursor window by iterating over the contents of the cursor.
     * The window is filled until the cursor is exhausted or the window runs out
     * of space.
     *
     * The original position of the cursor is left unchanged by this operation.
     *
     * @param cursor The cursor that contains the data to put in the window.
     * @param position The start position for filling the window.
     * @param window The window to fill.
     * @hide
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static void cursorFillWindowHoneyComb(final Cursor cursor,
            int position, final CursorWindow window) {
        if (position < 0 || position >= cursor.getCount()) {
            return;
        }
        final int oldPos = cursor.getPosition();
        final int numColumns = cursor.getColumnCount();
        window.clear();
        window.setStartPosition(position);
        window.setNumColumns(numColumns);
        if (cursor.moveToPosition(position)) {
            do {
                if (!window.allocRow()) {
                    break;
                }
                for (int i = 0; i < numColumns; i++) {
                    final int type = cursor.getType(i);
                    final boolean success;
                    switch (type) {
                        case Cursor.FIELD_TYPE_NULL:
                            success = window.putNull(position, i);
                            break;

                        case Cursor.FIELD_TYPE_INTEGER:
                            success = window.putLong(cursor.getLong(i), position, i);
                            break;

                        case Cursor.FIELD_TYPE_FLOAT:
                            success = window.putDouble(cursor.getDouble(i), position, i);
                            break;

                        case Cursor.FIELD_TYPE_BLOB: {
                            final byte[] value = cursor.getBlob(i);
                            success = value != null ? window.putBlob(value, position, i)
                                    : window.putNull(position, i);
                            break;
                        }

                        default: // assume value is convertible to String
                        case Cursor.FIELD_TYPE_STRING: {
                            final String value = cursor.getString(i);
                            success = value != null ? window.putString(value, position, i)
                                    : window.putNull(position, i);
                            break;
                        }
                    }
                    if (!success) {
                        window.freeLastRow();
                        break;
                    }
                }
                position += 1;
            } while (cursor.moveToNext());
        }
        cursor.moveToPosition(oldPos);
    }
    
    /**
     * Fills the specified cursor window by iterating over the contents of the cursor.
     * The window is filled until the cursor is exhausted or the window runs out
     * of space.
     *
     * The original position of the cursor is left unchanged by this operation.
     *
     * @param cursor The cursor that contains the data to put in the window.
     * @param position The start position for filling the window.
     * @param window The window to fill.
     * @hide
     */
	public static void cursorFillWindow(final Cursor cursor,
            int position, final CursorWindow window) {
        if (position < 0 || position >= cursor.getCount()) {
            return;
        }
        final int oldPos = cursor.getPosition();
        final int numColumns = cursor.getColumnCount();
        window.clear();
        window.setStartPosition(position);
        window.setNumColumns(numColumns);
        if (cursor.moveToPosition(position)) {
            do {
                if (!window.allocRow()) {
                    break;
                }
				for (int i = 0; i < numColumns; i++) {
					final boolean success;
					final String value = cursor.getString(i);
					success = value != null ? window.putString(value, position,
							i) : window.putNull(position, i);
					if (!success) {
						window.freeLastRow();
						break;
					}
				}
                position += 1;
            } while (cursor.moveToNext());
        }
        cursor.moveToPosition(oldPos);
    }
}
