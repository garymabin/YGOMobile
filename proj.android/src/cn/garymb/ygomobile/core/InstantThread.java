package cn.garymb.ygomobile.core;

import org.apache.http.client.HttpClient;

import cn.garymb.ygomobile.core.IBaseConnection.TaskStatusCallback;
import cn.garymb.ygomobile.data.wrapper.BaseDataWrapper;
import cn.garymb.ygomobile.data.wrapper.IBaseWrapper;
import cn.garymb.ygomobile.net.defaulthttp.BaseHttpConnector;

public abstract class InstantThread extends BaseThread {

	private BaseHttpConnector mConnector;

	protected BaseDataWrapper mWrapper;

	public InstantThread(TaskStatusCallback callback, HttpClient client) {
		super(callback);
		mConnector = initConnector(client);
	}
	
	protected abstract BaseHttpConnector initConnector(HttpClient client);

	/* package */ void setWrapper(BaseDataWrapper wrapper) {
		mWrapper = wrapper;
	}

	@Override
	public void run() {
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
