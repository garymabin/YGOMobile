package cn.garymb.ygomobile.core;

import cn.garymb.ygomobile.data.wrapper.BaseDataWrapper;
import cn.garymb.ygomobile.net.websocket.WebSocketConnector;


public class WebSocketConnection implements IBaseConnection {
	
	private IBaseThread mUpdateThread;
	private WebSocketConnector mConnector;
	
	public WebSocketConnection(TaskStatusCallback callback) {
		mConnector = new WebSocketConnector();
		mUpdateThread = new MiscUpdateThread(callback, mConnector);
		mUpdateThread.start();
	}

	@Override
	public void addTask(BaseDataWrapper wrapper) {
		((WebSocketThread)mUpdateThread).executeTask(wrapper);
	}

	@Override
	public void purge() {
		mUpdateThread.terminate();
	}

}
