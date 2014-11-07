package cn.garymb.ygomobile.controller;

import java.util.Observer;

import android.content.Context;

import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.common.NotificationMgr;
import cn.garymb.ygomobile.core.IBaseConnection;
import cn.garymb.ygomobile.core.ImageDownloadObservable;
import cn.garymb.ygomobile.core.ImageDownloadObservable.IDownloadEventCallback;

public final class CardImageDownloadManager implements IDownloadEventCallback {
	
	private ImageDownloadObservable mImageDLObservable;
	
	private UpdateController mController;
	
	private Context mContext;
	
	public CardImageDownloadManager(StaticApplication app, UpdateController controller) {
		mContext = app;
		mImageDLObservable = new ImageDownloadObservable(app);
		mImageDLObservable.setImageDowdnloadEventCallback(this);
		mController = controller;
	}
	
	/*package*/ IBaseConnection createOrGetDownloadConnection() {
		IBaseConnection connection = mController.getConnection(IBaseConnection.CONNECTION_TYPE_IMAGE_DOWNLOAD);
		if (connection == null) {
			connection = mController.newDownloadConnection(mImageDLObservable
					.getMessageHandler());
		}
		return connection;
	}
	
	/*package*/ void cleanupDownloadConnection() {
		mController.cleanupConnection(IBaseConnection.CONNECTION_TYPE_IMAGE_DOWNLOAD);
		NotificationMgr.cancelDownloadStatus(mContext);
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
