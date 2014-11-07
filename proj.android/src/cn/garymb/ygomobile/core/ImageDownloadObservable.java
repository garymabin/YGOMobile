package cn.garymb.ygomobile.core;


import java.util.Observer;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.utils.FastObservable;

public class ImageDownloadObservable extends FastObservable implements Handler.Callback{
	
	public interface IDownloadEventCallback {
		void onDownloadEvent(int event);
	}	
	
	public static final int DOWNLOAD_EVENT_FINISHED = 0;

	private static final String TAG = "ImageDownloadObservable";

	private Handler mHandler;
	
	private int mCount = 0;
	private int mTotalCount = 0;
	
	private IDownloadEventCallback mCallback;
	
	public ImageDownloadObservable(Context context) {
		mHandler = new Handler(context.getMainLooper(), this);
		reset();
	}
	
	public void setImageDowdnloadEventCallback(IDownloadEventCallback callback) {
		mCallback = callback;
	}
	
	
	private void reset() {
		mCount = 0;
		mTotalCount = 0;
	}
	
	@Override
	public void addObserver(Observer observer) {
		super.addObserver(observer);
		fireFastNotify(Message.obtain(null, Constants.IMAGE_DL_EVENT_TYPE_DOWNLOAD_COMPLETE, mCount, mTotalCount));
	}


	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == Constants.IMAGE_DL_EVENT_TYPE_DOWNLOAD_COMPLETE) {
			msg.arg1 = ++mCount;
			msg.arg2 = mTotalCount;
			Log.i(TAG, "receive download complete event count = " + mCount + " total = " + mTotalCount);
			if (mCount == mTotalCount) {
				if (mCallback != null) {
					mCallback.onDownloadEvent(DOWNLOAD_EVENT_FINISHED);
				}
			}
			fireFastNotify(msg);
			return true;
		}
		return false;
	}
	
	
	public Handler getMessageHandler() {
		return mHandler;
	}
	
	public void initWithTotalCount(int count) {
		mTotalCount = count;
		mCount = 0;
	}

}
