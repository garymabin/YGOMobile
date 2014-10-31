package cn.garymb.ygomobile.core;


import android.os.Handler;
import android.os.Message;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.utils.FastObservable;

public class ImageDownloadObservable extends FastObservable implements Handler.Callback{
	
	private Handler mHandler;
	
	public ImageDownloadObservable() {
		mHandler = new Handler(this);
	}
	
	
	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == Constants.IMAGE_DL_EVENT_TYPE_DOWNLOAD_COMPLETE) {
			notifyObservers(msg);
			return true;
		}
		return false;
	}
	
	
	public Message obtainOserverMesssage() {
		return Message.obtain(mHandler, Constants.IMAGE_DL_EVENT_TYPE_DOWNLOAD_COMPLETE);
	}

}
