package cn.garymb.ygomobile.core;

import org.apache.http.client.HttpClient;

import cn.garymb.ygomobile.core.IBaseConnection.TaskStatusCallback;
import cn.garymb.ygomobile.net.http.BaseHttpConnector;
import cn.garymb.ygomobile.net.http.DataHttpConnector;

public class ServerUpdateThread extends InstantThread {

	public ServerUpdateThread(TaskStatusCallback callback, HttpClient client) {
		super(callback, client);
	}

	@Override
	public void run() {
		if (isRunning) {
			super.run();
			if (!UpdateController.isServerUpdated) {
				synchronized (sServerLock) {
					sServerLock.notifyAll();
					UpdateController.isServerUpdated = true;
				}
			}
		}
	}

	@Override
	protected BaseHttpConnector initConnector(HttpClient client) {
		// TODO Auto-generated method stub
		return new DataHttpConnector(client);
	}

}
