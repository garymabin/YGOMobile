package cn.garymb.ygomobile.core;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.os.Handler;
import android.os.Message;

import com.squareup.okhttp.OkHttpClient;

import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.core.IBaseConnection.TaskStatusCallback;
import cn.garymb.ygomobile.data.wrapper.BaseDataWrapper;
import cn.garymb.ygomobile.data.wrapper.ImageDownloadWrapper;

public class ImageDownloadConnection implements IBaseConnection, TaskStatusCallback{

	private static int NUMBER_OF_CORES = Runtime.getRuntime()
			.availableProcessors() > 4 ? 4 : Runtime.getRuntime()
					.availableProcessors();

	protected BlockingQueue<BaseDataWrapper> mTaskQueue;
	
	private volatile boolean isRunning = false;
	
	private WeakReference<Handler> mHandlerRef;
	
	protected List<IBaseThread> mUpdateThreads = new ArrayList<>(
			NUMBER_OF_CORES);

	public ImageDownloadConnection(StaticApplication app,
			Handler handler) {
		mTaskQueue = new LinkedBlockingQueue<BaseDataWrapper>();
		mHandlerRef = new WeakReference<Handler>(handler);
		initThread(app, this);
	}

	protected void initThread(StaticApplication app, TaskStatusCallback callback) {
		OkHttpClient client = app.getOkHttpClient();
		for (int i = 0; i < NUMBER_OF_CORES; i++) {
			IBaseThread thread = new ImageDownloadThread(mTaskQueue, callback,
					client);
			mUpdateThreads.add(thread);
		}
	}

	@Override
	public void addTask(BaseDataWrapper wrapper) {
		if (wrapper instanceof ImageDownloadWrapper) {
			try {
				mTaskQueue.put(wrapper);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	}

	@Override
	public void purge() {
		isRunning = false;
		for (IBaseThread thread : mUpdateThreads) {
			thread.terminate();
			thread = null;
		}
		mTaskQueue.clear();
	}

	@Override
	public int getType() {
		return CONNECTION_TYPE_IMAGE_DOWNLOAD;
	}

	@Override
	public void execute() {
		for (IBaseThread thread : mUpdateThreads) {
			thread.start();
		}
		isRunning = true;
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}

	@Override
	public int getTaskCount() {
		return mTaskQueue.size();
	}


	@Override
	public void onTaskFinish(BaseDataWrapper wrapper) {
		Handler handler = mHandlerRef.get();
		synchronized (handler) {
			if (handler != null) {
				handler.sendMessage(Message.obtain(null,
						Constants.IMAGE_DL_EVENT_TYPE_DOWNLOAD_COMPLETE));
			}
		}
	}

	@Override
	public void onTaskContinue(BaseDataWrapper wrapper) {
	}

}
