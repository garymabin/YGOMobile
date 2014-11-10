package cn.garymb.ygomobile.model;

import java.util.Observer;

import android.content.Context;

import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.common.NotificationMgr;
import cn.garymb.ygomobile.core.IBaseTask;
import cn.garymb.ygomobile.core.ImageDownloadObservable;
import cn.garymb.ygomobile.core.ImageDownloadObservable.IDownloadEventCallback;

public final class CardImageDownloadManager implements IDownloadEventCallback {
	
	private ImageDownloadObservable mImageDLObservable;
	
	private MyCardTracker mController;
	
	private Context mContext;
	
	public CardImageDownloadManager(StaticApplication app, MyCardTracker controller) {
		mContext = app;
		mImageDLObservable = new ImageDownloadObservable(app);
		mImageDLObservable.setImageDowdnloadEventCallback(this);
		mController = controller;
	}
	
	/*package*/ IBaseTask createOrGetDownloadConnection() {
		IBaseTask connection = mController.getTask(IBaseTask.TASK_TYPE_IMAGE_DOWNLOAD);
		if (connection == null) {
			connection = mController.newTask(IBaseTask.TASK_TYPE_IMAGE_DOWNLOAD, mImageDLObservable
					.getMessageHandler());
		}
		return connection;
	}
	
	/*package*/ void cleanupDownloadConnection() {
		mController.cleanupTask(IBaseTask.TASK_TYPE_IMAGE_DOWNLOAD);
		NotificationMgr.cancelDownloadStatus(mContext);
	}
	

	/*package*/ void exeuteDownload(Context context) {
		IBaseTask connection = mController.getTask(IBaseTask.TASK_TYPE_IMAGE_DOWNLOAD);
		NotificationMgr.showDownloadStatus(context, connection.getJobCount());
		mController.execute(IBaseTask.TASK_TYPE_IMAGE_DOWNLOAD);
	}
	
	/*package*/ void setTotalDownloadCount(int count) {
		mImageDLObservable.initWithTotalCount(count);
	}
	
	/*package*/ void registerForImageDownload(Observer o) {
		mImageDLObservable.addObserver(o);
	}
	
	/*package*/ void unregisterForImageDownload(Observer o) {
		mImageDLObservable.deleteObserver(o);
	}
	
	@Override
	public void onDownloadEvent(int event) {
		switch (event) {
		case ImageDownloadObservable.DOWNLOAD_EVENT_FINISHED:
			cleanupDownloadConnection();
			NotificationMgr.showDownloadSuccess(mContext);
			break;
		default:
			break;
		}
	}
}
