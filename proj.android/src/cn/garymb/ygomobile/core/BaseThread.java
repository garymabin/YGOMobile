package cn.garymb.ygomobile.core;

import cn.garymb.ygomobile.core.IBaseConnection.TaskStatusCallback;


public abstract class BaseThread extends Thread implements IBaseThread{
	
	public BaseThread(TaskStatusCallback callback) {
		// TODO Auto-generated constructor stub
		mCallback = callback; 
	}
	
	protected volatile boolean isRunning = true;
	
	protected TaskStatusCallback mCallback;
	
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
