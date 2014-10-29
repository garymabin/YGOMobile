package cn.garymb.ygomobile.core;

import cn.garymb.ygomobile.data.wrapper.BaseDataWrapper;


public interface IBaseConnection {
	
	static final int CONNECTION_TYPE_INSTANT = 0;
	static final int CONNECTION_TYPE_IMAGE_DOWNLOAD = 1;
	
	public interface TaskStatusCallback {
		void onTaskFinish(BaseDataWrapper wrapper);
		void onTaskContinue(BaseDataWrapper wrapper);
	}
	
	
	/**
	 * Add a new task
	 * @param wrapper
	 */
	void addTask(BaseDataWrapper wrapper);
	
	/**
	 * Clean up the connection
	 */
	void purge();
	
	int getType();

}
