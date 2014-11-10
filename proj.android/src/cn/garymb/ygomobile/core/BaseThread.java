package cn.garymb.ygomobile.core;

import cn.garymb.ygomobile.data.wrapper.IBaseJob.JobStatusCallback;
import android.os.Process;


public abstract class BaseThread extends Thread implements IBaseThread {
	
	public BaseThread(JobStatusCallback callback) {
		mCallback = callback; 
	}
	
	protected volatile boolean isRunning = true;
	
	protected JobStatusCallback mCallback;
	
	@Override
	public void run() {
		super.run();
		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
	}
	
	public void terminate() {
		if (isRunning) {
			interrupt();
			isRunning = false;
		}
	}
	
	@Override
	public boolean isRunning() {
		return isRunning;
	}

}
