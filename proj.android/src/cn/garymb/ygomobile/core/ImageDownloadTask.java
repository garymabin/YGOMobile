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

import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.data.wrapper.BaseRequestJob;
import cn.garymb.ygomobile.data.wrapper.IBaseJob;
import cn.garymb.ygomobile.data.wrapper.IBaseJob.JobStatusCallback;
import cn.garymb.ygomobile.data.wrapper.ImageDownloadJob;

public class ImageDownloadTask extends BaseTask {

	private static int MAX_NUMBER_OF_THREADS = Runtime.getRuntime()
			.availableProcessors() > 4 ? 4 : Runtime.getRuntime()
			.availableProcessors();

	protected BlockingQueue<BaseRequestJob> mTaskQueue;

	private volatile boolean isRunning = false;

	private WeakReference<Handler> mHandlerRef;

	protected List<IBaseThread> mUpdateThreads = new ArrayList<>(
			MAX_NUMBER_OF_THREADS);

	private CloseableHttpPipeliningClient mClient;

	public ImageDownloadTask(StaticApplication app, Handler handler) {
		mTaskQueue = new LinkedBlockingQueue<BaseRequestJob>();
		mHandlerRef = new WeakReference<Handler>(handler);
		mClient = app.getPipelinlingHttpClient();
		initThread(app, this);
	}

	protected void initThread(StaticApplication app, JobStatusCallback callback) {
		for (int i = 0; i < MAX_NUMBER_OF_THREADS; i++) {
			IBaseThread thread = null;
			thread = new PipeliningImageDownloadThread(mTaskQueue, callback, mClient);
			mUpdateThreads.add(thread);
		}
	}

	@Override
	public void addJob(BaseRequestJob wrapper) {
		if (wrapper instanceof ImageDownloadJob) {
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
        try {
			mClient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mClient = null;
	}

	@Override
	public int getType() {
		return TASK_TYPE_IMAGE_DOWNLOAD;
	}

	@Override
	public void execute() {
		for (IBaseThread thread : mUpdateThreads) {
			if (!thread.isRunning()) {
				thread.start();
			}
		}
		isRunning = true;
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}

	@Override
	public int getJobCount() {
		return mTaskQueue.size();
	}

	@Override
	public void onJobFinish(BaseRequestJob wrapper) {
		if (wrapper.getResult() == IBaseJob.STATUS_SUCCESS
				|| wrapper.isFailed()) {
			Handler handler = mHandlerRef.get();
			synchronized (handler) {
				if (handler != null) {
					handler.sendMessage(Message.obtain(null,
							Constants.IMAGE_DL_EVENT_TYPE_DOWNLOAD_COMPLETE));
				}
			}
		} else {
			if (wrapper.getRetryCount() <= BaseRequestJob.MAX_RETRY_COUNT) {
				addJob(wrapper);
			}
		}
	}

	@Override
	public void onJobContinue(BaseRequestJob wrapper) {
	}

}
