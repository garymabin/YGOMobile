package cn.garymb.ygomobile.core;

import cn.garymb.ygomobile.core.IBaseConnection.TaskStatusCallback;
import cn.garymb.ygomobile.net.websocket.WebSocketConnector;


public class MiscUpdateThread extends WebSocketThread {

	public MiscUpdateThread(TaskStatusCallback callback,
			WebSocketConnector connector) {
		super(callback, connector);
	}
	
	@Override
	public void run() {
		if (!UpdateController.isServerUpdated) {
			synchronized (sServerLock) {
				try {
					sServerLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		super.run();
	}

}
