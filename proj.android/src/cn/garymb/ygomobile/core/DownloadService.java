package cn.garymb.ygomobile.core;

import cn.garymb.ygomobile.common.NotificationMgr;
import cn.garymb.ygomobile.controller.Controller;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class DownloadService extends Service {

	public static final String ACTION_START_TASK = "action_start_task";
	public static final String ACTION_START_BATCH_TASK = "action_start_batch_task";

	public static final String ACTION_STOP_TASK = "action_pause_task";
	public static final String ACTION_STOP_ALL_TASK = "action_stop_all_task";
	
	public static final String BUNDLE_KEY_BATCH_TASK = "dl_task_window";

	private ServiceBinder mBinder;

	private IBaseConnection mDownloadConnection;
	
	private boolean isDownloading = false;

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mBinder = new ServiceBinder();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String action = null;
		if (intent != null) {
			action = intent.getAction();
		}
		if (ACTION_START_BATCH_TASK.equals(action)) {
			mDownloadConnection = Controller.peekInstance().createOrGetDownloadConnection();
			Controller.peekInstance().setTotalDownloadCount(mDownloadConnection.getTaskCount());
			NotificationMgr.showDownloadStatus(this, mDownloadConnection.getTaskCount());
			mDownloadConnection.execute();
			isDownloading = true;
		} else if (ACTION_STOP_ALL_TASK.equals(action)) {
			mDownloadConnection.purge();
			mDownloadConnection = null;
			isDownloading = false;
		} else if (ACTION_START_TASK.equals(action)) {
		} else if (ACTION_STOP_TASK.equals(action)) {
		}
		if (mDownloadConnection == null) {
			stopSelf();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	public class ServiceBinder extends Binder {
		public DownloadService getService() {
			return DownloadService.this;
		}
	}
	
	public boolean isDownloading() {
		return isDownloading;
	}
}
