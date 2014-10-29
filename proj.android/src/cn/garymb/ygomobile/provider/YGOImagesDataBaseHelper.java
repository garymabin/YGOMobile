package cn.garymb.ygomobile.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class YGOImagesDataBaseHelper {
	public static final class IgoHelper extends SQLiteOpenHelper {

		/**
		 * @param context
		 * @param name
		 * @param factory
		 * @param version
		 */
		public IgoHelper(Context context, String name, CursorFactory factory,
				int version) {
			super(context, name, factory, version);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database
		 * .sqlite.SQLiteDatabase)
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TABLE_IMAGES + "(_id TEXT PRIMARY KEY,"
					+ "status INTEGER);");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database
		 * .sqlite.SQLiteDatabase, int, int)
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (newVersion > oldVersion) {
				onCreate(db);
			}
		}
	}

	private IgoHelper mDataBaseOpenHelper;

	private static final String DATABASE_NAME = "images.db";

	private static final int DATABASE_VERSION = 2 << 16;
	
	public static final String TABLE_IMAGES = "images";

	/**
	 * 
	 */
	public YGOImagesDataBaseHelper(Context context) {
		mDataBaseOpenHelper = new IgoHelper(context, DATABASE_NAME, null,
				DATABASE_VERSION);
	}

	public void beginTransation() {
		SQLiteDatabase db = mDataBaseOpenHelper.getWritableDatabase();
		db.beginTransaction();
	}

	public void endTransation() {
		SQLiteDatabase db = mDataBaseOpenHelper.getWritableDatabase();
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public void insert(String table, ContentValues initialValues) {
		SQLiteDatabase db = mDataBaseOpenHelper.getWritableDatabase();
		db.insert(table, null, initialValues);
	}

	public Cursor query(String table, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = mDataBaseOpenHelper.getReadableDatabase();
		return db.query(table, projection, selection, selectionArgs, null,
				null, sortOrder);
	}

	public int delete(String table, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mDataBaseOpenHelper.getWritableDatabase();
		return db.delete(table, selection, selectionArgs);
	}

	public int update(String table, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = mDataBaseOpenHelper.getWritableDatabase();
		return db.update(table, values, selection, selectionArgs);
	}
}
