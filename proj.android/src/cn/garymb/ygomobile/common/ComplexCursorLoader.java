package cn.garymb.ygomobile.common;

import cn.garymb.ygomobile.utils.DatabaseUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWindow;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.CursorLoader;

public class ComplexCursorLoader extends CursorLoader {
	private CursorWindow mWindow;

	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
	public ComplexCursorLoader(Context context, Uri uri, String[] projection,
			String selection, String[] selectionArgs, String sortOrder) {
		super(context, uri, projection, selection, selectionArgs, sortOrder);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
			mWindow = new CursorWindow(null);
		} else {
			mWindow = new CursorWindow(true);
		}
	}
	
	@Override
	public Cursor loadInBackground() {
		Cursor cursor =  super.loadInBackground();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			DatabaseUtils.cursorFillWindowHoneyComb(cursor, 0, mWindow);
		} else {
			DatabaseUtils.cursorFillWindow(cursor, 0, mWindow);
		}
		return cursor;
	}
	
	public CursorWindow getCursorWindow() {
		return mWindow;
	}

}
