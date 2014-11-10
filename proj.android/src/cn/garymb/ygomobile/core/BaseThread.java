package cn.garymb.ygomobile.core;

import android.os.Process;
import cn.garymb.ygomobile.core.IBaseConnection.TaskStatusCallback;


public abstract class BaseThread extends Thread implements IBaseThread {
	
	public BaseThread(TaskStatusCallback callback) {
		mCallback = callback; 
	}
	
	protected volatile boolean isRunning = true;
	
	protected TaskStatusCallback mCallback;
	
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
