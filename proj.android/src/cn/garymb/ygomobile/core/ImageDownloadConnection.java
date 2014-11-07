package cn.garymb.ygomobile.core;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.HC4.impl.nio.client.CloseableHttpPipeliningClient;

import android.os.Handler;
import android.os.Message;

import com.squareup.okhttp.OkHttpClient;

import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.core.IBaseConnection.TaskStatusCallback;
import cn.garymb.ygomobile.data.wrapper.BaseDataWrapper;
import cn.garymb.ygomobile.data.wrapper.IBaseWrapper;
import cn.garymb.ygomobile.data.wrapper.ImageDownloadWrapper;

public class ImageDownloadConnection implements IBaseConnection,
		TaskStatusCallback {

	private static int MAX_NUMBER_OF_THREADS = Runtime.getRuntime()
			.availableProcessors() > 4 ? 4 : Runtime.getRuntime()
			.availableProcessors();

	protected BlockingQueue<BaseDataWrapper> mTaskQueue;

	private volatile boolean isRunning = false;

	private WeakReference<Handler> mHandlerRef;

	protected List<IBaseThread> mUpdateThreads = new ArrayList<>(
			MAX_NUMBER_OF_THREADS);

	private Object mClient;

	public ImageDownloadConnection(StaticApplication app, Handler handler,
			boolean isAsync) {
		mTaskQueue = new LinkedBlockingQueue<BaseDataWrapper>();
		mHandlerRef = new WeakReference<Handler>(handler);
		initThread(app, this, isAsync);
	}

	protected void initThread(StaticApplication app,
			TaskStatusCallback callback, boolean isAsync) {
		if (isAsync) {
			mClient = app.getPipelinlingHttpClient();
		} else {
			mClient = app.getOkHttpClient();
		}

		for (int i = 0; i < MAX_NUMBER_OF_THREADS; i++) {
			IBaseThread thread = null;
			if (isAsync) {
				thread = new PipeliningImageDownloadThread(mTaskQueue,
						callback, (CloseableHttpPipeliningClient) mClient);
			} else {
				thread = new ImageDownloadThread(mTaskQueue, callback,
						(OkHttpClient) mClient);
			}
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
		mTaskQueue = null;
		if (mClient instanceof CloseableHttpPipeliningClient) {
			try {
				((CloseableHttpPipeliningClient) mClient).close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		mClient = null;
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
		if (wrapper.getResult() == IBaseWrapper.TASK_STATUS_SUCCESS
				|| wrapper.isFailed()) {
			Handler handler = mHandlerRef.get();
			synchronized (handler) {
				if (handler != null) {
					handler.sendMessage(Message.obtain(null,
							Constants.IMAGE_DL_EVENT_TYPE_DOWNLOAD_COMPLETE));
				}
			}
		} else {
			if (wrapper.getRetryCount() <= BaseDataWrapper.MAX_RETRY_COUNT) {
				addTask(wrapper);
			}
		}
	}

	@Override
	public void onTaskContinue(BaseDataWrapper wrapper) {
	}

}
