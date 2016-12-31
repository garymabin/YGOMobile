package cn.garymb.ygomobile.utils;

import java.io.File;
import java.io.IOException;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.provider.YGOCardsProvider;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Build;

/**
 * Static utility methods for dealing with databases and {@link Cursor}s.
 */
public class DatabaseUtils {

	/**
	 * Fills the specified cursor window by iterating over the contents of the
	 * cursor. The window is filled until the cursor is exhausted or the window
	 * runs out of space.
	 * 
	 * The original position of the cursor is left unchanged by this operation.
	 * 
	 * @param cursor
	 *            The cursor that contains the data to put in the window.
	 * @param position
	 *            The start position for filling the window.
	 * @param window
	 *            The window to fill.
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
						success = window
								.putLong(cursor.getLong(i), position, i);
						break;

					case Cursor.FIELD_TYPE_FLOAT:
						success = window.putDouble(cursor.getDouble(i),
								position, i);
						break;

					case Cursor.FIELD_TYPE_BLOB: {
						final byte[] value = cursor.getBlob(i);
						success = value != null ? window.putBlob(value,
								position, i) : window.putNull(position, i);
						break;
					}

					default: // assume value is convertible to String
					case Cursor.FIELD_TYPE_STRING: {
						final String value = cursor.getString(i);
						success = value != null ? window.putString(value,
								position, i) : window.putNull(position, i);
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
	 * Fills the specified cursor window by iterating over the contents of the
	 * cursor. The window is filled until the cursor is exhausted or the window
	 * runs out of space.
	 * 
	 * The original position of the cursor is left unchanged by this operation.
	 * 
	 * @param cursor
	 *            The cursor that contains the data to put in the window.
	 * @param position
	 *            The start position for filling the window.
	 * @param window
	 *            The window to fill.
	 * @hide
	 */
	public static void cursorFillWindow(final Cursor cursor, int position,
			final CursorWindow window) {
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

	@SuppressLint("SdCardPath")
	private static boolean checkDataBase(Context context, String path,
			boolean needsUpdate) {
		if (needsUpdate) {
			new File(path, YGOCardsProvider.DATABASE_NAME).delete();
			return false;
		}
		return new File(path, YGOCardsProvider.DATABASE_NAME).exists();
	}
	
	public static boolean checkAndCopyFromExternalDatabase(Context context, String fromPath, String toPath,
			boolean needsUpdate) {
		boolean result = true;
		if (!checkDataBase(context, toPath, needsUpdate)) {
			try {
				new File(toPath).mkdirs();
				FileOpsUtils.copyFileUsingFileChannels(new File(fromPath), new File(toPath, YGOCardsProvider.DATABASE_NAME));
				doSomeTrickOnDatabase(toPath);
			} catch (IOException e) {
				result = false;
				e.printStackTrace();
			} catch (SQLiteException e) {
				e.printStackTrace();
				result = false;
			}
		}
		return result;
	}

	public static boolean checkAndCopyFromInternalDatabase(Context context, String path,
			boolean needsUpdate) {
		boolean result = true;
		if (!checkDataBase(context, path, needsUpdate)) {
			try {
				new File(path).mkdirs();
				FileOpsUtils.copyRawData(path + File.separator + YGOCardsProvider.DATABASE_NAME,
						R.raw.cards);
				doSomeTrickOnDatabase(path);
			} catch (IOException e) {
				result = false;
				e.printStackTrace();
			} catch (SQLiteException e) {
				e.printStackTrace();
				result = false;
			}
		}
		return result;
	}

	private static void doSomeTrickOnDatabase(String path)
			throws SQLiteException {
		SQLiteDatabase db = null;
		String myPath = path + File.separator + YGOCardsProvider.DATABASE_NAME;
		db = SQLiteDatabase.openDatabase(myPath, null,
				SQLiteDatabase.OPEN_READWRITE);
		try {
			db.beginTransaction();
			db.execSQL("ALTER TABLE datas RENAME TO datas_backup;");
			db.execSQL("CREATE TABLE datas (_id integer PRIMARY KEY, ot integer, alias integer, setcode integer, type integer,"
					+ " atk integer, def integer, level integer, race integer, attribute integer, category integer);");
			db.execSQL("INSERT INTO datas (_id, ot, alias, setcode, type, atk, def, level, race, attribute, category) "
					+ "SELECT id, ot, alias, setcode, type, atk, def, level, race, attribute, category FROM datas_backup;");
			db.execSQL("DROP TABLE datas_backup;");
			db.execSQL("ALTER TABLE texts RENAME TO texts_backup;");
			db.execSQL("CREATE TABLE texts (_id integer PRIMARY KEY, name varchar(128), desc varchar(1024),"
					+ " str1 varchar(256), str2 varchar(256), str3 varchar(256), str4 varchar(256), str5 varchar(256),"
					+ " str6 varchar(256), str7 varchar(256), str8 varchar(256), str9 varchar(256), str10 varchar(256),"
					+ " str11 varchar(256), str12 varchar(256), str13 varchar(256), str14 varchar(256), str15 varchar(256), str16 varchar(256));");
			db.execSQL("INSERT INTO texts (_id, name, desc, str1, str2, str3, str4, str5, str6, str7, str8, str9, str10, str11, str12, str13, str14, str15, str16)"
					+ " SELECT id, name, desc, str1, str2, str3, str4, str5, str6, str7, str8, str9, str10, str11, str12, str13, str14, str15, str16 FROM texts_backup;");
			db.execSQL("DROP TABLE texts_backup;");
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		if (db != null) {
			db.close();
		}
	}

}
