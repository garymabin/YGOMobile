package cn.garymb.ygomobile.controller;


import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.core.IBaseConnection;
import cn.garymb.ygomobile.core.ImageDownloadConnection;

import android.os.Handler;
import android.support.v4.util.SparseArrayCompat;

public class UpdateController {

	private static final int UPDATE_TYPE_IMAGE_UPDATE = 0x0;
	private static final int UPDATE_MAX_TYPE = UPDATE_TYPE_IMAGE_UPDATE + 1;

	private SparseArrayCompat<IBaseConnection> mConnections;

	private StaticApplication mApp;

	public UpdateController(StaticApplication app) {
		mApp = app;
		mConnections = new SparseArrayCompat<IBaseConnection>();
	}

	public IBaseConnection newDownloadConnection(Handler target) {
		IBaseConnection connection = new ImageDownloadConnection(mApp, target);
		mConnections.put(connection.getType(), connection);
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
}
