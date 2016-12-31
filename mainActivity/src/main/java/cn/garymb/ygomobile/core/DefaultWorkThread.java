package cn.garymb.ygomobile.core;

import cn.garymb.ygomobile.data.wrapper.BaseRequestJob;
import cn.garymb.ygomobile.data.wrapper.IBaseJob;
import cn.garymb.ygomobile.data.wrapper.IBaseJob.JobStatusCallback;
import cn.garymb.ygomobile.net.IBaseConnector;

public abstract class DefaultWorkThread<T> extends BaseThread {

	private IBaseConnector mConnector;

	protected BaseRequestJob mWrapper;

	public DefaultWorkThread(JobStatusCallback callback, T client) {
		super(callback);
		mConnector = initConnector(client);
	}
	
	protected abstract IBaseConnector initConnector(T client);

	/* package */ void setWrapper(BaseRequestJob wrapper) {
		mWrapper = wrapper;
	}

	@Override
	public void run() {
		super.run();
		if (isRunning) {
			try {
				if (mWrapper != null) {
					mConnector.get(mWrapper);
					mCallback.onJobFinish(mWrapper);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				mWrapper.setResult(IBaseJob.STATUS_CANCELED);
				mCallback.onJobFinish(mWrapper);
			}
		}
		isRunning = false;
	}
}
