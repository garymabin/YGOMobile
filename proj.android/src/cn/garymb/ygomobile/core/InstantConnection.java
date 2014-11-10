package cn.garymb.ygomobile.core;

import org.apache.http.client.HttpClient;

import com.squareup.okhttp.OkHttpClient;

import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.core.IBaseConnection.TaskStatusCallback;
import cn.garymb.ygomobile.data.wrapper.BaseRequestWrapper;

import android.util.SparseArray;

public class InstantConnection implements IBaseConnection, TaskStatusCallback {
	protected SparseArray<IBaseThread> mWorkThreads;
	
	private HttpClient mHttpClient;
	
	private OkHttpClient mOkHttpClient;
	
	public InstantConnection(StaticApplication app) {
		mHttpClient = app.getHttpClient();
		mOkHttpClient = app.getOkHttpClient();
		mWorkThreads = new SparseArray<IBaseThread>();
	}
	

	@Override
	public void addTask(BaseRequestWrapper wrapper) {
		int requestType = wrapper.getRequestType();
		IBaseThread thread = mWorkThreads.get(requestType);
		if (thread == null || !thread.isRunning()) {
			if (requestType == Constants.REQUEST_TYPE_CHECK_UPDATE) {
				thread = new CheckUpdateThread(this, mHttpClient);
			} else if (requestType == Constants.REQUEST_TYPE_MYCARD_API_GET_CARDIMAGE_URL) {
				thread = new MyCardAPIThead(this, mOkHttpClient);
			}
			mWorkThreads.put(requestType, thread);
			((DefaultWorkThread<?>)thread).setWrapper(wrapper);
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


	@Override
	public int getTaskCount() {
		return 1;
	}


	@Override
	public void onTaskFinish(BaseRequestWrapper wrapper) {
	}


	@Override
	public void onTaskContinue(BaseRequestWrapper wrapper) {
	}

}
