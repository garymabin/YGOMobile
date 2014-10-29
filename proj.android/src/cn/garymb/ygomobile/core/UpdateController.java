package cn.garymb.ygomobile.core;

import java.util.Map;
import java.util.WeakHashMap;

import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.core.IBaseConnection.TaskStatusCallback;
import cn.garymb.ygomobile.data.wrapper.BaseDataWrapper;
import cn.garymb.ygomobile.provider.YGOImagesDataBaseHelper;

import android.os.Message;

public class UpdateController implements TaskStatusCallback {

	private static final int UPDATE_TYPE_IMAGE_UPDATE = 0x0;
	private static final int UPDATE_MAX_TYPE = UPDATE_TYPE_IMAGE_UPDATE + 1;

	private Map<IBaseConnection, Message> mConnections;
	
	private StaticApplication mApp;
	
	public UpdateController(StaticApplication app) {
		mApp = app;
		mConnections = new WeakHashMap<IBaseConnection, Message>();
	}
	
	
	public IBaseConnection newDownloadConnection(Message msg, YGOImagesDataBaseHelper helper) {
		IBaseConnection connection = new ImageDownloadConnection(mApp, this, helper);
		mConnections.put(connection, msg);
		return connection;
	}

	@Override
	public void onTaskFinish(BaseDataWrapper wrapper) {
		Message msg = mConnections.get(wrapper.getRequestType());
		if (msg != null) {
			msg.sendToTarget();
		}
	}

	@Override
	public void onTaskContinue(BaseDataWrapper wrapper) {
	}
}
