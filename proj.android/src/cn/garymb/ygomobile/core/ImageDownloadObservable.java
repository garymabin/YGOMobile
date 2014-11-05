package cn.garymb.ygomobile.core;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.utils.FastObservable;

public class ImageDownloadObservable extends FastObservable implements Handler.Callback{
	
	public interface IDownloadEventCallback {
		void onDownloadEvent(int event);
	}	
	
	public static final int DOWNLOAD_EVENT_FINISHED = 0;

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
	public boolean handleMessage(Message msg) {
		if (msg.what == Constants.IMAGE_DL_EVENT_TYPE_DOWNLOAD_COMPLETE) {
			msg.arg1 = ++mCount;
			msg.arg2 = mTotalCount;
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
	
	public void setTotalCount(int count) {
		mTotalCount = count;
	}

}
