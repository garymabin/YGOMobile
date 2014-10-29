package cn.garymb.ygomobile.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.squareup.okhttp.OkHttpClient;

import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.data.wrapper.BaseDataWrapper;
import cn.garymb.ygomobile.data.wrapper.ImageDownloadWrapper;
import cn.garymb.ygomobile.provider.YGOImagesDataBaseHelper;

public class ImageDownloadConnection implements IBaseConnection {

	private static int NUMBER_OF_CORES = Runtime.getRuntime()
			.availableProcessors();

	protected BlockingQueue<BaseDataWrapper> mTaskQueue;
	
	private YGOImagesDataBaseHelper mHelper;

	protected List<IBaseThread> mUpdateThreads = new ArrayList<>(
			NUMBER_OF_CORES);

	public ImageDownloadConnection(StaticApplication app,
			TaskStatusCallback callback, YGOImagesDataBaseHelper helper) {
		mTaskQueue = new LinkedBlockingQueue<BaseDataWrapper>();
		initThread(app, callback);
	}

	protected void initThread(StaticApplication app, TaskStatusCallback callback) {
		OkHttpClient client = app.getOkHttpClient();
		for (int i = 0; i < NUMBER_OF_CORES; i++) {
			IBaseThread thread = new ImageDownloadThread(mTaskQueue, callback,
					client);
			thread.start();
			mUpdateThreads.add(thread);
		}
	}

	@Override
	public void addTask(BaseDataWrapper wrapper) {
		if (wrapper instanceof ImageDownloadWrapper) {
			((ImageDownloadWrapper) wrapper).setDataBaseHelper(mHelper);
			try {
				mTaskQueue.put(wrapper);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	}

	@Override
	public void purge() {
		for (IBaseThread thread : mUpdateThreads) {
			thread.terminate();
		}
		mTaskQueue.clear();
	}

	@Override
	public int getType() {
		return CONNECTION_TYPE_IMAGE_DOWNLOAD;
	}

}
