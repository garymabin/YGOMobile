package cn.garymb.ygomobile.core;

import java.util.concurrent.BlockingQueue;


import cn.garymb.ygomobile.data.wrapper.BaseRequestJob;
import cn.garymb.ygomobile.data.wrapper.IBaseJob.JobStatusCallback;
import cn.garymb.ygomobile.net.IBaseConnector;

public class SingleHttpThreadPool extends BaseThread {

	private BlockingQueue<BaseRequestJob> mQueue;

	protected IBaseConnector mConnector;
	
	public SingleHttpThreadPool(BlockingQueue<BaseRequestJob> queue, JobStatusCallback callback) {
		super(callback);
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
	
}

