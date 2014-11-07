package cn.garymb.ygomobile.core;

import java.util.concurrent.BlockingQueue;


import com.squareup.okhttp.OkHttpClient;

import cn.garymb.ygomobile.core.IBaseConnection.TaskStatusCallback;
import cn.garymb.ygomobile.data.wrapper.BaseDataWrapper;
import cn.garymb.ygomobile.net.IBaseConnector;
import cn.garymb.ygomobile.net.defaulthttp.OkHttpConector;

public class ImageDownloadThread extends BaseThread {
	
	protected volatile boolean isRunning = true;
	private IBaseConnector mConnector;
	private BlockingQueue<BaseDataWrapper> mQueue;

	public ImageDownloadThread(BlockingQueue<BaseDataWrapper> queue, TaskStatusCallback callback, OkHttpClient client) {
		super(callback);
		mConnector = new OkHttpConector(client);
		mQueue = queue;
	}
	
	@Override
	public void run() {
		BaseDataWrapper wrapper = null;
		while (isRunning && !isInterrupted()) {
			try {
				wrapper = mQueue.take();
				if (wrapper != null) {
					mConnector.get(wrapper);
					mCallback.onTaskFinish(wrapper);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void terminate() {
		if (isRunning) {
			interrupt();
			isRunning = false;
			mQueue = null;
		}
	}
	
	@Override
	public boolean isRunning() {
		return isRunning;
	}
	
}
