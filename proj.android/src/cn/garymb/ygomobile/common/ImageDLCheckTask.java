package cn.garymb.ygomobile.common;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.provider.YGOCards;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

public class ImageDLCheckTask extends AsyncTask<Object, Boolean, Bundle> {

	private static final String CARD_IMAGE_SUFFIX = ".jpg";
	
	private static final String NO_MEDIA_FILE_SUFFIX = ".nomedia";

	private static final String TAG = "ImageDLCheckTask";

	private FilenameFilter mFilter;
	
	private ProgressDialog mWaitDialog;

	private ImageDLCheckListener mListener;

	public interface ImageDLCheckListener {
		void onDLCheckComplete(Bundle result);
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mWaitDialog.show();
	}

	public ImageDLCheckTask(Context context) {
		mWaitDialog = new ProgressDialog(context);
		mWaitDialog.setMessage(context.getResources()
				.getString(R.string.searching_image_directory));
		mWaitDialog.setCancelable(false);
		mFilter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				boolean isAccpeted = true;
				if (!filename.endsWith(CARD_IMAGE_SUFFIX)) {
					isAccpeted = false;
				}
				return isAccpeted;
			}
		};
	}
	
	public void setImageDLCheckListener(ImageDLCheckListener listener) {
		mListener = listener;
	}

	@Override
	protected Bundle doInBackground(Object... params) {
		Bundle bundle = null;
		File imageDir = new File(StaticApplication.peekInstance()
				.getCardImagePath());
		//check no media files.
		imageDir.mkdirs();
		File noMediaFile = new File(imageDir, NO_MEDIA_FILE_SUFFIX);
		if (!noMediaFile.exists()) {
			try {
				noMediaFile.createNewFile();
			} catch (IOException e) {
				Log.w(TAG, "can not create .nomedia file");
				e.printStackTrace();
			}
		}
		String[] filenames = imageDir.list();
		List<Integer> images = null;
        if (filenames != null && filenames != null) {
        	images = new ArrayList<Integer>(filenames.length);
            for (String filename : filenames) {
                if (mFilter.accept(imageDir, filename)) {
                	filename = filename.replaceAll("\\.jpg", "");
                	if (TextUtils.isDigitsOnly(filename)) {
                		images.add(Integer.parseInt(filename));
                	}
                }
            }
        }
        Collections.sort(images);
		ContentResolver cr = StaticApplication.peekInstance()
				.getContentResolver();
		Cursor cursor = null;

		try {
			cursor = cr.query(YGOCards.Datas.CONTENT_URI,
					new String[] { YGOCards.Datas._ID }, null, null, YGOCards.Datas._ID + " ASC");
			if (cursor != null && cursor.moveToFirst()) {
				bundle = new Bundle();
				ArrayList<Integer> ids = new ArrayList<Integer>(cursor.getCount()); 
				do {
					int id = cursor.getInt(0);
					if (Collections.binarySearch(images, id) < 0) {
						ids.add(cursor.getInt(0));
					}
				} while (cursor.moveToNext());
				if (ids.size() != 0) {
					bundle.putIntegerArrayList("ids", ids);
				} else {
					bundle = null;
				}
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return bundle;
	}

	@Override
	protected void onPostExecute(Bundle result) {
		mWaitDialog.cancel();
		mWaitDialog = null;
		if (mListener != null) {
			mListener.onDLCheckComplete(result);
			mListener = null;
		}
	}

}
