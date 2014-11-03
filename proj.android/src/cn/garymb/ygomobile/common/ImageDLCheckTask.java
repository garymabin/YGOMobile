package cn.garymb.ygomobile.common;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.provider.YGOCards;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.database.CursorWindow;
import android.os.AsyncTask;
import android.os.Build;

public class ImageDLCheckTask extends AsyncTask<Object, Boolean, CursorWindow> {

	private static final String CARD_IMAGE_SUFFIX = ".jpg";

	private FilenameFilter mFilter;
	
	private ProgressDialog mWaitDialog;

	private Comparator<? super String> mIdComparator = new Comparator<String>() {

		@Override
		public int compare(String lhs, String rhs) {
			if (lhs == null || rhs == null) {
				return 0;
			}
			if (rhs.endsWith(CARD_IMAGE_SUFFIX)) {
				rhs = rhs.replaceAll("\\.jpg", "");
			}
			if (lhs.endsWith(CARD_IMAGE_SUFFIX)) {
				lhs = lhs.replaceAll("\\.jpg", "");
			}
			int li = Integer.parseInt(lhs);
			int ri = Integer.parseInt(rhs);
			return li - ri;
		}
	};

	private ImageDLCheckListener mListener;

	public interface ImageDLCheckListener {
		void onDLCheckComplete(CursorWindow result);
	}

	public ImageDLCheckTask() {
		mWaitDialog = new ProgressDialog(StaticApplication.peekInstance());
		mWaitDialog.setMessage(StaticApplication.peekInstance().getResources()
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

	@SuppressWarnings("deprecation")
	@Override
	protected CursorWindow doInBackground(Object... params) {
		CursorWindow window = null;
		File imageDir = new File(StaticApplication.peekInstance()
				.getCardImagePath());
		String[] images = imageDir.list(mFilter);
		ContentResolver cr = StaticApplication.peekInstance()
				.getContentResolver();
		Cursor cursor = null;

		try {
			cursor = cr.query(YGOCards.Datas.CONTENT_URI,
					new String[] { YGOCards.Datas._ID }, null, null, YGOCards.Datas._ID + " ASC");
			if (cursor != null && cursor.moveToFirst()) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
					window = new CursorWindow(null);
				} else {
					window = new CursorWindow(true);
				}
				int position = 0;
				window.clear();
				window.setNumColumns(1);
				window.setStartPosition(0);
				do {
					String id = cursor.getString(0);
					if (Arrays.binarySearch(images, id, mIdComparator) < 0) {
						window.putLong(cursor.getLong(0), position, 0);
						position += 1;
					}
				} while (cursor.moveToNext());
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return window;
	}

	@Override
	protected void onPostExecute(CursorWindow result) {
		mWaitDialog.cancel();
		mWaitDialog = null;
		if (mListener != null) {
			mListener.onDLCheckComplete(result);
			mListener = null;
		}
	}

}
