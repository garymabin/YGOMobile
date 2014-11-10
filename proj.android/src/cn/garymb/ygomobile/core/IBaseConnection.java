package cn.garymb.ygomobile.core;

import cn.garymb.ygomobile.data.wrapper.BaseRequestWrapper;


public interface IBaseConnection {
	
	static final int CONNECTION_TYPE_INSTANT = 0;
	static final int CONNECTION_TYPE_IMAGE_DOWNLOAD = 1;
	
	public interface TaskStatusCallback {
		void onTaskFinish(BaseRequestWrapper wrapper);
		void onTaskContinue(BaseRequestWrapper wrapper);
	}
	
	
	/**
	 * Add a new task
	 * @param wrapper
	 */
	void addTask(BaseRequestWrapper wrapper);
	
	/**
	 * Clean up the connection
	 */
	void purge();
	
	int getType();
	
	void execute();
	
	boolean isRunning();
	
	int getTaskCount();
}
