package cn.garymb.ygomobile.core;

import cn.garymb.ygomobile.common.ImageDLAddTask;
import cn.garymb.ygomobile.common.ImageDLAddTask.ImageDLAddListener;
import android.app.Service;
import android.content.Intent;
import android.database.CursorWindow;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

public class DownloadService extends Service {

	public static final String ACTION_START_TASK = "action_start_task";
	public static final String ACTION_START_BATCH_TASK = "action_start_batch_task";

	public static final String ACTION_STOP_TASK = "action_pause_task";
	public static final String ACTION_STOP_ALL_TASK = "action_stop_all_task";

	private ServiceBinder mBinder;

	private IBaseConnection mDownloadConnection;

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
			initDownloadConnection();
			CursorWindow window = intent.getParcelableExtra("dl_task_window");
			ImageDLAddTask task = new ImageDLAddTask(mDownloadConnection);
			task.setImageDLAddListener(new ImageDLAddListener() {
				@Override
				public void onDLAddComplete(Bundle result) {
					mDownloadConnection.execute();
				}
			});
			if (Build.VERSION.SDK_INT >= 11) {
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, window);
			} else {
				task.execute(window);
			}

		} else if (ACTION_STOP_ALL_TASK.equals(action)) {
			mDownloadConnection.purge();
			mDownloadConnection = null;
		} else if (ACTION_START_TASK.equals(action)) {
		} else if (ACTION_STOP_TASK.equals(action)) {
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private void initDownloadConnection() {
		if (mDownloadConnection == null || !mDownloadConnection.isRunning()) {
			mDownloadConnection = Controller.peekInstance()
					.newDownloadConnection();
		}
	}

	public class ServiceBinder extends Binder {
		public DownloadService getService() {
			return DownloadService.this;
		}
	}

}
