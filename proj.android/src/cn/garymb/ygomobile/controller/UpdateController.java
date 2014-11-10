package cn.garymb.ygomobile.controller;


import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.core.IBaseConnection;
import cn.garymb.ygomobile.core.ImageDownloadConnection;
import cn.garymb.ygomobile.core.InstantConnection;

import android.os.Handler;
import android.support.v4.util.SparseArrayCompat;

public class UpdateController {

	private SparseArrayCompat<IBaseConnection> mConnections;

	private StaticApplication mApp;

	public UpdateController(StaticApplication app) {
		mApp = app;
		mConnections = new SparseArrayCompat<IBaseConnection>();
	}

	public IBaseConnection newConnection(int type, Handler target) {
		IBaseConnection connection = null;
		switch (type) {
		case IBaseConnection.CONNECTION_TYPE_INSTANT:
			connection = new InstantConnection(mApp);
			break;
		case IBaseConnection.CONNECTION_TYPE_IMAGE_DOWNLOAD:
			connection = new ImageDownloadConnection(mApp, target, true);
			break;
		default:
			break;
		}
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
