package cn.garymb.ygomobile.core;

import java.lang.ref.WeakReference;

import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.core.IBaseConnection.TaskStatusCallback;
import cn.garymb.ygomobile.data.wrapper.BaseDataWrapper;

import android.os.Handler;
import android.os.Message;
import android.support.v4.util.SparseArrayCompat;

public class UpdateController implements TaskStatusCallback {

	private static final int UPDATE_TYPE_IMAGE_UPDATE = 0x0;
	private static final int UPDATE_MAX_TYPE = UPDATE_TYPE_IMAGE_UPDATE + 1;

	private SparseArrayCompat<IBaseConnection> mConnections;

	private SparseArrayCompat<WeakReference<Handler>> mTargets;

	private StaticApplication mApp;

	public UpdateController(StaticApplication app) {
		mApp = app;
		mConnections = new SparseArrayCompat<IBaseConnection>();
		mTargets = new SparseArrayCompat<WeakReference<Handler>>();
	}

	public IBaseConnection newDownloadConnection(Handler target) {
		IBaseConnection connection = new ImageDownloadConnection(mApp, this);
		mConnections.put(connection.getType(), connection);
		mTargets.put(connection.getType(), new WeakReference<Handler>(target));
		return connection;
	}
	
	public void cleanupConnection(int type) {
		IBaseConnection connection = mConnections.get(type);
		if (connection != null) {
			connection.purge();
		}
		mConnections.remove(type);
	}

	public IBaseConnection getConnection(int type) {
		return mConnections.get(type);
	}

	@Override
	public void onTaskFinish(BaseDataWrapper wrapper) {
		Handler handler = mTargets.get(wrapper.getRequestType()).get();
		synchronized (handler) {
			if (handler != null) {
				handler.sendMessage(Message.obtain(null,
						Constants.IMAGE_DL_EVENT_TYPE_DOWNLOAD_COMPLETE));
			}
		}
	}

	@Override
	public void onTaskContinue(BaseDataWrapper wrapper) {
	}
}
