package cn.garymb.ygomobile.core;

import cn.garymb.ygomobile.core.IBaseConnection.TaskStatusCallback;
import cn.garymb.ygomobile.data.wrapper.BaseRequestWrapper;
import cn.garymb.ygomobile.data.wrapper.IBaseWrapper;
import cn.garymb.ygomobile.net.IBaseConnector;

public abstract class DefaultWorkThread<T> extends BaseThread {

	private IBaseConnector mConnector;

	protected BaseRequestWrapper mWrapper;

	public DefaultWorkThread(TaskStatusCallback callback, T client) {
		super(callback);
		mConnector = initConnector(client);
	}
	
	protected abstract IBaseConnector initConnector(T client);

	/* package */ void setWrapper(BaseRequestWrapper wrapper) {
		mWrapper = wrapper;
	}

	@Override
	public void run() {
		super.run();
		if (isRunning) {
			try {
				if (mWrapper != null) {
					mConnector.get(mWrapper);
					mCallback.onTaskFinish(mWrapper);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				mWrapper.setResult(IBaseWrapper.TASK_STATUS_CANCELED);
				mCallback.onTaskFinish(mWrapper);
			}
		}
		isRunning = false;
	}
}
