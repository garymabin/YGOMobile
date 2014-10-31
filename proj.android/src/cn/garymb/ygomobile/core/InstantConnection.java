package cn.garymb.ygomobile.core;


import org.apache.http.client.HttpClient;

import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.data.wrapper.BaseDataWrapper;

import android.util.SparseArray;

public class InstantConnection implements IBaseConnection {
	protected SparseArray<IBaseThread> mWorkThreads;
	
	private HttpClient mHttpClient;
	
	private TaskStatusCallback mCallBack;

	public InstantConnection(StaticApplication app,
			TaskStatusCallback callback) {
		mHttpClient = app.getHttpClient();
		mCallBack = callback;
		mWorkThreads = new SparseArray<IBaseThread>();
	}
	

	@Override
	public void addTask(BaseDataWrapper wrapper) {
		int requestType = wrapper.getRequestType();
		IBaseThread thread = mWorkThreads.get(requestType);
		if (thread == null || !thread.isRunning()) {
			if (requestType == Constants.REQUEST_TYPE_CHECK_UPDATE) {
				thread = new CheckUpdateThread(mCallBack, mHttpClient);
			}
			mWorkThreads.put(requestType, thread);
			((InstantThread)thread).setWrapper(wrapper);
			thread.start();
		}
	}

	@Override
	public void purge() {
		for (int i = 0; i < mWorkThreads.size(); i++) {
			IBaseThread thread = mWorkThreads.valueAt(i);
			if (thread != null) {
				thread.terminate();
			}
		}
	}


	@Override
	public int getType() {
		return CONNECTION_TYPE_INSTANT;
	}


	@Override
	public void execute() {
	}


	@Override
	public boolean isRunning() {
		return false;
	}

}
