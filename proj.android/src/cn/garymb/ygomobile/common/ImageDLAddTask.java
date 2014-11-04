package cn.garymb.ygomobile.common;

import java.lang.ref.WeakReference;
import java.util.List;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.core.IBaseConnection;
import cn.garymb.ygomobile.data.wrapper.ImageDownloadWrapper;
import cn.garymb.ygomobile.model.data.ImageItem;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

public class ImageDLAddTask extends AsyncTask<Bundle, Integer, Integer> {
	
	public interface ImageDLAddListener {
		void onDLAddComplete(Bundle result);
	}

	private ProgressDialog mWaitDialog;

	private WeakReference<IBaseConnection> mConnectionRef;

	private int mTotalCount = 0;
	
	private ImageDLAddListener mListener;

	public ImageDLAddTask(Context context, IBaseConnection connection) {
		mWaitDialog = new ProgressDialog(context);
		mWaitDialog.setMessage(context.getResources()
				.getString(R.string.adding_image_download_task));
		mWaitDialog.setCancelable(false);
		mConnectionRef = new WeakReference<IBaseConnection>(connection);
	}
	
	public void setImageDLAddListener(ImageDLAddListener listener) {
		mListener = listener;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mWaitDialog.show();
	}

	@Override
	protected Integer doInBackground(Bundle... params) {
		IBaseConnection connection = mConnectionRef.get();
		Bundle tasks = params[0];
		List<Integer> ids = tasks.getIntegerArrayList("ids");
		if (connection != null && ids != null) {
			mTotalCount = ids.size();
			publishProgress(0);
			long currentTime = System.currentTimeMillis();
			long updateTime = System.currentTimeMillis();
			int i = 0;
			for (int id : ids) {
				ImageItem item = new ImageItem(String.valueOf(id), 0, 0);
				ImageDownloadWrapper wrapper = new ImageDownloadWrapper(
						IBaseConnection.CONNECTION_TYPE_IMAGE_DOWNLOAD, item);
				connection.addTask(wrapper);
				updateTime = System.currentTimeMillis();
				if (updateTime - currentTime > 500) {
					publishProgress(++i);
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
