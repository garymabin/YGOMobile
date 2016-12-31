package cn.garymb.ygomobile.core;

import java.util.concurrent.BlockingQueue;


import okhttp3.OkHttpClient;

import cn.garymb.ygomobile.data.wrapper.BaseRequestJob;
import cn.garymb.ygomobile.data.wrapper.IBaseJob.JobStatusCallback;
import cn.garymb.ygomobile.net.IBaseConnector;
import cn.garymb.ygomobile.net.defaulthttp.OkHttpConector;

public class ImageDownloadThread extends BaseThread {
	
	protected volatile boolean isRunning = true;
	private IBaseConnector mConnector;
	private BlockingQueue<BaseRequestJob> mQueue;

	public ImageDownloadThread(BlockingQueue<BaseRequestJob> queue, JobStatusCallback callback, OkHttpClient client) {
		super(callback);
		mConnector = new OkHttpConector(client);
		mQueue = queue;
	}
	
	@Override
	public void run() {
		BaseRequestJob wrapper = null;
		while (isRunning && !isInterrupted()) {
			try {
				wrapper = mQueue.take();
				if (wrapper != null) {
					mConnector.get(wrapper);
					mCallback.onJobFinish(wrapper);
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
