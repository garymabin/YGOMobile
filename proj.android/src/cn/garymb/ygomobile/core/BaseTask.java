package cn.garymb.ygomobile.core;

import cn.garymb.ygomobile.data.wrapper.IBaseJob.JobStatusCallback;


public abstract class BaseTask implements IBaseTask, JobStatusCallback{
	
	public interface TaskStatusCallback {
		void onTaskFinish(int type, int result);
	}

	protected int mNextTask = TASK_TYPE_NONE;
	
	protected TaskStatusCallback mTaskCallback;
	
	public void setTaskStatusCallback(TaskStatusCallback callback) {
		mTaskCallback = callback;
	}
	
	public void setNextTask(int taskType) {
		mNextTask = taskType;
	}
	
	public int getNextTask() {
		return mNextTask;
	}
}
