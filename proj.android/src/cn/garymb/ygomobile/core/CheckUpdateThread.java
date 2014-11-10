package cn.garymb.ygomobile.core;

import org.apache.http.client.HttpClient;

import cn.garymb.ygomobile.data.wrapper.IBaseJob.JobStatusCallback;
import cn.garymb.ygomobile.net.IBaseConnector;
import cn.garymb.ygomobile.net.defaulthttp.DataHttpConnector;

public class CheckUpdateThread extends DefaultWorkThread<HttpClient> {

	public CheckUpdateThread(JobStatusCallback callback, HttpClient client) {
		super(callback, client);
	}

	@Override
	protected IBaseConnector initConnector(HttpClient client) {
		return new DataHttpConnector(client);
	}
}
