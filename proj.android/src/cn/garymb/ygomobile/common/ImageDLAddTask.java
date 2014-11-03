package cn.garymb.ygomobile.common;

import java.lang.ref.WeakReference;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.core.IBaseConnection;
import cn.garymb.ygomobile.data.wrapper.ImageDownloadWrapper;
import cn.garymb.ygomobile.model.data.ImageItem;
import android.app.ProgressDialog;
import android.database.CursorWindow;
import android.os.AsyncTask;
import android.os.Bundle;

public class ImageDLAddTask extends AsyncTask<CursorWindow, Integer, Integer> {
	
	public interface ImageDLAddListener {
		void onDLAddComplete(Bundle result);
	}

	private ProgressDialog mWaitDialog;

	private WeakReference<IBaseConnection> mConnectionRef;

	private int mTotalCount = 0;
	
	private ImageDLAddListener mListener;

	public ImageDLAddTask(IBaseConnection connection) {
		mWaitDialog = new ProgressDialog(StaticApplication.peekInstance());
		mWaitDialog.setMessage(StaticApplication.peekInstance().getResources()
				.getString(R.string.adding_image_download_task));
		mWaitDialog.setCancelable(false);
		mConnectionRef = new WeakReference<IBaseConnection>(connection);
	}
	
	public void setImageDLAddListener(ImageDLAddListener listener) {
		mListener = listener;
	}

	@Override
	protected Integer doInBackground(CursorWindow... params) {
		IBaseConnection connection = mConnectionRef.get();
		CursorWindow window = params[0];
		if (connection != null && window != null) {
			mTotalCount = window.getNumRows();
			publishProgress(0);
			long currentTime = System.currentTimeMillis();
			long updateTime = System.currentTimeMillis();
			for (int i = 0; i < mTotalCount; i++) {
				String id = window.getString(i, 0);
				ImageItem item = new ImageItem(id, 0, 0);
				ImageDownloadWrapper wrapper = new ImageDownloadWrapper(
						IBaseConnection.CONNECTION_TYPE_IMAGE_DOWNLOAD, item);
				connection.addTask(wrapper);
				updateTime = System.currentTimeMillis();
				if (updateTime - currentTime > 500) {
					publishProgress(i + 1);
					currentTime = updateTime;
				}
			}
		}
		return 0;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		mWaitDialog.setMessage(StaticApplication
				.peekInstance()
				.getResources()
				.getString(R.string.adding_image_download_count_task,
						values[0], mTotalCount));
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		mWaitDialog.dismiss();
		mWaitDialog = null;
		if (mListener != null) {
			mListener.onDLAddComplete(null);
			mListener = null;
		}
	}

}
