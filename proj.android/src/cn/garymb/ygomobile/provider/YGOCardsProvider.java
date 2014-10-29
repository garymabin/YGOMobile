package cn.garymb.ygomobile.provider;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cn.garymb.ygomobile.R;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

public class YGOCardsProvider extends ContentProvider {

	public static final class DataBaseHelper extends SQLiteOpenHelper {

		private SQLiteDatabase mDataBase;

		private String mDataBasePath;

		private final Context mContext;

		/**
		 * Constructor Takes and keeps a reference of the passed context in
		 * order to access to the application assets and resources.
		 * 
		 * @param context
		 */
		public DataBaseHelper(Context context) {

			super(context, DATABASE_NAME, null, 1);
			this.mContext = context;
			if (android.os.Build.VERSION.SDK_INT >= 17) {
				mDataBasePath = context.getApplicationInfo().dataDir
						+ "/databases/";
			} else {
				mDataBasePath = "/data/data/" + context.getPackageName()
						+ "/databases/";
			}
		}

		/**
		 * Creates a empty database on the system and rewrites it with your own
		 * database.
		 * */
		public void createDataBase() throws IOException {

			boolean dbExist = checkDataBase();

			if (dbExist) {
				// do nothing - database already exist
			} else {
				// By calling this method and empty database will be created
				// into the default system path
				// of your application so we are gonna be able to overwrite that
				// database with our database.
				this.getReadableDatabase();

				try {
					copyDataBase();
				} catch (IOException e) {
					throw new Error("Error copying database");
				}
			}

		}

		/**
		 * Check if the database already exist to avoid re-copying the file each
		 * time you open the application.
		 * 
		 * @return true if it exists, false if it doesn't
		 */
		private boolean checkDataBase() {

			SQLiteDatabase checkDB = null;

			try {
				String myPath = mDataBasePath + DATABASE_NAME;
				checkDB = SQLiteDatabase.openDatabase(myPath, null,
						SQLiteDatabase.OPEN_READONLY);

			} catch (SQLiteException e) {
				// database does't exist yet.
				e.printStackTrace();
			}
			if (checkDB != null) {
				checkDB.close();
			}

			return checkDB != null ? true : false;
		}

		/**
		 * Copies your database from your local assets-folder to the just
		 * created empty database in the system folder, from where it can be
		 * accessed and handled. This is done by transfering bytestream.
		 * */
		private void copyDataBase() throws IOException {

			// Open your local db as the input stream
			InputStream myInput = mContext.getResources().openRawResource(
					R.raw.cards);
			// Path to the just created empty db
			String outFileName = mDataBasePath + DATABASE_NAME;
			// Open the empty db as the output stream
			OutputStream myOutput = new FileOutputStream(outFileName);
			// transfer bytes from the inputfile to the outputfile
			byte[] buffer = new byte[1024];
			int length;
			while ((length = myInput.read(buffer)) > 0) {
				myOutput.write(buffer, 0, length);
			}
			// Close the streams
			myOutput.flush();
			myOutput.close();
			myInput.close();

		}

		@Override
		public synchronized void close() {

			if (mDataBase != null)
				mDataBase.close();

			super.close();

		}

		@Override
		public void onCreate(SQLiteDatabase db) {
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		}

	}

	public static final String DATABASE_NAME = "cards.cdb";

	private DataBaseHelper mOpenHelper;
	private static final UriMatcher s_urlMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	private static final int CARD_DATA = 1;
	private static final int CARD_TEXT = 2;
	private static final int CARD_DATA_ID = 3;
	private static final int CARD_TEXT_ID = 4;
	private static final int CARD_COMBINED = 5;

	private static final String TABLE_DATA = "datas";
	private static final String TABLE_TEXT = "texts";

	private static final String TAG = "YGOCardsProvider";

	static {
		s_urlMatcher.addURI(YGOCards.AUTHROITY, "datas", CARD_DATA);
		s_urlMatcher.addURI(YGOCards.AUTHROITY, "texts", CARD_TEXT);
		s_urlMatcher.addURI(YGOCards.AUTHROITY, "datas/#", CARD_DATA_ID);
		s_urlMatcher.addURI(YGOCards.AUTHROITY, "texts/#", CARD_TEXT_ID);
		s_urlMatcher.addURI(YGOCards.AUTHROITY, "combined", CARD_COMBINED);
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new DataBaseHelper(getContext());
		return true;
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		if (Build.VERSION.SDK_INT >= 14) {
			qb.setStrict(true);
		}
		int match = s_urlMatcher.match(uri);
		switch (match) {
		case CARD_DATA_ID:
			qb.appendWhere("_id = " + uri.getPathSegments().get(1));
		case CARD_DATA:
			qb.setTables(TABLE_DATA);
			break;
		case CARD_TEXT_ID:
			qb.appendWhere("_id = " + uri.getPathSegments().get(1));
		case CARD_TEXT:
			qb.setTables(TABLE_TEXT);
			break;
		case CARD_COMBINED:
			qb.setTables(TABLE_DATA + ", " + TABLE_TEXT);
			break;
		default:
			break;
		}
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor ret = null;
		try {
			if (match == CARD_COMBINED) {
				if (selection != null && selection.length() > 0){  
                    selection += "AND (datas." + YGOCards.Datas._ID + " = texts."+YGOCards.Texts._ID + ")";  
                }  
                else{  
                    selection = "datas." + YGOCards.Datas._ID + " = texts." + YGOCards.Texts._ID;  
                } 
			}
			ret = qb.query(db, projection, selection, selectionArgs, null,
					null, sortOrder);
		} catch (SQLException e) {
			Log.e(TAG, "got exception when querying: " + e);
		}
		return ret;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		switch (s_urlMatcher.match(uri)) {
		case CARD_DATA:
		case CARD_TEXT:
		case CARD_COMBINED:
			return "vnd.android.cursor.dir/ygocard-data";
		case CARD_DATA_ID:
		case CARD_TEXT_ID:
			return "vnd.android.cursor.item/ygocard-data";
		default:
			throw new IllegalArgumentException("UnKnown URL " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO insert is not supported for now.
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO deleted is not supported for now.
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO deleted is not supported for now.
		return 0;
	}

}
