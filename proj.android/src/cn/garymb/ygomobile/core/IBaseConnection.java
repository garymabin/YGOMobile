package cn.garymb.ygomobile.core;

import cn.garymb.ygomobile.data.wrapper.BaseDataWrapper;


public interface IBaseConnection {
	
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

}
