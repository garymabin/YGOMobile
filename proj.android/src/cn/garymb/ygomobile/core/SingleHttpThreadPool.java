package cn.garymb.ygomobile.core;

import java.util.concurrent.BlockingQueue;


import cn.garymb.ygomobile.core.IBaseConnection.TaskStatusCallback;
import cn.garymb.ygomobile.data.wrapper.BaseRequestWrapper;
import cn.garymb.ygomobile.net.IBaseConnector;

public class SingleHttpThreadPool extends BaseThread {

	private BlockingQueue<BaseRequestWrapper> mQueue;

	protected IBaseConnector mConnector;
	
	public SingleHttpThreadPool(BlockingQueue<BaseRequestWrapper> queue, TaskStatusCallback callback) {
		super(callback);
		mQueue = queue;
		
	}

	@Override
	public void run() {
		BaseRequestWrapper wrapper = null;
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
	
}

