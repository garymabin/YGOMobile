package cn.garymb.ygomobile.core;

import cn.garymb.ygomobile.data.wrapper.BaseRequestJob;


public interface IBaseTask {
	
	static final int TASK_TYPE_MYCARD_API = 0;
	static final int TASK_TYPE_IMAGE_DOWNLOAD = 1;
	static final int TASK_TYPE_DOWNLOAD = 2;
	static final int TASK_TYPE_NONE = -1;
	
	
	/**
	 * Add a new task
	 * @param wrapper
	 */
	void addJob(BaseRequestJob wrapper);
	
	/**
	 * Clean up the connection
	 */
	void purge();
	
	int getType();
	
	boolean isRunning();
	
	int getJobCount();
	
	void execute();
}
