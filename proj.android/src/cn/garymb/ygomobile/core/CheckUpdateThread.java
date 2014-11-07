package cn.garymb.ygomobile.core;

import org.apache.http.client.HttpClient;

import cn.garymb.ygomobile.core.IBaseConnection.TaskStatusCallback;
import cn.garymb.ygomobile.net.defaulthttp.BaseHttpConnector;
import cn.garymb.ygomobile.net.defaulthttp.DataHttpConnector;

public class CheckUpdateThread extends InstantThread {

	public CheckUpdateThread(TaskStatusCallback callback, HttpClient client) {
		super(callback, client);
	}

	@Override
	protected BaseHttpConnector initConnector(HttpClient client) {
		return new DataHttpConnector(client);
	}
}
